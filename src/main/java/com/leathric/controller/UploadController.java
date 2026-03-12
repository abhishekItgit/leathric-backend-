package com.leathric.controller;

import com.leathric.dto.ApiResponse;
import com.leathric.dto.response.ProductImageResponse;
import com.leathric.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Backward-compatible image upload controller.
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class UploadController {

    private final ProductService productService;

    /**
     * Uploads product image to S3 and updates product image URL.
     */
    @PostMapping("/{productId}/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductImageResponse> uploadImage(
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.<ProductImageResponse>builder()
                .success(true)
                .message("Image uploaded successfully")
                .data(productService.uploadProductImage(productId, file))
                .build();
    }
}
