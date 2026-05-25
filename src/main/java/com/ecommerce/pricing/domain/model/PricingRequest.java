package com.ecommerce.pricing.domain.model;

import java.util.List;
import java.util.Objects;

public final class PricingRequest {

    private final CustomerType customerType;
    private final List<OrderItem> items;
    private final String couponCode;

    public PricingRequest(CustomerType customerType, List<OrderItem> items, String couponCode) {
        this.customerType = customerType;
        this.items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (this.items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        this.couponCode = couponCode;
    }

    public CustomerType customerType() {
        return customerType;
    }

    public List<OrderItem> items() {
        return items;
    }

    public String couponCode() {
        return couponCode;
    }
}
