package com.ecommerce.pricing.domain.model;

public enum CustomerType {
    REGULAR,
    VIP;

    public static CustomerType from(String value) {
        if (value == null || value.isBlank()) {
            return REGULAR;
        }
        return CustomerType.valueOf(value.trim().toUpperCase());
    }
}
