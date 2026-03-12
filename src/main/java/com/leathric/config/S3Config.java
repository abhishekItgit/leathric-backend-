package com.leathric.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Registers AWS S3 SDK clients using typed configuration properties.
 */
@Configuration
@EnableConfigurationProperties(AwsS3Properties.class)
public class S3Config {

    /**
     * Builds and exposes a synchronous S3 client.
     *
     * @param properties AWS S3 configuration values
     * @return configured {@link S3Client}
     */
    @Bean
    public S3Client s3Client(AwsS3Properties properties) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey());
        return S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    /**
     * Builds and exposes an S3 presigner used for pre-signed URL operations.
     *
     * @param properties AWS S3 configuration values
     * @return configured {@link S3Presigner}
     */
    @Bean
    public S3Presigner s3Presigner(AwsS3Properties properties) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey());
        return S3Presigner.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
