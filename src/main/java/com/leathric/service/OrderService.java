package com.leathric.service;

import com.leathric.dto.OrderDtos;
import com.leathric.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDtos.OrderResponse placeOrder(OrderDtos.PlaceOrderRequest request);

    Page<OrderDtos.OrderResponse> getMyOrders(Pageable pageable);

    OrderDtos.OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
}
