package com.leathric.service.impl;

import com.leathric.dto.OrderDtos;
import com.leathric.entity.*;
import com.leathric.exception.BadRequestException;
import com.leathric.exception.ResourceNotFoundException;
import com.leathric.repository.CartRepository;
import com.leathric.repository.OrderRepository;
import com.leathric.repository.UserRepository;
import com.leathric.service.OrderService;
import com.leathric.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderDtos.OrderResponse placeOrder(OrderDtos.PlaceOrderRequest request) {
        User user = userRepository.findByEmail(SecurityUtils.currentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PLACED)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();
            order.getItems().add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(total);
        cart.getItems().clear();
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDtos.OrderResponse> getMyOrders(Pageable pageable) {
        User user = userRepository.findByEmail(SecurityUtils.currentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return orderRepository.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public OrderDtos.OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(status);
        return toResponse(order);
    }

    private OrderDtos.OrderResponse toResponse(Order order) {
        return OrderDtos.OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(item -> OrderDtos.OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()).toList())
                .build();
    }
}
