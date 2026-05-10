package com.leathric.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductImageReorderRequest {

    @NotNull
    private List<ImageOrder> items;

    @Getter
    @Setter
    public static class ImageOrder {
        @NotNull
        private Long imageId;

        @NotNull
        @Min(0)
        private Integer displayOrder;
    }
}
