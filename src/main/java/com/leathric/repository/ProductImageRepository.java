package com.leathric.repository;

import com.leathric.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    Optional<ProductImage> findFirstByProductIdAndActiveTrueOrderByCreatedAtDesc(Long productId);

    List<ProductImage> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<ProductImage> findByProductIdAndActiveTrueOrderByDisplayOrderAscCreatedAtAsc(Long productId);

    Optional<ProductImage> findByIdAndProductIdAndActiveTrue(Long imageId, Long productId);

    @Modifying
    @Query("update ProductImage pi set pi.primary = false where pi.product.id = :productId and pi.active = true")
    void clearPrimaryForProduct(Long productId);
}
