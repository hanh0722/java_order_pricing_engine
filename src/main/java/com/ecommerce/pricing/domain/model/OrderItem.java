package com.ecommerce.pricing.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class OrderItem {

    private final String sku;
    private final BigDecimal unitPrice;
    private final int quantity;

    public OrderItem(String sku, BigDecimal unitPrice, int quantity) {
        this.sku = Objects.requireNonNull(sku, "sku must not be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("unitPrice must not be negative");
        }
        this.quantity = quantity;
    }

    public String sku() {
        return sku;
    }

    public BigDecimal unitPrice() {
        return unitPrice;
    }

    public int quantity() {
        return quantity;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return this.sku + " - " + this.unitPrice + " - " + this.quantity;
     }
}
