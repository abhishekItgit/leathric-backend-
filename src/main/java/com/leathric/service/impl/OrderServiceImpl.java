package com.leathric.service.impl;

import com.leathric.dto.OrderDtos;
import com.leathric.entity.*;
import com.leathric.exception.BadRequestException;
import com.leathric.exception.ResourceNotFoundException;
import com.leathric.repository.CartRepository;
import com.leathric.repository.OrderRepository;
import com.leathric.repository.OrderStatusHistoryRepository;
import com.leathric.repository.UserRepository;
import com.leathric.service.OrderService;
import com.leathric.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;

    @Override
    @Transactional
    public OrderDtos.OrderResponse placeOrder(OrderDtos.PlaceOrderRequest request) {
        log.info("Placing order for user: {}", SecurityUtils.currentUserEmail());
        
        User user = getCurrentUser();
        Cart cart = getCartWithItems(user.getId());

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // Validate stock availability and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException(
                    String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                        product.getName(), product.getStockQuantity(), cartItem.getQuantity())
                );
            }
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);
        }

        // Create order with CREATED status
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .totalAmount(totalAmount)
                .note(request.getNote())
                .items(new ArrayList<>())
                .statusHistory(new ArrayList<>())
                .build();

        // Add initial status history
        order.addStatusHistory(OrderStatus.CREATED, "Order created from cart");

        // Create order items and deduct stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();
            
            order.getItems().add(orderItem);
            
            // Deduct stock (pessimistic lock already acquired via cart fetch)
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
        }

        // Save order (cascades to items and history)
        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order creation
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order {} created successfully for user {}", savedOrder.getId(), user.getEmail());
        return toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderDtos.OrderResponse confirmPayment(Long orderId, OrderDtos.ConfirmPaymentRequest request) {
        log.info("Confirming payment for order: {}", orderId);
        
        Order order = getOrderForUpdate(orderId);
        validateOrderOwnership(order);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BadRequestException("Payment can only be confirmed for orders in CREATED status");
        }

        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment already confirmed");
        }

        // Update payment status
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        
        // Move to CONFIRMED status
        updateStatus(order, OrderStatus.CONFIRMED, "Payment confirmed: " + request.getPaymentReference());

        Order savedOrder = orderRepository.save(order);
        log.info("Payment confirmed for order {}", orderId);
        
        return toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderDtos.OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String note) {
        log.info("Updating order {} status to {}", orderId, newStatus);
        
        Order order = getOrderForUpdate(orderId);
        
        if (order.getStatus() == newStatus) {
            throw new BadRequestException("Order is already in " + newStatus + " status");
        }

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new BadRequestException(
                String.format("Cannot transition from %s to %s", order.getStatus(), newStatus)
            );
        }

        updateStatus(order, newStatus, note);
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order {} status updated to {}", orderId, newStatus);
        return toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderDtos.OrderResponse cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);
        
        Order order = getOrderForUpdate(orderId);
        validateOrderOwnership(order);

        if (!order.getStatus().isCancellable()) {
            throw new BadRequestException(
                String.format("Order cannot be cancelled in %s status. Cancellation allowed only before SHIPPED.", 
                    order.getStatus())
            );
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }

        updateStatus(order, OrderStatus.CANCELLED, "Order cancelled by user");
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order {} cancelled successfully", orderId);
        return toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDtos.OrderResponse> getMyOrders(Pageable pageable) {
        User user = getCurrentUser();
        return orderRepository.findByUserId(user.getId(), pageable)
                .map(this::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDtos.OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        validateOrderOwnership(order);
        return toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDtos.OrderTrackingResponse getOrderTracking(Long orderId) {
        Order order = orderRepository.findByIdWithTracking(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        validateOrderOwnership(order);

        var timeline = order.getStatusHistory().stream()
                .map(history -> OrderDtos.StatusHistoryItem.builder()
                        .status(history.getStatus())
                        .timestamp(history.getTimestamp())
                        .note(history.getNote())
                        .build())
                .toList();

        return OrderDtos.OrderTrackingResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .currentStatus(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .timeline(timeline)
                .build();
    }

    // ==================== Private Helper Methods ====================

    private User getCurrentUser() {
        return userRepository.findByEmail(SecurityUtils.currentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Cart getCartWithItems(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart not found"));
    }

    private Order getOrderForUpdate(Long orderId) {
        return orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private void validateOrderOwnership(Order order) {
        String currentUserEmail = SecurityUtils.currentUserEmail();
        if (!order.getUser().getEmail().equals(currentUserEmail)) {
            throw new BadRequestException("Access denied to this order");
        }
    }

    private void updateStatus(Order order, OrderStatus newStatus, String note) {
        order.setStatus(newStatus);
        order.addStatusHistory(newStatus, note);
    }

    private OrderDtos.OrderResponse toOrderResponse(Order order) {
        var items = order.getItems().stream()
                .map(item -> {
                    BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return OrderDtos.OrderItemResponse.builder()
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .lineTotal(lineTotal)
                            .build();
                })
                .toList();

        return OrderDtos.OrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }
}
