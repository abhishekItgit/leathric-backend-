package com.leathric.interfaces;

import com.leathric.dto.response.StorageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

/**
 * Defines the contract for object storage providers.
 */
public interface StorageService {

    /**
     * Uploads a file into a logical directory.
     *
     * @param directory logical directory/prefix in storage
     * @param file file to upload
     * @return uploaded object metadata
     */
    StorageUploadResponse upload(String directory, MultipartFile file);

    /**
     * Deletes a stored object by URL.
     *
     * @param fileUrl public object URL
     */
    void deleteByUrl(String fileUrl);

    /**
     * Generates a pre-signed PUT URL for direct client upload.
     *
     * @param directory logical directory/prefix in storage
     * @param originalFileName original file name
     * @param contentType MIME content type
     * @param expiration URL validity duration
     * @return pre-signed upload URL and object URL
     */
    com.leathric.dto.response.PresignedUploadUrlResponse generatePresignedUploadUrl(
            String directory,
            String originalFileName,
            String contentType,
            Duration expiration
    );
}
