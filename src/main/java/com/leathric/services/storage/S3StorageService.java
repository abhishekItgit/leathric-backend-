package com.leathric.services.storage;

import com.leathric.config.AwsS3Properties;
import com.leathric.dto.response.PresignedUploadUrlResponse;
import com.leathric.dto.response.StorageUploadResponse;
import com.leathric.exception.BadRequestException;
import com.leathric.exception.StorageOperationException;
import com.leathric.interfaces.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * S3 backed implementation of {@link StorageService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService implements StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsS3Properties properties;

    /**
     * Uploads a product image file to S3.
     */
    @Override
    public StorageUploadResponse upload(String directory, MultipartFile file) {
        validateFile(file);
        String key = buildObjectKey(directory, file.getOriginalFilename());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return StorageUploadResponse.builder()
                    .key(key)
                    .fileUrl(getFileUrl(key))
                    .build();
        } catch (IOException e) {
            throw new StorageOperationException("Unable to read uploaded file bytes", e);
        } catch (S3Exception e) {
            log.error("S3 upload failed for key={} status={} message={}", key, e.statusCode(), e.getMessage());
            throw new StorageOperationException("Unable to upload file to storage", e);
        }
    }

    /**
     * Deletes an S3 object by public URL.
     */
    @Override
    public void deleteByUrl(String fileUrl) {
        String key = extractObjectKey(fileUrl);
        if (key == null || key.isBlank()) {
            return;
        }

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            log.error("S3 delete failed for key={} status={} message={}", key, e.statusCode(), e.getMessage());
            throw new StorageOperationException("Unable to delete file from storage", e);
        }
    }

    /**
     * Generates a pre-signed URL for browser/mobile direct upload.
     */
    @Override
    public PresignedUploadUrlResponse generatePresignedUploadUrl(
            String directory,
            String originalFileName,
            String contentType,
            Duration expiration
    ) {
        validateFileNameAndContentType(originalFileName, contentType);
        String key = buildObjectKey(directory, originalFileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        return PresignedUploadUrlResponse.builder()
                .objectKey(key)
                .uploadUrl(presigned.url().toString())
                .fileUrl(getFileUrl(key))
                .build();
    }

    /**
     * Returns the public URL for an object key.
     */
    public String getFileUrl(String key) {
        URL url = s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .build());
        return url.toString();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new BadRequestException("File exceeds configured max size");
        }
        validateFileNameAndContentType(file.getOriginalFilename(), file.getContentType());
    }

    private void validateFileNameAndContentType(String originalFileName, String contentType) {
        String extension = getExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only JPG, PNG and WEBP images are allowed");
        }
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Invalid image content type");
        }
    }

    private String buildObjectKey(String directory, String originalFileName) {
        String sanitizedName = originalFileName == null ? "image" : originalFileName.replaceAll("\\s+", "-").toLowerCase(Locale.ROOT);
        return "%s/%s-%s".formatted(directory, UUID.randomUUID(), sanitizedName);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String extractObjectKey(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        String marker = "/" + properties.getBucket() + "/";
        int markerIndex = fileUrl.indexOf(marker);
        if (markerIndex > -1) {
            return fileUrl.substring(markerIndex + marker.length());
        }

        String[] splitByDomain = fileUrl.split("\\.amazonaws\\.com/");
        if (splitByDomain.length == 2) {
            return splitByDomain[1];
        }

        throw new BadRequestException("Unsupported storage URL format");
    }
}
