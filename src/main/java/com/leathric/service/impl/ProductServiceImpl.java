package com.leathric.service.impl;

import com.leathric.dto.ProductDto;
import com.leathric.dto.ProductResponseDto;
import com.leathric.entity.Category;
import com.leathric.entity.Product;
import com.leathric.exception.ResourceNotFoundException;
import com.leathric.mapper.ProductMapper;
import com.leathric.repository.CategoryRepository;
import com.leathric.repository.ProductRepository;
import com.leathric.service.ProductService;
import com.leathric.storage.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final S3StorageService s3StorageService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAll(Pageable pageable) {
        return productRepository.findAllProductResponses(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getById(Long id) {
        return productMapper.toResponseDto(findProductWithCategory(id));
    }

    @Override
    @Transactional
    public ProductResponseDto create(ProductDto dto) {
        return create(dto, null);
    }

    @Override
    @Transactional
    public ProductResponseDto create(ProductDto dto, MultipartFile file) {
        Category category = findCategory(dto.getCategoryId());
        Product product = productMapper.toEntity(dto, category);

        if (hasFile(file)) {
            String imageUrl = s3StorageService.uploadFile(file);
            product.setImageUrl(imageUrl);
        }

        Product createdProduct = productRepository.save(product);
        return productMapper.toResponseDto(findProductWithCategory(createdProduct.getId()));
    }

    @Override
    @Transactional
    public ProductResponseDto update(Long id, ProductDto dto) {
        return update(id, dto, null);
    }

    @Override
    @Transactional
    public ProductResponseDto update(Long id, ProductDto dto, MultipartFile file) {
        Product product = findProductWithCategory(id);
        Category category = findCategory(dto.getCategoryId());

        productMapper.updateEntity(product, dto, category);

        if (hasFile(file)) {
            String imageUrl = s3StorageService.uploadFile(file);
            product.setImageUrl(imageUrl);
        }

        return productMapper.toResponseDto(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findProductWithCategory(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getTrending(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductResponseDto> page = productRepository.findAllProductResponses(pageable);
        return page.getContent();
    }

    private Product findProductWithCategory(Long id) {
        return productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for id: " + id));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found for id: " + id));
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}
