package com.leathric.controllers;

import com.leathric.dto.ApiResponse;
import com.leathric.dto.ProductResponseDto;
import com.leathric.dto.request.PresignedUploadUrlRequest;
import com.leathric.dto.response.ProductImageResponse;
import com.leathric.dto.response.PresignedUploadUrlResponse;
import com.leathric.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for product image management APIs.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductService productService;

    /**
     * Uploads a product image and persists the generated URL.
     */
    @PostMapping("/{productId}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductImageResponse> uploadImage(@PathVariable Long productId,
                                                         @RequestPart("file") MultipartFile file) {
        return ApiResponse.<ProductImageResponse>builder()
                .success(true)
                .message("Product image uploaded")
                .data(productService.uploadProductImage(productId, file))
                .build();
    }

    /**
     * Creates a pre-signed URL for direct browser/mobile upload to S3.
     */
    @PostMapping("/images/presigned-upload-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PresignedUploadUrlResponse> createPresignedUploadUrl(
            @Valid @RequestBody PresignedUploadUrlRequest request
    ) {
        return ApiResponse.<PresignedUploadUrlResponse>builder()
                .success(true)
                .message("Pre-signed upload URL generated")
                .data(productService.generatePresignedUploadUrl(request.getFileName(), request.getContentType()))
                .build();
    }

    /**
     * Replaces an existing product image with a new one.
     */
    @PutMapping("/{productId}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductImageResponse> updateImage(@PathVariable Long productId,
                                                         @RequestPart("file") MultipartFile file) {
        return ApiResponse.<ProductImageResponse>builder()
                .success(true)
                .message("Product image updated")
                .data(productService.updateProductImage(productId, file))
                .build();
    }

    /**
     * Deletes product image from S3 and clears DB URL.
     */
    @DeleteMapping("/{productId}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductImageResponse> deleteImage(@PathVariable Long productId) {
        return ApiResponse.<ProductImageResponse>builder()
                .success(true)
                .message("Product image deleted")
                .data(productService.deleteProductImage(productId))
                .build();
    }

    /**
     * Lists all products that currently have image URLs.
     */
    @GetMapping("/images")
    public ApiResponse<List<ProductResponseDto>> listProductsWithImages() {
        return ApiResponse.<List<ProductResponseDto>>builder()
                .success(true)
                .message("Products with images fetched successfully")
                .data(productService.listProductsWithImages())
                .build();
    }
}
