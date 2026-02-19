package com.leathric.service;

import com.leathric.dto.OrderDtos;
import com.leathric.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    
    /**
     * Place order from user's cart
     * Creates order with CREATED status and clears cart
     */
    OrderDtos.OrderResponse placeOrder(OrderDtos.PlaceOrderRequest request);

    /**
     * Confirm payment and move order to CONFIRMED status
     */
    OrderDtos.OrderResponse confirmPayment(Long orderId, OrderDtos.ConfirmPaymentRequest request);

    /**
     * Update order status with validation and history tracking
     */
    OrderDtos.OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String note);

    /**
     * Cancel order (only allowed before SHIPPED)
     */
    OrderDtos.OrderResponse cancelOrder(Long orderId);

    /**
     * Get user's order history
     */
    Page<OrderDtos.OrderResponse> getMyOrders(Pageable pageable);

    /**
     * Get single order details
     */
    OrderDtos.OrderResponse getOrderById(Long orderId);

    /**
     * Get order tracking timeline
     */
    OrderDtos.OrderTrackingResponse getOrderTracking(Long orderId);
}
