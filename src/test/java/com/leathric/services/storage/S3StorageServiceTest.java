package com.leathric.services.storage;

import com.leathric.config.AwsS3Properties;
import com.leathric.dto.response.PresignedUploadUrlResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;
    @Mock
    private AwsS3Properties properties;

    @Spy
    @InjectMocks
    private S3StorageService s3StorageService;

    @Test
    void generatePresignedUploadUrl_shouldReturnResponseObject() throws Exception {
        when(properties.getBucket()).thenReturn("bucket");
        doReturn("https://bucket.s3.amazonaws.com/products/mock.jpg").when(s3StorageService).getFileUrl(any());

        PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
        when(presigned.url()).thenReturn(URI.create("https://presigned.example.com/upload").toURL());
        when(s3Presigner.presignPutObject(any())).thenReturn(presigned);

        PresignedUploadUrlResponse response = s3StorageService.generatePresignedUploadUrl(
                "products",
                "mock.jpg",
                "image/jpeg",
                Duration.ofMinutes(10)
        );

        assertThat(response.getUploadUrl()).isEqualTo("https://presigned.example.com/upload");
        assertThat(response.getFileUrl()).contains("products/");
    }
}
