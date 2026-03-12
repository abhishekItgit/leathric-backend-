package com.leathric.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * API response for product image operations.
 */
@Getter
@Builder
public class ProductImageResponse {
    private Long productId;
    private String imageUrl;
    private String message;
}
