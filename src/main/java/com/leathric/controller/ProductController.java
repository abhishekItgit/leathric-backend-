package com.leathric.controller;

import com.leathric.dto.ApiResponse;
import com.leathric.dto.ProductDto;
import com.leathric.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<Page<ProductDto>> getAll(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.<Page<ProductDto>>builder().success(true).message("Products fetched")
                .data(productService.getAll(pageable)).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDto> getById(@PathVariable Long id) {
        return ApiResponse.<ProductDto>builder().success(true).message("Product fetched")
                .data(productService.getById(id)).build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductDto> create(@Valid @RequestBody ProductDto dto) {
        return ApiResponse.<ProductDto>builder().success(true).message("Product created")
                .data(productService.create(dto)).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductDto> update(@PathVariable Long id, @Valid @RequestBody ProductDto dto) {
        return ApiResponse.<ProductDto>builder().success(true).message("Product updated")
                .data(productService.update(id, dto)).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.<Void>builder().success(true).message("Product deleted").build();
    }
}
