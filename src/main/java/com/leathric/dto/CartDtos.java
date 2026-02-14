package com.leathric.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class CartDtos {

    @Getter
    @Setter
    public static class AddCartItemRequest {
        @NotNull
        private Long productId;

        @NotNull
        @Min(1)
        private Integer quantity;
    }

    @Getter
    @Setter
    public static class UpdateCartItemRequest {
        @NotNull
        @Min(1)
        private Integer quantity;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CartItemResponse {
        private Long cartItemId;
        private Long productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal lineTotal;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CartResponse {
        private Long cartId;
        private List<CartItemResponse> items;
        private BigDecimal totalAmount;
    }
}
