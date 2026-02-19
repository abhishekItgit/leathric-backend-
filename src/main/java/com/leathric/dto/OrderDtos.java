package com.leathric.dto;

import com.leathric.entity.OrderStatus;
import com.leathric.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceOrderRequest {
        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateOrderStatusRequest {
        @NotNull
        private OrderStatus status;
        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmPaymentRequest {
        @NotNull
        private String paymentReference;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal lineTotal;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderResponse {
        private Long orderId;
        private String orderNumber;
        private OrderStatus status;
        private PaymentStatus paymentStatus;
        private BigDecimal totalAmount;
        private String note;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<OrderItemResponse> items;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderTrackingResponse {
        private Long orderId;
        private String orderNumber;
        private OrderStatus currentStatus;
        private PaymentStatus paymentStatus;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;
        private List<StatusHistoryItem> timeline;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StatusHistoryItem {
        private OrderStatus status;
        private LocalDateTime timestamp;
        private String note;
    }
}
