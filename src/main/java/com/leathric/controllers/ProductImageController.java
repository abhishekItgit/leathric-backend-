package com.leathric.controllers;

import com.leathric.dto.ApiResponse;
import com.leathric.dto.ProductResponseDto;
import com.leathric.dto.request.PresignedUploadUrlRequest;
import com.leathric.dto.request.ProductImageReorderRequest;
import com.leathric.dto.request.ProductImageUploadRequest;
import com.leathric.dto.response.ProductImageResponse;
import com.leathric.dto.response.PresignedUploadUrlResponse;
import com.leathric.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {
    private final ProductService productService;

    @PostMapping(value = "/{productId}/images", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductImageResponse> uploadImage(@PathVariable Long productId,
                                                         @RequestPart("file") MultipartFile file,
                                                         @Valid @RequestPart("metadata") ProductImageUploadRequest request) {
        return ApiResponse.<ProductImageResponse>builder().success(true).message("Product image uploaded")
                .data(productService.uploadProductImage(productId, file, request)).build();
    }

    @GetMapping("/{productId}/images")
    public ApiResponse<List<ProductImageResponse>> getProductImages(@PathVariable Long productId) {
        return ApiResponse.<List<ProductImageResponse>>builder().success(true).message("Product images fetched")
                .data(productService.getProductImages(productId)).build();
    }

    @PutMapping("/{productId}/images/{imageId}/primary")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductImageResponse> setPrimary(@PathVariable Long productId, @PathVariable Long imageId) {
        return ApiResponse.<ProductImageResponse>builder().success(true).message("Primary image updated")
                .data(productService.setPrimaryImage(productId, imageId)).build();
    }

    @PutMapping("/{productId}/images/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> reorder(@PathVariable Long productId, @Valid @RequestBody ProductImageReorderRequest request) {
        productService.reorderImages(productId, request);
        return ApiResponse.<Void>builder().success(true).message("Images reordered").build();
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteImage(@PathVariable Long productId, @PathVariable Long imageId) {
        productService.deleteProductImage(productId, imageId);
        return ApiResponse.<Void>builder().success(true).message("Product image deleted").build();
    }

    @PostMapping("/images/presigned-upload-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PresignedUploadUrlResponse> createPresignedUploadUrl(@Valid @RequestBody PresignedUploadUrlRequest request) {
        return ApiResponse.<PresignedUploadUrlResponse>builder().success(true).message("Pre-signed upload URL generated")
                .data(productService.generatePresignedUploadUrl(request.getFileName(), request.getContentType())).build();
    }

    @GetMapping("/images")
    public ApiResponse<List<ProductResponseDto>> listProductsWithImages() {
        return ApiResponse.<List<ProductResponseDto>>builder().success(true).message("Products with images fetched successfully")
                .data(productService.listProductsWithImages()).build();
    }
}
