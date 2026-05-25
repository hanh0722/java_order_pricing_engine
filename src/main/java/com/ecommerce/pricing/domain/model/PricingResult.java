package com.ecommerce.pricing.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PricingResult {

    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal finalPrice;
    private final List<AppliedPromotion> appliedPromotions;

    public PricingResult(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal finalPrice,
            List<AppliedPromotion> appliedPromotions) {
        this.subtotal = subtotal;
        this.discount = discount;
        this.finalPrice = finalPrice;
        this.appliedPromotions = List.copyOf(appliedPromotions);
    }

    public BigDecimal subtotal() {
        return subtotal;
    }

    public BigDecimal discount() {
        return discount;
    }

    public BigDecimal finalPrice() {
        return finalPrice;
    }

    public List<AppliedPromotion> appliedPromotions() {
        return appliedPromotions;
    }

    public static Builder builder(BigDecimal subtotal) {
        return new Builder(subtotal);
    }

    public static final class Builder {
        private final BigDecimal subtotal;
        private BigDecimal totalDiscount = BigDecimal.ZERO;
        private final List<AppliedPromotion> appliedPromotions = new ArrayList<>();

        private Builder(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }

        public Builder addDiscount(String code, BigDecimal amount) {
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                totalDiscount = totalDiscount.add(amount);
                appliedPromotions.add(new AppliedPromotion(code, amount));
            }
            return this;
        }

        public PricingResult build() {
            BigDecimal cappedDiscount = totalDiscount.min(subtotal);
            BigDecimal finalPrice = subtotal.subtract(cappedDiscount).max(BigDecimal.ZERO);
            return new PricingResult(subtotal, cappedDiscount, finalPrice, appliedPromotions);
        }
    }

    public record AppliedPromotion(String code, BigDecimal amount) {}
}
