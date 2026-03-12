package com.leathric.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Externalized AWS S3 configuration loaded from application properties.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.aws.s3")
public class AwsS3Properties {

    @NotBlank
    private String accessKey;

    @NotBlank
    private String secretKey;

    @NotBlank
    private String region;

    @NotBlank
    private String bucket;

    @NotBlank
    private String productImagePrefix = "products";

    @Min(60)
    private long presignedUrlExpirationSeconds = 900;

    @Min(1)
    private long maxFileSizeBytes = 5_242_880;
}
