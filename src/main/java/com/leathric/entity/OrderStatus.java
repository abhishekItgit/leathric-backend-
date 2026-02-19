package com.leathric.entity;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    PACKED,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    RETURN_REQUESTED,
    REFUNDED;

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case CREATED -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == PACKED || newStatus == CANCELLED;
            case PACKED -> newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED -> newStatus == OUT_FOR_DELIVERY || newStatus == RETURN_REQUESTED;
            case OUT_FOR_DELIVERY -> newStatus == DELIVERED || newStatus == RETURN_REQUESTED;
            case DELIVERED -> newStatus == RETURN_REQUESTED;
            case RETURN_REQUESTED -> newStatus == REFUNDED;
            case CANCELLED, REFUNDED -> false;
        };
    }

    public boolean isCancellable() {
        return this == CREATED || this == CONFIRMED || this == PACKED;
    }
}
