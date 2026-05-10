package com.leathric.dto.request;

import com.leathric.entity.ImageType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageUploadRequest {

    @NotNull
    private ImageType imageType;

    @Size(max = 255)
    private String altText;

    @NotNull
    @Min(0)
    private Integer displayOrder;

    private boolean primary;
}
