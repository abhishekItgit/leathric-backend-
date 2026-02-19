package com.leathric.controller;

import com.leathric.dto.ApiResponse;
import com.leathric.dto.OrderDtos;
import com.leathric.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Place order from cart
     * POST /api/orders
     */
    @PostMapping
    public ApiResponse<OrderDtos.OrderResponse> placeOrder(
            @Valid @RequestBody(required = false) OrderDtos.PlaceOrderRequest request) {
        if (request == null) {
            request = new OrderDtos.PlaceOrderRequest();
        }
        return ApiResponse.<OrderDtos.OrderResponse>builder()
                .success(true)
                .message("Order placed successfully")
                .data(orderService.placeOrder(request))
                .build();
    }

    /**
     * Confirm payment for order
     * POST /api/orders/{orderId}/confirm-payment
     */
    @PostMapping("/{orderId}/confirm-payment")
    public ApiResponse<OrderDtos.OrderResponse> confirmPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderDtos.ConfirmPaymentRequest request) {
        return ApiResponse.<OrderDtos.OrderResponse>builder()
                .success(true)
                .message("Payment confirmed successfully")
                .data(orderService.confirmPayment(orderId, request))
                .build();
    }

    /**
     * Get user's order history
     * GET /api/orders
     */
    @GetMapping
    public ApiResponse<Page<OrderDtos.OrderResponse>> getMyOrders(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.<Page<OrderDtos.OrderResponse>>builder()
                .success(true)
                .message("Order history retrieved")
                .data(orderService.getMyOrders(pageable))
                .build();
    }

    /**
     * Get single order details
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDtos.OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ApiResponse.<OrderDtos.OrderResponse>builder()
                .success(true)
                .message("Order details retrieved")
                .data(orderService.getOrderById(orderId))
                .build();
    }

    /**
     * Get order tracking timeline
     * GET /api/orders/{orderId}/tracking
     */
    @GetMapping("/{orderId}/tracking")
    public ApiResponse<OrderDtos.OrderTrackingResponse> getOrderTracking(@PathVariable Long orderId) {
        return ApiResponse.<OrderDtos.OrderTrackingResponse>builder()
                .success(true)
                .message("Order tracking retrieved")
                .data(orderService.getOrderTracking(orderId))
                .build();
    }

    /**
     * Cancel order (user)
     * PATCH /api/orders/{orderId}/cancel
     */
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderDtos.OrderResponse> cancelOrder(@PathVariable Long orderId) {
        return ApiResponse.<OrderDtos.OrderResponse>builder()
                .success(true)
                .message("Order cancelled successfully")
                .data(orderService.cancelOrder(orderId))
                .build();
    }

    /**
     * Update order status (admin only)
     * PATCH /api/orders/{orderId}/status
     */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderDtos.OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderDtos.UpdateOrderStatusRequest request) {
        String note = request.getNote() != null ? request.getNote() : "Status updated by admin";
        return ApiResponse.<OrderDtos.OrderResponse>builder()
                .success(true)
                .message("Order status updated")
                .data(orderService.updateOrderStatus(orderId, request.getStatus(), note))
                .build();
    }
}
