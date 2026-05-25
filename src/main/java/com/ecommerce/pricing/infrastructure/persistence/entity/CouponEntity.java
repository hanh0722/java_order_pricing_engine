package com.ecommerce.pricing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "coupons")
public class CouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "used_count", nullable = false)
    private int usedCount;

    public String getCode() {
        return code;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public void incrementUsedCount() {
        this.usedCount++;
    }

    public boolean hasRemainingUses() {
        return maxUses == null || usedCount < maxUses;
    }
}
