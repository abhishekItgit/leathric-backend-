package com.leathric.controller;

import com.leathric.dto.ApiResponse;
import com.leathric.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        // Controller handles HTTP concerns only; storage logic is delegated to service layer.
        String imageUrl = fileStorageService.storeProductImage(file);

        return ApiResponse.<Map<String, String>>builder()
                .success(true)
                .message("Image uploaded successfully")
                .data(Map.of("imageUrl", imageUrl))
                .build();
    }
}
