package com.leathric.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload to create a pre-signed upload URL.
 */
@Getter
@Setter
public class PresignedUploadUrlRequest {

    @NotBlank
    private String fileName;

    @NotBlank
    private String contentType;
}
