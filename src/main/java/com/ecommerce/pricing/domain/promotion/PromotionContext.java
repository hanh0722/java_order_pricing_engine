package com.ecommerce.pricing.domain.promotion;

import com.ecommerce.pricing.domain.model.CustomerType;
import com.ecommerce.pricing.domain.model.OrderItem;
import com.ecommerce.pricing.domain.model.PricingResult;

import java.math.BigDecimal;
import java.util.List;

public final class PromotionContext {

    private final CustomerType customerType;
    private final List<OrderItem> items;
    private final String couponCode;
    private final BigDecimal subtotal;
    private final PricingResult.Builder resultBuilder;
    private BigDecimal runningTotal;

    public PromotionContext(
            CustomerType customerType,
            List<OrderItem> items,
            String couponCode,
            BigDecimal subtotal,
            PricingResult.Builder resultBuilder) {
        this.customerType = customerType;
        this.items = items;
        this.couponCode = couponCode;
        this.subtotal = subtotal;
        this.resultBuilder = resultBuilder;
        this.runningTotal = subtotal;
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

    public BigDecimal subtotal() {
        return subtotal;
    }

    public BigDecimal runningTotal() {
        return runningTotal;
    }

    public void applyDiscount(String code, BigDecimal amount) {
        BigDecimal applicable = amount.min(runningTotal);
        if (applicable.compareTo(BigDecimal.ZERO) > 0) {
            runningTotal = runningTotal.subtract(applicable);
            resultBuilder.addDiscount(code, applicable);
        }
    }

    public PricingResult.Builder resultBuilder() {
        return resultBuilder;
    }
}
