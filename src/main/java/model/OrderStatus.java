package model;

public enum OrderStatus{
    CREATED,
    CONFIRMED,
    CANCELLED,
    FAILED,
    ROLLBACK_FAILED,
    CANCELLATION_PENDING
}