package com.leathric.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * API response for pre-signed URL generation.
 */
@Getter
@Builder
public class PresignedUploadUrlResponse {
    private String objectKey;
    private String uploadUrl;
    private String fileUrl;
}
