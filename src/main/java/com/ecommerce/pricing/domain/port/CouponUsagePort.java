package com.ecommerce.pricing.domain.port;

/**
 * Atomically reserves one coupon use under concurrent access.
 * Implementations must use database or distributed locking (not JVM-only synchronized).
 */
public interface CouponUsagePort {

    /**
     * Locks the coupon row, verifies availability, and increments {@code used_count}.
     * Must run inside a transaction; rolls back on failure.
     */
    void reserve(String couponCode);
}
