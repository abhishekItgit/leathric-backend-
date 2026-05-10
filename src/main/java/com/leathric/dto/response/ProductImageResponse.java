package com.leathric.dto.response;

import com.leathric.entity.ImageType;
import lombok.Builder;
import lombok.Getter;

/**
 * API response for product image operations.
 */
@Getter
@Builder
public class ProductImageResponse {
    private Long imageId;
    private Long productId;
    private String imageUrl;
    private ImageType imageType;
    private String altText;
    private Integer displayOrder;
    private boolean primary;
    private String message;
}
