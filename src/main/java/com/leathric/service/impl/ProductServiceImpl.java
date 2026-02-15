package com.leathric.service.impl;

import com.leathric.dto.ProductDto;
import com.leathric.entity.Category;
import com.leathric.entity.Product;
import com.leathric.exception.ResourceNotFoundException;
import com.leathric.repository.CategoryRepository;
import com.leathric.repository.ProductRepository;
import com.leathric.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public ProductDto getById(Long id) {
        return toDto(findProduct(id));
    }

    @Override
    @Transactional
    public ProductDto create(ProductDto dto) {
        Category category = findCategory(dto.getCategoryId());

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                .stockQuantity(dto.getStockQuantity())
                .category(category)
                .build();

        return toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        Product product = findProduct(id);
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategory(findCategory(dto.getCategoryId()));
        return toDto(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productRepository.delete(findProduct(id));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private ProductDto toDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .stockQuantity(product.getStockQuantity())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
