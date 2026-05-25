package com.ecommerce.pricing.domain.exception;

public class CouponExhaustedException extends RuntimeException {

    public CouponExhaustedException(String couponCode) {
        super("Coupon usage limit reached: " + couponCode);
    }
}
