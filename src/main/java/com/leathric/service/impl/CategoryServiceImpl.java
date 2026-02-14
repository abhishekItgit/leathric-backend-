package com.leathric.service.impl;

import com.leathric.dto.CategoryDto;
import com.leathric.entity.Category;
import com.leathric.exception.ResourceNotFoundException;
import com.leathric.repository.CategoryRepository;
import com.leathric.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Page<CategoryDto> getAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public CategoryDto getById(Long id) {
        Category category = findCategory(id);
        return toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto create(CategoryDto dto) {
        Category category = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        return toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = findCategory(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return toDto(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findCategory(id);
        categoryRepository.delete(category);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
