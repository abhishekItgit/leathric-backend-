CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    object_key VARCHAR(768) NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    image_type VARCHAR(40) NOT NULL,
    alt_text VARCHAR(255) NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_primary BIT(1) NOT NULL DEFAULT b'0',
    content_type VARCHAR(120) NULL,
    file_size_bytes BIGINT NULL,
    is_active BIT(1) NOT NULL DEFAULT b'1',
    deleted_reason VARCHAR(255) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_product_images_product_active_order
    ON product_images(product_id, is_active, display_order, created_at);

CREATE INDEX idx_product_images_product_primary
    ON product_images(product_id, is_primary, is_active);

INSERT INTO product_images(product_id, object_key, image_url, image_type, alt_text, display_order, is_primary, content_type, file_size_bytes, is_active, created_at, updated_at)
SELECT p.id,
       SUBSTRING_INDEX(p.image_url, '/', -1),
       p.image_url,
       'THUMBNAIL',
       p.name,
       0,
       b'1',
       NULL,
       NULL,
       b'1',
       NOW(6),
       NOW(6)
FROM products p
WHERE p.image_url IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id AND pi.is_primary = b'1' AND pi.is_active = b'1');
