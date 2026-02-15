package com.leathric.mapper;

import com.leathric.dto.ProductDto;
import com.leathric.dto.ProductResponseDto;
import com.leathric.entity.Category;
import com.leathric.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductDto dto, Category category) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                .stockQuantity(dto.getStockQuantity())
                .category(category)
                .build();
    }

    public void updateEntity(Product product, ProductDto dto, Category category) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategory(category);
    }

    public ProductResponseDto toResponseDto(Product product) {
        Category category = product.getCategory();

        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .categoryName(category != null ? category.getName() : null)
                .build();
    }
}
