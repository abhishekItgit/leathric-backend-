package com.leathric.service.impl;

import com.leathric.config.AwsS3Properties;
import com.leathric.dto.ProductDto;
import com.leathric.dto.response.StorageUploadResponse;
import com.leathric.entity.Category;
import com.leathric.entity.Product;
import com.leathric.interfaces.StorageService;
import com.leathric.mapper.ProductMapper;
import com.leathric.repository.CategoryRepository;
import com.leathric.repository.ProductImageRepository;
import com.leathric.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private StorageService storageService;
    @Mock
    private AwsS3Properties awsS3Properties;
    @Mock
    private ProductImageRepository productImageRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().id(10L).name("Bags").build();
        product = Product.builder()
                .id(1L)
                .name("Brown Bag")
                .price(BigDecimal.valueOf(120))
                .category(category)
                .build();
    }

    @Test
    void uploadProductImage_shouldUploadAndPersistUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "bag.jpg", "image/jpeg", "bytes".getBytes());

        when(productRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(product));
        when(awsS3Properties.getProductImagePrefix()).thenReturn("products");
        when(storageService.upload(eq("products"), any())).thenReturn(
                StorageUploadResponse.builder().key("products/1.jpg").fileUrl("https://cdn.example.com/products/1.jpg").build()
        );

        var response = productService.uploadProductImage(1L, file);

        assertThat(response.getImageUrl()).isEqualTo("https://cdn.example.com/products/1.jpg");
        verify(storageService).upload(eq("products"), any());
        verify(productImageRepository, atLeastOnce()).save(any());
    }
}
