package com.leathric.dto;

import com.leathric.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDtos {

    @Getter
    @Setter
    public static class PlaceOrderRequest {
        private String note;
    }

    @Getter
    @Setter
    public static class UpdateOrderStatusRequest {
        @NotNull
        private OrderStatus status;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderResponse {
        private Long orderId;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;
        private List<OrderItemResponse> items;
    }
}
