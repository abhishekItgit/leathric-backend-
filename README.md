# Leathric Backend

Production-ready Spring Boot backend for Leathric leather ecommerce platform.

## Tech Stack
- Java 17
- Spring Boot 3
- Maven
- MySQL
- Spring Data JPA
- Spring Security + JWT
- Lombok
- Jakarta Validation

## Package Structure
```
com.leathric
 ├── config
 ├── controller
 ├── dto
 ├── entity
 ├── repository
 ├── service
 ├── security
 ├── exception
 └── util
```

## Entity Relationship Overview
- **User** ⟷ **Role**: many-to-many
- **User** ⟷ **Cart**: one-to-one
- **Cart** ⟷ **CartItem**: one-to-many
- **CartItem** ⟶ **Product**: many-to-one
- **Product** ⟶ **Category**: many-to-one
- **User** ⟷ **Order**: one-to-many
- **Order** ⟷ **OrderItem**: one-to-many
- **OrderItem** ⟶ **Product**: many-to-one
- **Review** connects **User** and **Product** with one review per user+product

## Security
- `/api/auth/register` and `/api/auth/login` are public.
- Product and category reads are public.
- Mutations on products/categories and order status update require `ADMIN`.
- Cart and order placement/history require authenticated users.
- JWT stateless authentication with BCrypt password hashing.

## Run locally
1. Create MySQL database or allow auto-create from URL.
2. Update credentials and JWT secret in `src/main/resources/application.yml`.
3. Run:
   ```bash
   mvn spring-boot:run
   ```

## APIs
### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`

### Product
- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products` (ADMIN)
- `PUT /api/products/{id}` (ADMIN)
- `DELETE /api/products/{id}` (ADMIN)

### Category
- `GET /api/categories`
- `GET /api/categories/{id}`
- `POST /api/categories` (ADMIN)
- `PUT /api/categories/{id}` (ADMIN)
- `DELETE /api/categories/{id}` (ADMIN)

### Cart
- `GET /api/cart`
- `POST /api/cart/items`
- `PUT /api/cart/items/{itemId}`
- `DELETE /api/cart/items/{itemId}`

### Order
- `POST /api/orders`
- `GET /api/orders`
- `PATCH /api/orders/{orderId}/status` (ADMIN)
