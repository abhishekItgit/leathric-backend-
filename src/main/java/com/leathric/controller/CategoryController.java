package com.leathric.controller;

import com.leathric.dto.ApiResponse;
import com.leathric.dto.CategoryDto;
import com.leathric.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<Page<CategoryDto>> getAll(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ApiResponse.<Page<CategoryDto>>builder().success(true).message("Categories fetched")
                .data(categoryService.getAll(pageable)).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryDto> getById(@PathVariable Long id) {
        return ApiResponse.<CategoryDto>builder().success(true).message("Category fetched")
                .data(categoryService.getById(id)).build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryDto> create(@Valid @RequestBody CategoryDto dto) {
        return ApiResponse.<CategoryDto>builder().success(true).message("Category created")
                .data(categoryService.create(dto)).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryDto> update(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        return ApiResponse.<CategoryDto>builder().success(true).message("Category updated")
                .data(categoryService.update(id, dto)).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.<Void>builder().success(true).message("Category deleted").build();
    }
}
