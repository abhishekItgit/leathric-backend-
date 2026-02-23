package com.leathric.storage;

import com.leathric.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${AWS_S3_BUCKET}")
    private String bucketName;

    @Value("${app.storage.max-file-size-bytes:5242880}")
    private long maxFileSizeBytes;

    public String uploadFile(MultipartFile file) {
        validateFile(file);

        String key = buildObjectKey(file.getOriginalFilename());
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            String fileUrl = getFileUrl(key);
            log.info("Uploaded product image to S3 with key={}", key);
            return fileUrl;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read uploaded file", e);
        } catch (S3Exception e) {
            log.error("Failed uploading file to S3. key={} statusCode={} message={}", key, e.statusCode(), e.awsErrorDetails().errorMessage());
            throw new IllegalStateException("Unable to upload image to storage");
        }
    }

    public void deleteFile(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
            log.info("Deleted S3 object with key={}", key);
        } catch (S3Exception e) {
            log.error("Failed deleting S3 object. key={} statusCode={} message={}", key, e.statusCode(), e.awsErrorDetails().errorMessage());
            throw new IllegalStateException("Unable to delete image from storage");
        }
    }

    public String getFileUrl(String key) {
        URL url = s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName).key(key).build());
        return url.toString();
    }

    public String generatePresignedUploadUrl(String originalFileName, String contentType, Duration expiration) {
        String extension = getExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only JPG, PNG and WEBP images are allowed");
        }

        String key = buildObjectKey(originalFileName);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new BadRequestException("Image exceeds max allowed size of " + maxFileSizeBytes + " bytes");
        }

        String contentType = file.getContentType();
        String extension = getExtension(file.getOriginalFilename());

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))
                || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only JPG, PNG and WEBP images are allowed");
        }
    }

    private String buildObjectKey(String originalFileName) {
        String extension = getExtension(originalFileName);
        return "products/" + UUID.randomUUID() + "." + extension;
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BadRequestException("File must have a valid extension");
        }

        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
