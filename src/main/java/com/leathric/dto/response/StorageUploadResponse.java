package com.leathric.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents uploaded object metadata.
 */
@Getter
@Builder
public class StorageUploadResponse {
    private String key;
    private String fileUrl;
}
