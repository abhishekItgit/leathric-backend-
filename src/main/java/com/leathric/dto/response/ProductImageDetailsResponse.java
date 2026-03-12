package com.leathric.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * View DTO for a persisted product image record.
 */
@Getter
@Builder
public class ProductImageDetailsResponse {
    private Long imageId;
    private Long productId;
    private String objectKey;
    private String imageUrl;
    private String contentType;
    private Long fileSizeBytes;
    private boolean active;
    private String deletedReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
