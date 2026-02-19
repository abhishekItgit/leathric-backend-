package com.leathric.service;

import com.leathric.dto.ProductDto;
import com.leathric.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<ProductResponseDto> getAll(Pageable pageable);

    ProductResponseDto getById(Long id);

    ProductResponseDto create(ProductDto dto);

    ProductResponseDto update(Long id, ProductDto dto);

    void delete(Long id);

    /**
     * Get trending products (most recently created products)
     */
    List<ProductResponseDto> getTrending(int limit);
}
