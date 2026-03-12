package com.leathric.service;

import com.leathric.dto.ProductDto;
import com.leathric.dto.ProductResponseDto;
import com.leathric.dto.response.ProductImageResponse;
import com.leathric.dto.response.PresignedUploadUrlResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    Page<ProductResponseDto> getAll(Pageable pageable);

    ProductResponseDto getById(Long id);

    ProductResponseDto create(ProductDto dto);

    ProductResponseDto create(ProductDto dto, MultipartFile file);

    ProductResponseDto update(Long id, ProductDto dto);

    ProductResponseDto update(Long id, ProductDto dto, MultipartFile file);

    void delete(Long id);

    /**
     * Get trending products (most recently created products).
     */
    List<ProductResponseDto> getTrending(int limit);

    /**
     * Uploads and assigns an image to a product.
     */
    ProductImageResponse uploadProductImage(Long productId, MultipartFile file);

    /**
     * Generates pre-signed URL for direct product image uploads.
     */
    PresignedUploadUrlResponse generatePresignedUploadUrl(String fileName, String contentType);

    /**
     * Deletes image from a product and storage backend.
     */
    ProductImageResponse deleteProductImage(Long productId);

    /**
     * Replaces existing product image with a new file.
     */
    ProductImageResponse updateProductImage(Long productId, MultipartFile file);

    /**
     * Lists products that currently have image URLs.
     */
    List<ProductResponseDto> listProductsWithImages();
}
