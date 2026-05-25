package com.ecommerce.pricing.domain.exception;

public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(String couponCode) {
        super("Coupon not found or inactive: " + couponCode);
    }
}
