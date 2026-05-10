package com.leathric.service.impl;

import com.leathric.config.AwsS3Properties;
import com.leathric.dto.ProductDto;
import com.leathric.dto.ProductResponseDto;
import com.leathric.dto.request.ProductImageReorderRequest;
import com.leathric.dto.request.ProductImageUploadRequest;
import com.leathric.dto.response.ProductImageResponse;
import com.leathric.dto.response.PresignedUploadUrlResponse;
import com.leathric.dto.response.StorageUploadResponse;
import com.leathric.entity.Category;
import com.leathric.entity.ImageType;
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
import org.springframework.data.domain.*;
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

    @Transactional(readOnly = true) public Page<ProductResponseDto> getAll(Pageable pageable){return productRepository.findAllProductResponses(pageable);}    
    @Transactional(readOnly = true) public ProductResponseDto getById(Long id){Product p=findProductWithCategory(id);return productMapper.toResponseDto(p,activeImages(id));}
    @Transactional public ProductResponseDto create(ProductDto dto){return create(dto,null);}    
    @Transactional public ProductResponseDto create(ProductDto dto, MultipartFile file){Product p=productRepository.save(productMapper.toEntity(dto,findCategory(dto.getCategoryId())));if(hasFile(file)){uploadProductImage(p.getId(),file,defaultRequest());}return getById(p.getId());}
    @Transactional public ProductResponseDto update(Long id, ProductDto dto){return update(id,dto,null);}    
    @Transactional public ProductResponseDto update(Long id, ProductDto dto, MultipartFile file){Product p=findProductWithCategory(id);productMapper.updateEntity(p,dto,findCategory(dto.getCategoryId()));if(hasFile(file)){uploadProductImage(id,file,defaultRequest());}return getById(id);}    
    @Transactional public void delete(Long id){productRepository.delete(findProductWithCategory(id));}
    @Transactional(readOnly = true) public List<ProductResponseDto> getTrending(int limit){Page<ProductResponseDto> page=productRepository.findAllProductResponses(PageRequest.of(0,limit,Sort.by(Sort.Direction.DESC,"createdAt")));return page.getContent();}

    @Transactional
    public ProductImageResponse uploadProductImage(Long productId, MultipartFile file, ProductImageUploadRequest request) {
        Product product = findProductWithCategory(productId);
        StorageUploadResponse upload = storageService.upload(awsS3Properties.getProductImagePrefix(), file);
        if (request.isPrimary()) { productImageRepository.clearPrimaryForProduct(productId); }
        ProductImage saved = productImageRepository.save(ProductImage.builder().product(product).objectKey(upload.getKey()).imageUrl(upload.getFileUrl())
                .contentType(file.getContentType()).fileSizeBytes(file.getSize()).active(true).imageType(request.getImageType())
                .altText(request.getAltText()).displayOrder(request.getDisplayOrder()).primary(request.isPrimary()).build());
        if (saved.isPrimary() || product.getImageUrl() == null) { product.setImageUrl(saved.getImageUrl()); }
        return toResponse(saved, "Product image uploaded successfully");
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImages(Long productId) { findProductWithCategory(productId); return activeImages(productId).stream().map(i->toResponse(i,"Images fetched")).toList(); }

    @Transactional
    public ProductImageResponse setPrimaryImage(Long productId, Long imageId) {
        findProductWithCategory(productId); ProductImage image = productImageRepository.findByIdAndProductIdAndActiveTrue(imageId, productId).orElseThrow(() -> new ResourceNotFoundException("Image not found for product"));
        productImageRepository.clearPrimaryForProduct(productId); image.setPrimary(true); findProductWithCategory(productId).setImageUrl(image.getImageUrl());
        return toResponse(productImageRepository.save(image), "Primary image updated");
    }

    @Transactional public void reorderImages(Long productId, ProductImageReorderRequest request){findProductWithCategory(productId);for (var item:request.getItems()){ProductImage img=productImageRepository.findByIdAndProductIdAndActiveTrue(item.getImageId(),productId).orElseThrow(()->new ResourceNotFoundException("Image not found for product"));img.setDisplayOrder(item.getDisplayOrder());productImageRepository.save(img);}}

    @Transactional public void deleteProductImage(Long productId, Long imageId){ProductImage image=productImageRepository.findByIdAndProductIdAndActiveTrue(imageId,productId).orElseThrow(()->new ResourceNotFoundException("Image not found for product"));storageService.deleteByUrl(image.getImageUrl());image.setActive(false);image.setDeletedReason("DELETED");image.setPrimary(false);productImageRepository.save(image);}

    @Transactional(readOnly = true) public PresignedUploadUrlResponse generatePresignedUploadUrl(String fileName, String contentType){return storageService.generatePresignedUploadUrl(awsS3Properties.getProductImagePrefix(),fileName,contentType,Duration.ofSeconds(awsS3Properties.getPresignedUrlExpirationSeconds()));}
    @Transactional(readOnly = true) public List<ProductResponseDto> listProductsWithImages(){return productRepository.findProductsWithImages();}

    private Product findProductWithCategory(Long id){return productRepository.findByIdWithCategory(id).orElseThrow(() -> new ResourceNotFoundException("Product not found for id: " + id));}
    private Category findCategory(Long id){return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found for id: " + id));}
    private boolean hasFile(MultipartFile file){return file != null && !file.isEmpty();}
    private ProductImageUploadRequest defaultRequest(){ProductImageUploadRequest r=new ProductImageUploadRequest();r.setImageType(ImageType.OTHER);r.setDisplayOrder(0);r.setPrimary(true);return r;}
    private List<ProductImage> activeImages(Long productId){return productImageRepository.findByProductIdAndActiveTrueOrderByDisplayOrderAscCreatedAtAsc(productId);}    
    private ProductImageResponse toResponse(ProductImage i,String message){return ProductImageResponse.builder().imageId(i.getId()).productId(i.getProduct().getId()).imageUrl(i.getImageUrl()).imageType(i.getImageType()).altText(i.getAltText()).displayOrder(i.getDisplayOrder()).primary(i.isPrimary()).message(message).build();}
}
