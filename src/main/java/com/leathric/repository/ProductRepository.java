package com.leathric.repository;

import com.leathric.dto.ProductResponseDto;
import com.leathric.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    @Query(
            value = "SELECT new com.leathric.dto.ProductResponseDto(p.id, p.name, p.price, c.name) " +
                    "FROM Product p LEFT JOIN p.category c",
            countQuery = "SELECT COUNT(p) FROM Product p"
    )
    Page<ProductResponseDto> findAllProductResponses(Pageable pageable);
}
