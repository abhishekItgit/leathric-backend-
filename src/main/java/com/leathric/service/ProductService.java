package com.leathric.service;

import com.leathric.dto.ProductDto;
import com.leathric.dto.ProductResponseDto;
import com.leathric.dto.request.ProductImageReorderRequest;
import com.leathric.dto.request.ProductImageUploadRequest;
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
    List<ProductResponseDto> getTrending(int limit);

    ProductImageResponse uploadProductImage(Long productId, MultipartFile file, ProductImageUploadRequest request);
    List<ProductImageResponse> getProductImages(Long productId);
    ProductImageResponse setPrimaryImage(Long productId, Long imageId);
    void reorderImages(Long productId, ProductImageReorderRequest request);
    void deleteProductImage(Long productId, Long imageId);

    PresignedUploadUrlResponse generatePresignedUploadUrl(String fileName, String contentType);
    List<ProductResponseDto> listProductsWithImages();
}
