package com.leathric.service;

import com.leathric.dto.ProductDto;
import com.leathric.dto.ProductResponseDto;
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
     * Get trending products (most recently created products)
     */
    List<ProductResponseDto> getTrending(int limit);
}
