package com.leathric.storage;

import com.leathric.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final String BASE_URL = "http://localhost:8080";
    private final Path productUploadPath = Paths.get("uploads", "products");

    @PostConstruct
    void init() {
        try {
            // Ensure upload directory exists before first upload call.
            Files.createDirectories(productUploadPath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize product uploads directory", e);
        }
    }

    @Override
    public String storeProductImage(MultipartFile file) {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;
        Path target = productUploadPath.resolve(fileName).normalize();

        if (!target.startsWith(productUploadPath)) {
            throw new BadRequestException("Invalid file path");
        }

        try {
            // Persist the file to local filesystem storage instead of DB blob columns.
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String imageUrl = BASE_URL + "/uploads/products/" + fileName;
            log.info("Product image uploaded successfully: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            throw new IllegalStateException("Could not store file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        String contentType = file.getContentType();
        String extension = getExtension(file.getOriginalFilename());

        // Validate both MIME type and extension for safer file filtering.
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))
                || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only JPG and PNG images are allowed");
        }
    }

    private String getExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            throw new BadRequestException("File must have a valid extension");
        }

        return originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
