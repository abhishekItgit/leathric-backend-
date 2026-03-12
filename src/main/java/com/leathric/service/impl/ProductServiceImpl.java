package com.leathric.service.impl;

import com.leathric.config.AwsS3Properties;
import com.leathric.dto.ProductDto;
import com.leathric.dto.ProductResponseDto;
import com.leathric.dto.response.ProductImageDetailsResponse;
import com.leathric.dto.response.ProductImageResponse;
import com.leathric.dto.response.PresignedUploadUrlResponse;
import com.leathric.dto.response.StorageUploadResponse;
import com.leathric.entity.Category;
import com.leathric.entity.Product;
import com.leathric.entity.ProductImage;
import com.leathric.exception.ResourceNotFoundException;
import com.leathric.interfaces.StorageService;
import com.leathric.mapper.ProductMapper;
import com.leathric.repository.CategoryRepository;
import com.leathric.repository.ProductImageRepository;
import com.leathric.repository.ProductRepository;
import com.leathric.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final StorageService storageService;
    private final AwsS3Properties awsS3Properties;
    private final ProductImageRepository productImageRepository;

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

        StorageUploadResponse upload = null;
        if (hasFile(file)) {
            upload = storageService.upload(awsS3Properties.getProductImagePrefix(), file);
            product.setImageUrl(upload.getFileUrl());
        }

        Product createdProduct = productRepository.save(product);

        if (upload != null) {
            trackImageRecord(createdProduct, file, upload, true, null);
        }

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
            replaceProductImage(product, file, "REPLACED");
        }

        return productMapper.toResponseDto(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findProductWithCategory(id);
        if (product.getImageUrl() != null) {
            storageService.deleteByUrl(product.getImageUrl());
        }
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getTrending(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductResponseDto> page = productRepository.findAllProductResponses(pageable);
        return page.getContent();
    }

    @Override
    @Transactional
    public ProductImageResponse uploadProductImage(Long productId, MultipartFile file) {
        Product product = findProductWithCategory(productId);
        replaceProductImage(product, file, "REPLACED");

        return ProductImageResponse.builder()
                .productId(product.getId())
                .imageUrl(product.getImageUrl())
                .message("Product image uploaded successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PresignedUploadUrlResponse generatePresignedUploadUrl(String fileName, String contentType) {
        return storageService.generatePresignedUploadUrl(
                awsS3Properties.getProductImagePrefix(),
                fileName,
                contentType,
                Duration.ofSeconds(awsS3Properties.getPresignedUrlExpirationSeconds())
        );
    }

    @Override
    @Transactional
    public ProductImageResponse deleteProductImage(Long productId) {
        Product product = findProductWithCategory(productId);
        if (product.getImageUrl() != null) {
            storageService.deleteByUrl(product.getImageUrl());
            product.setImageUrl(null);
            markActiveImageInactive(productId, "DELETED");
        }
        return ProductImageResponse.builder()
                .productId(product.getId())
                .imageUrl(null)
                .message("Product image deleted successfully")
                .build();
    }

    @Override
    @Transactional
    public ProductImageResponse updateProductImage(Long productId, MultipartFile file) {
        return uploadProductImage(productId, file);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> listProductsWithImages() {
        return productRepository.findProductsWithImages();
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

    private void replaceProductImage(Product product, MultipartFile file, String reason) {
        if (product.getImageUrl() != null) {
            storageService.deleteByUrl(product.getImageUrl());
            markActiveImageInactive(product.getId(), reason);
        }

        StorageUploadResponse upload = storageService.upload(awsS3Properties.getProductImagePrefix(), file);
        product.setImageUrl(upload.getFileUrl());
        trackImageRecord(product, file, upload, true, null);
    }

    private void markActiveImageInactive(Long productId, String reason) {
        productImageRepository.findFirstByProductIdAndActiveTrueOrderByCreatedAtDesc(productId)
                .ifPresent(image -> {
                    image.setActive(false);
                    image.setDeletedReason(reason);
                    productImageRepository.save(image);
                });
    }

    private void trackImageRecord(Product product,
                                  MultipartFile file,
                                  StorageUploadResponse upload,
                                  boolean active,
                                  String deletedReason) {
        ProductImage image = ProductImage.builder()
                .product(product)
                .objectKey(upload.getKey())
                .imageUrl(upload.getFileUrl())
                .contentType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .active(active)
                .deletedReason(deletedReason)
                .build();
        productImageRepository.save(image);
    }

    private ProductImageDetailsResponse toProductImageDetails(ProductImage image) {
        return ProductImageDetailsResponse.builder()
                .imageId(image.getId())
                .productId(image.getProduct().getId())
                .objectKey(image.getObjectKey())
                .imageUrl(image.getImageUrl())
                .contentType(image.getContentType())
                .fileSizeBytes(image.getFileSizeBytes())
                .active(image.isActive())
                .deletedReason(image.getDeletedReason())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }
}
