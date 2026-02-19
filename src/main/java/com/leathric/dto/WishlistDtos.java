package com.leathric.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class WishlistDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddToWishlistRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class WishlistItemResponse {
        private Long wishlistItemId;
        private Long productId;
        private String productName;
        private String productDescription;
        private BigDecimal price;
        private String imageUrl;
        private Integer stockQuantity;
        private String categoryName;
        private Boolean inStock;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class WishlistResponse {
        private Long wishlistId;
        private Integer itemCount;
        private List<WishlistItemResponse> items;
    }
}
