package com.leathric.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final S3StorageService s3StorageService;

    @Override
    public String storeProductImage(MultipartFile file) {
        return s3StorageService.uploadFile(file);
    }
}
