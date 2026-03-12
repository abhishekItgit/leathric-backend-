# Leathric Backend

Production-grade Spring Boot backend for Leathric ecommerce, with cleanly separated API, service, storage strategy, and exception layers.

## Enhanced Architecture

```text
com.leathric
├── config
│   ├── AwsS3Properties.java
│   └── S3Config.java
├── controller
│   ├── ProductController.java
│   └── UploadController.java
├── controllers
│   └── ProductImageController.java
├── dto
│   ├── ProductDto.java
│   ├── ProductResponseDto.java
│   ├── request
│   │   └── PresignedUploadUrlRequest.java
│   └── response
│       ├── PresignedUploadUrlResponse.java
│       ├── ProductImageResponse.java
│       └── StorageUploadResponse.java
├── exception
│   ├── GlobalExceptionHandler.java
│   └── StorageOperationException.java
├── interfaces
│   └── StorageService.java
├── mapper
│   └── ProductMapper.java
├── repository
│   └── ProductRepository.java
├── service
│   ├── ProductService.java
│   └── impl
│       └── ProductServiceImpl.java
├── services
│   └── storage
│       └── S3StorageService.java
└── strategy
    └── StorageStrategy.java
```

## S3 Capabilities

The storage layer now supports both patterns:

1. **Direct upload/delete/get URL** using `S3Client`.
2. **Pre-signed upload URL generation** using `S3Presigner`.

## Configuration Properties

S3 values are externalized via typed properties (`@ConfigurationProperties`):

```yaml
app:
  aws:
    s3:
      access-key: ${AWS_ACCESS_KEY_ID:}
      secret-key: ${AWS_SECRET_ACCESS_KEY:}
      region: ${AWS_REGION:ap-south-1}
      bucket: ${AWS_S3_BUCKET:}
      product-image-prefix: ${AWS_S3_PRODUCT_PREFIX:products}
      presigned-url-expiration-seconds: ${AWS_S3_PRESIGNED_EXPIRATION_SECONDS:900}
      max-file-size-bytes: ${AWS_S3_MAX_FILE_SIZE_BYTES:5242880}
```

## Product Image APIs

### Upload image and save URL
- `POST /api/products/{productId}/image` (ADMIN)

### Get pre-signed upload URL
- `POST /api/products/images/presigned-upload-url` (ADMIN)

Request body:
```json
{
  "fileName": "shoe.jpg",
  "contentType": "image/jpeg"
}
```

### Update image
- `PUT /api/products/{productId}/image` (ADMIN)

### Delete image
- `DELETE /api/products/{productId}/image` (ADMIN)

### List products with images
- `GET /api/products/images`

### Get active image for a product
- `GET /api/products/{productId}/image`

### Get product image history (DB tracked)
- `GET /api/products/{productId}/images/history`

## Response Format

All endpoints return the common response structure:

```json
{
  "success": true,
  "message": "Product image uploaded",
  "data": {
    "productId": 1,
    "imageUrl": "https://...",
    "message": "Product image uploaded successfully"
  }
}
```

## Exception Handling

`GlobalExceptionHandler` now standardizes responses for:
- `ResourceNotFoundException`
- `BadRequestException`
- `StorageOperationException`
- Validation errors
- Generic exceptions

## Unit Tests (Mockito Samples)

Included examples:
- `ProductServiceImplTest` for service orchestration with storage + repository mocks.
- `S3StorageServiceTest` for pre-signed URL generation behavior.

Run tests:

```bash
mvn test
```


## Image Tracking in Main Database

Uploaded product images are now tracked in the `product_images` table in the primary MySQL database (`leathric-db`).
Each record stores product association, S3 object key, URL, content type, size, active flag, and deletion reason for audit/history use-cases.
