# Wishlist Module - Documentation

## Overview
Complete wishlist functionality for authenticated users to save products for later purchase.

## Features
- ✅ Add products to wishlist
- ✅ Remove products from wishlist
- ✅ View wishlist with product details
- ✅ Clear entire wishlist
- ✅ Check if product is in wishlist
- ✅ Automatic wishlist creation per user
- ✅ Duplicate prevention (UNIQUE constraint)
- ✅ Stock status tracking

## Database Schema

```sql
-- Wishlists table
CREATE TABLE wishlists (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Wishlist items table
CREATE TABLE wishlist_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wishlist_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (wishlist_id) REFERENCES wishlists(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY uk_wishlist_product (wishlist_id, product_id)
);
```

## Entity Relationships

```
User (1) ←→ (1) Wishlist
Wishlist (1) ←→ (M) WishlistItem
WishlistItem (M) → (1) Product
```

## API Endpoints

### 1. Get Wishlist
```http
GET /api/wishlist
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Wishlist retrieved successfully",
  "data": {
    "wishlistId": 1,
    "itemCount": 3,
    "items": [
      {
        "wishlistItemId": 1,
        "productId": 45,
        "productName": "Leather Wallet",
        "productDescription": "Premium leather wallet",
        "price": 149.99,
        "imageUrl": "/uploads/products/wallet.png",
        "stockQuantity": 10,
        "categoryName": "Accessories",
        "inStock": true
      }
    ]
  }
}
```

### 2. Add to Wishlist
```http
POST /api/wishlist/items
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 45
}
```

**Response:**
```json
{
  "success": true,
  "message": "Product added to wishlist",
  "data": {
    "wishlistId": 1,
    "itemCount": 4,
    "items": [...]
  }
}
```

**Error Cases:**
- Product not found: 404
- Product already in wishlist: 400

### 3. Remove from Wishlist
```http
DELETE /api/wishlist/items/{productId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Product removed from wishlist",
  "data": {
    "wishlistId": 1,
    "itemCount": 2,
    "items": [...]
  }
}
```

### 4. Clear Wishlist
```http
DELETE /api/wishlist
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Wishlist cleared successfully"
}
```

### 5. Check if Product in Wishlist
```http
GET /api/wishlist/check/{productId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Wishlist check completed",
  "data": true
}
```

## Implementation Details

### Auto-Creation
Wishlist is automatically created when:
- User first accesses their wishlist
- User adds first product to wishlist

### Duplicate Prevention
- UNIQUE constraint on (wishlist_id, product_id)
- Application-level check before adding
- Returns 400 error if product already exists

### Transaction Management
- Read operations: `@Transactional(readOnly = true)`
- Write operations: `@Transactional`
- Cascade ALL for wishlist items

### Query Optimization
```java
@EntityGraph(attributePaths = {"items", "items.product", "items.product.category"})
Optional<Wishlist> findByUserIdWithItems(Long userId);
```
- Single query fetches wishlist + items + products + categories
- Avoids N+1 query problem

## Usage Examples

### Frontend Integration

```javascript
// Get wishlist
const getWishlist = async () => {
  const response = await fetch('http://localhost:8080/api/wishlist', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};

// Add to wishlist
const addToWishlist = async (productId) => {
  const response = await fetch('http://localhost:8080/api/wishlist/items', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ productId })
  });
  return response.json();
};

// Remove from wishlist
const removeFromWishlist = async (productId) => {
  const response = await fetch(`http://localhost:8080/api/wishlist/items/${productId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};

// Check if in wishlist
const isInWishlist = async (productId) => {
  const response = await fetch(`http://localhost:8080/api/wishlist/check/${productId}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const result = await response.json();
  return result.data; // true or false
};
```

## Testing

### Manual Testing with cURL

```bash
# Get wishlist
curl -X GET http://localhost:8080/api/wishlist \
  -H "Authorization: Bearer YOUR_TOKEN"

# Add to wishlist
curl -X POST http://localhost:8080/api/wishlist/items \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1}'

# Remove from wishlist
curl -X DELETE http://localhost:8080/api/wishlist/items/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# Clear wishlist
curl -X DELETE http://localhost:8080/api/wishlist \
  -H "Authorization: Bearer YOUR_TOKEN"

# Check if in wishlist
curl -X GET http://localhost:8080/api/wishlist/check/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Security

- All endpoints require authentication
- Users can only access their own wishlist
- Wishlist ownership validated via JWT token
- No admin-specific endpoints (user-only feature)

## Files Created

```
src/main/java/com/leathric/
├── entity/
│   ├── Wishlist.java              ✨ NEW
│   └── WishlistItem.java          ✨ NEW
├── repository/
│   └── WishlistRepository.java    ✨ NEW
├── service/
│   ├── WishlistService.java       ✨ NEW
│   └── impl/
│       └── WishlistServiceImpl.java ✨ NEW
├── controller/
│   └── WishlistController.java    ✨ NEW
└── dto/
    └── WishlistDtos.java          ✨ NEW
```

## Future Enhancements

- [ ] Wishlist sharing (share with friends)
- [ ] Price drop notifications
- [ ] Back-in-stock notifications
- [ ] Move to cart (bulk add to cart)
- [ ] Wishlist analytics (most wishlisted products)
- [ ] Public wishlists (gift registry)
- [ ] Wishlist notes (personal notes per item)

---

**Status:** ✅ Production Ready
**Last Updated:** 2024-01-20
