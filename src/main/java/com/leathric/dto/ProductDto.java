package com.leathric.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;

    @NotBlank
    private String name;
    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    private String imageUrl;

    @NotNull
    @PositiveOrZero
    private Integer stockQuantity;

    @NotNull
    private Long categoryId;
    private String categoryName;
}
