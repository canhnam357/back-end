package com.bookstore.Constant;

public enum OrderStatus {
    PENDING, // USER
    REJECTED, // EMPLOYEE
    IN_PREPARATION, // EMPLOYEE
    CANCELLATION_REQUESTED,
    READY_TO_SHIP, // EMPLOYEE
    DELIVERING, // SHIPPER
    DELIVERED, // SHIPPER
    CANCELLED // USER
}
