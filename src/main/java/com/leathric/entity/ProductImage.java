package com.leathric.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Persistent image record for product media lifecycle tracking.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_images")
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "object_key", nullable = false, length = 768)
    private String objectKey;

    @Column(name = "image_url", nullable = false, length = 1024)
    private String imageUrl;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "deleted_reason", length = 255)
    private String deletedReason;
}
