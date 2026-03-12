package com.leathric.repository;

import com.leathric.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    Optional<ProductImage> findFirstByProductIdAndActiveTrueOrderByCreatedAtDesc(Long productId);

    List<ProductImage> findByProductIdOrderByCreatedAtDesc(Long productId);
}
