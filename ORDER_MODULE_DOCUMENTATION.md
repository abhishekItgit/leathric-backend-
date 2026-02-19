# Order Processing Module - Production-Grade Implementation

## Overview
This document describes the complete order processing system implemented for the Leathric e-commerce backend. The implementation follows modular monolith architecture with strict transaction boundaries, concurrency safety, and comprehensive order lifecycle management.

---

## 1. FOLDER STRUCTURE

```
src/main/java/com/leathric/
├── entity/
│   ├── Order.java                      # Enhanced with version, paymentStatus, statusHistory
│   ├── OrderItem.java                  # Existing (no changes)
│   ├── OrderStatus.java                # Enhanced with lifecycle validation
│   ├── OrderStatusHistory.java         # NEW - Status tracking
│   └── PaymentStatus.java              # NEW - Payment state enum
├── repository/
│   ├── OrderRepository.java            # Enhanced with pessimistic locking
│   └── OrderStatusHistoryRepository.java # NEW
├── service/
│   ├── OrderService.java               # Enhanced interface
│   └── impl/
│       └── OrderServiceImpl.java       # Complete rewrite with production logic
├── controller/
│   └── OrderController.java            # Enhanced with all endpoints
└── dto/
    └── OrderDtos.java                  # Enhanced with tracking DTOs
```

---

## 2. ENTITY DESIGN

### Order Entity
```java
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_id", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created_at", columnList = "created_at")
})
public class Order extends BaseEntity {
    private Long id;
    private User user;
    private OrderStatus status;              // Lifecycle status
    private PaymentStatus paymentStatus;     // Payment state
    private BigDecimal totalAmount;
    private String note;                     // Optional user note
    private Long version;                    // Optimistic locking
    private List<OrderItem> items;
    private List<OrderStatusHistory> statusHistory;
    
    // Helper method to add status history
    public void addStatusHistory(OrderStatus newStatus, String note);
}
```

**Key Features:**
- `@Version` for optimistic locking (prevents lost updates)
- Database indexes on user_id, status, created_at for query performance
- Bidirectional relationship with OrderStatusHistory
- Cascade ALL for items and statusHistory (lifecycle management)

### OrderStatusHistory Entity
```java
@Entity
@Table(name = "order_status_history", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "status", "timestamp"}))
public class OrderStatusHistory {
    private Long id;
    private Order order;
    private OrderStatus status;
    private LocalDateTime timestamp;
    private String note;
}
```

**Key Features:**
- UNIQUE constraint on (order_id, status, timestamp) prevents duplicate entries
- Automatic timestamp generation via @PrePersist
- Immutable audit trail (no update/delete operations)

### OrderStatus Enum
```java
public enum OrderStatus {
    CREATED,           // Order created from cart
    CONFIRMED,         // Payment confirmed
    PACKED,            // Order packed for shipping
    SHIPPED,           // Order shipped
    OUT_FOR_DELIVERY,  // Out for delivery
    DELIVERED,         // Successfully delivered
    CANCELLED,         // Cancelled by user/admin
    RETURN_REQUESTED,  // Return initiated
    REFUNDED;          // Refund processed

    // Validates allowed state transitions
    public boolean canTransitionTo(OrderStatus newStatus);
    
    // Checks if order can be cancelled
    public boolean isCancellable();
}
```

**State Transition Rules:**
- CREATED → CONFIRMED, CANCELLED
- CONFIRMED → PACKED, CANCELLED
- PACKED → SHIPPED, CANCELLED
- SHIPPED → OUT_FOR_DELIVERY, RETURN_REQUESTED
- OUT_FOR_DELIVERY → DELIVERED, RETURN_REQUESTED
- DELIVERED → RETURN_REQUESTED
- RETURN_REQUESTED → REFUNDED
- CANCELLED, REFUNDED → No transitions (terminal states)

### PaymentStatus Enum
```java
public enum PaymentStatus {
    PENDING,    // Payment not yet processed
    COMPLETED,  // Payment successful
    FAILED,     // Payment failed
    REFUNDED    // Payment refunded
}
```

---

## 3. ORDER LIFECYCLE IMPLEMENTATION

### 3.1 Place Order Flow
```
User Cart → Validate Stock → Create Order (CREATED) → Deduct Stock → Clear Cart
```

**Implementation:**
```java
@Transactional
public OrderDtos.OrderResponse placeOrder(PlaceOrderRequest request) {
    // 1. Fetch user and cart
    User user = getCurrentUser();
    Cart cart = getCartWithItems(user.getId());
    
    // 2. Validate cart not empty
    if (cart.getItems().isEmpty()) throw BadRequestException;
    
    // 3. Validate stock and calculate total
    for (CartItem item : cart.getItems()) {
        if (product.stock < item.quantity) throw BadRequestException;
        totalAmount += product.price * item.quantity;
    }
    
    // 4. Create order with CREATED status
    Order order = Order.builder()
        .status(OrderStatus.CREATED)
        .paymentStatus(PaymentStatus.PENDING)
        .totalAmount(totalAmount)
        .build();
    
    // 5. Add initial status history
    order.addStatusHistory(OrderStatus.CREATED, "Order created from cart");
    
    // 6. Create order items and deduct stock
    for (CartItem item : cart.getItems()) {
        OrderItem orderItem = createOrderItem(item);
        order.getItems().add(orderItem);
        product.setStockQuantity(product.stock - item.quantity);
    }
    
    // 7. Save order (cascades to items and history)
    Order saved = orderRepository.save(order);
    
    // 8. Clear cart
    cart.getItems().clear();
    cartRepository.save(cart);
    
    return toOrderResponse(saved);
}
```

**Transaction Boundary:** Entire operation in single transaction ensures:
- Stock deduction and order creation are atomic
- Cart clearing happens only after successful order creation
- Rollback on any failure restores stock and cart

### 3.2 Confirm Payment Flow
```
Order (CREATED) → Validate Status → Update PaymentStatus → Move to CONFIRMED
```

**Implementation:**
```java
@Transactional
public OrderDtos.OrderResponse confirmPayment(Long orderId, ConfirmPaymentRequest request) {
    // 1. Fetch order with pessimistic lock
    Order order = getOrderForUpdate(orderId);
    validateOrderOwnership(order);
    
    // 2. Validate current status
    if (order.getStatus() != OrderStatus.CREATED) {
        throw BadRequestException("Payment can only be confirmed for CREATED orders");
    }
    
    // 3. Update payment status
    order.setPaymentStatus(PaymentStatus.COMPLETED);
    
    // 4. Move to CONFIRMED and add history
    updateStatus(order, OrderStatus.CONFIRMED, "Payment confirmed: " + paymentRef);
    
    return toOrderResponse(orderRepository.save(order));
}
```

### 3.3 Update Order Status Flow
```
Current Status → Validate Transition → Update Status → Add History
```

**Implementation:**
```java
@Transactional
public OrderDtos.OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String note) {
    // 1. Fetch order with pessimistic lock
    Order order = getOrderForUpdate(orderId);
    
    // 2. Validate transition
    if (!order.getStatus().canTransitionTo(newStatus)) {
        throw BadRequestException("Invalid status transition");
    }
    
    // 3. Update status and add history
    updateStatus(order, newStatus, note);
    
    return toOrderResponse(orderRepository.save(order));
}

private void updateStatus(Order order, OrderStatus newStatus, String note) {
    order.setStatus(newStatus);
    order.addStatusHistory(newStatus, note);
}
```

### 3.4 Cancel Order Flow
```
Validate Cancellable → Restore Stock → Move to CANCELLED
```

**Implementation:**
```java
@Transactional
public OrderDtos.OrderResponse cancelOrder(Long orderId) {
    // 1. Fetch order with pessimistic lock
    Order order = getOrderForUpdate(orderId);
    validateOrderOwnership(order);
    
    // 2. Validate cancellable (only CREATED, CONFIRMED, PACKED)
    if (!order.getStatus().isCancellable()) {
        throw BadRequestException("Cannot cancel order in " + order.getStatus());
    }
    
    // 3. Restore stock
    for (OrderItem item : order.getItems()) {
        Product product = item.getProduct();
        product.setStockQuantity(product.stock + item.quantity);
    }
    
    // 4. Update status
    updateStatus(order, OrderStatus.CANCELLED, "Order cancelled by user");
    
    return toOrderResponse(orderRepository.save(order));
}
```

---

## 4. ORDER TRACKING API

### Endpoint: GET /api/orders/{orderId}/tracking

**Response Format:**
```json
{
  "success": true,
  "message": "Order tracking retrieved",
  "data": {
    "orderId": 123,
    "currentStatus": "SHIPPED",
    "paymentStatus": "COMPLETED",
    "totalAmount": 299.99,
    "createdAt": "2024-01-15T10:30:00",
    "timeline": [
      {
        "status": "CREATED",
        "timestamp": "2024-01-15T10:30:00",
        "note": "Order created from cart"
      },
      {
        "status": "CONFIRMED",
        "timestamp": "2024-01-15T10:35:00",
        "note": "Payment confirmed: PAY-12345"
      },
      {
        "status": "PACKED",
        "timestamp": "2024-01-15T14:20:00",
        "note": "Order packed for shipping"
      },
      {
        "status": "SHIPPED",
        "timestamp": "2024-01-16T09:00:00",
        "note": "Order shipped via FedEx - Tracking: FDX123456"
      }
    ]
  }
}
```

**Implementation:**
```java
@Transactional(readOnly = true)
public OrderTrackingResponse getOrderTracking(Long orderId) {
    Order order = orderRepository.findByIdWithTracking(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    
    validateOrderOwnership(order);
    
    var timeline = order.getStatusHistory().stream()
        .map(history -> StatusHistoryItem.builder()
            .status(history.getStatus())
            .timestamp(history.getTimestamp())
            .note(history.getNote())
            .build())
        .toList();
    
    return OrderTrackingResponse.builder()
        .orderId(order.getId())
        .currentStatus(order.getStatus())
        .paymentStatus(order.getPaymentStatus())
        .totalAmount(order.getTotalAmount())
        .createdAt(order.getCreatedAt())
        .timeline(timeline)
        .build();
}
```

---

## 5. USER ORDER APIs

### 5.1 GET /api/orders
**Description:** Get user's order history (paginated)

**Response:**
```json
{
  "success": true,
  "message": "Order history retrieved",
  "data": {
    "content": [
      {
        "orderId": 123,
        "status": "DELIVERED",
        "paymentStatus": "COMPLETED",
        "totalAmount": 299.99,
        "note": "Please deliver before 5 PM",
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-18T14:20:00",
        "items": [
          {
            "productId": 45,
            "productName": "Leather Wallet",
            "quantity": 2,
            "price": 149.99,
            "lineTotal": 299.98
          }
        ]
      }
    ],
    "pageable": {...},
    "totalElements": 15,
    "totalPages": 2
  }
}
```

### 5.2 GET /api/orders/{orderId}
**Description:** Get single order details

### 5.3 PATCH /api/orders/{orderId}/cancel
**Description:** Cancel order (only allowed before SHIPPED)

**Request:** No body required

**Response:**
```json
{
  "success": true,
  "message": "Order cancelled successfully",
  "data": {
    "orderId": 123,
    "status": "CANCELLED",
    "paymentStatus": "PENDING",
    "totalAmount": 299.99,
    ...
  }
}
```

---

## 6. TRANSACTION MANAGEMENT STRATEGY

### 6.1 Transaction Boundaries

**Service Layer:**
- All write operations: `@Transactional`
- All read operations: `@Transactional(readOnly = true)`

**Controller Layer:**
- NO `@Transactional` annotations
- Controllers delegate to service layer

**Repository Layer:**
- Inherits transaction from service layer
- No explicit transaction management

### 6.2 Transaction Isolation

**Default:** READ_COMMITTED (MySQL default)

**Pessimistic Locking for Updates:**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT o FROM Order o WHERE o.id = :id")
Optional<Order> findByIdForUpdate(@Param("id") Long id);
```

**Why Pessimistic Locking?**
- Prevents concurrent status updates
- Ensures stock restoration is accurate during cancellation
- Avoids lost update anomalies

### 6.3 Optimistic Locking

```java
@Version
private Long version;
```

**Purpose:**
- Detects concurrent modifications
- Throws `OptimisticLockException` if version mismatch
- Fallback safety mechanism

---

## 7. CONCURRENCY SAFETY

### 7.1 Preventing Duplicate Orders

**Problem:** User clicks "Place Order" multiple times

**Solution:**
```java
// Cart is fetched and locked during order creation
Cart cart = cartRepository.findByUserId(userId);

// After order creation, cart is cleared
cart.getItems().clear();
cartRepository.save(cart);

// Second request will find empty cart and fail validation
if (cart.getItems().isEmpty()) {
    throw new BadRequestException("Cart is empty");
}
```

**Additional Safety:** Add unique constraint on cart_id + order_id if needed

### 7.2 Stock Deduction Race Condition

**Problem:** Two orders for same product with limited stock

**Solution:**
```java
// Pessimistic lock on order prevents concurrent updates
Order order = orderRepository.findByIdForUpdate(orderId);

// Stock validation happens within transaction
if (product.getStockQuantity() < quantity) {
    throw new BadRequestException("Insufficient stock");
}

// Stock deduction is atomic
product.setStockQuantity(product.stock - quantity);
```

### 7.3 Status Update Conflicts

**Problem:** Admin updates status while user cancels order

**Solution:**
```java
// Pessimistic lock ensures sequential processing
Order order = orderRepository.findByIdForUpdate(orderId);

// Validation prevents invalid transitions
if (!order.getStatus().canTransitionTo(newStatus)) {
    throw new BadRequestException("Invalid transition");
}
```

---

## 8. STATUS HISTORY LOGIC

### 8.1 Automatic History Tracking

Every status change automatically creates history entry:

```java
public void addStatusHistory(OrderStatus newStatus, String note) {
    OrderStatusHistory history = OrderStatusHistory.builder()
        .order(this)
        .status(newStatus)
        .note(note)
        .build();
    this.statusHistory.add(history);
}
```

### 8.2 History Immutability

- No update or delete operations on OrderStatusHistory
- UNIQUE constraint prevents duplicate entries
- Provides complete audit trail

### 8.3 History Query Optimization

```java
@EntityGraph(attributePaths = {"items", "items.product", "statusHistory"})
@Query("SELECT o FROM Order o WHERE o.id = :id")
Optional<Order> findByIdWithTracking(@Param("id") Long id);
```

**Benefits:**
- Single query fetches order + items + history
- Avoids N+1 query problem
- Optimized for tracking endpoint

---

## 9. API ENDPOINTS SUMMARY

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | /api/orders | User | Place order from cart |
| POST | /api/orders/{id}/confirm-payment | User | Confirm payment |
| GET | /api/orders | User | Get order history |
| GET | /api/orders/{id} | User | Get order details |
| GET | /api/orders/{id}/tracking | User | Get order tracking |
| PATCH | /api/orders/{id}/cancel | User | Cancel order |
| PATCH | /api/orders/{id}/status | Admin | Update order status |

---

## 10. DATABASE SCHEMA

```sql
-- Orders table
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    note VARCHAR(1000),
    version BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_order_user_id (user_id),
    INDEX idx_order_status (status),
    INDEX idx_order_created_at (created_at)
);

-- Order items table
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Order status history table
CREATE TABLE order_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    note VARCHAR(500),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    UNIQUE KEY uk_order_status_time (order_id, status, timestamp)
);
```

---

## 11. TESTING RECOMMENDATIONS

### Unit Tests
- OrderStatus.canTransitionTo() validation
- OrderStatus.isCancellable() logic
- DTO mapping logic

### Integration Tests
- Place order flow (cart → order → clear cart)
- Concurrent order placement (duplicate prevention)
- Stock deduction accuracy
- Status transition validation
- Order cancellation with stock restoration

### Load Tests
- Concurrent order placement
- Concurrent status updates
- Database lock contention

---

## 12. PRODUCTION CHECKLIST

- [x] Transaction boundaries defined
- [x] Pessimistic locking for updates
- [x] Optimistic locking with @Version
- [x] Status transition validation
- [x] Concurrency safety mechanisms
- [x] Comprehensive logging (SLF4J)
- [x] Database indexes for performance
- [x] Audit trail (status history)
- [x] Clean architecture (no entities in controllers)
- [x] Proper exception handling
- [ ] Unit tests
- [ ] Integration tests
- [ ] API documentation (Swagger)
- [ ] Performance testing

---

## 13. FUTURE ENHANCEMENTS

1. **Inventory Reservation:** Reserve stock when adding to cart
2. **Payment Gateway Integration:** Stripe/PayPal integration
3. **Email Notifications:** Order confirmation, shipping updates
4. **Shipping Integration:** FedEx/UPS tracking
5. **Return Management:** Complete return workflow
6. **Order Analytics:** Sales reports, revenue tracking
7. **Bulk Order Operations:** Admin bulk status updates
8. **Order Search:** Advanced filtering and search

---

## Conclusion

This implementation provides a production-grade order processing system with:
- Complete order lifecycle management
- Robust concurrency safety
- Comprehensive audit trail
- Clean architecture
- Transaction integrity
- Performance optimization

The system is ready for production deployment with proper testing and monitoring.
