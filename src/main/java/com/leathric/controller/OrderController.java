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

    @PostMapping
    public ApiResponse<OrderDtos.OrderResponse> placeOrder(@Valid @RequestBody(required = false) OrderDtos.PlaceOrderRequest request) {
        if (request == null) {
            request = new OrderDtos.PlaceOrderRequest();
        }
        return ApiResponse.<OrderDtos.OrderResponse>builder().success(true).message("Order placed")
                .data(orderService.placeOrder(request)).build();
    }

    @GetMapping
    public ApiResponse<Page<OrderDtos.OrderResponse>> myOrders(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.<Page<OrderDtos.OrderResponse>>builder().success(true).message("Order history fetched")
                .data(orderService.getMyOrders(pageable)).build();
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderDtos.OrderResponse> updateStatus(@PathVariable Long orderId,
                                                             @Valid @RequestBody OrderDtos.UpdateOrderStatusRequest request) {
        return ApiResponse.<OrderDtos.OrderResponse>builder().success(true).message("Order status updated")
                .data(orderService.updateOrderStatus(orderId, request.getStatus())).build();
    }
}
