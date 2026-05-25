package com.ecommerce.pricing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "promotions")
public class PromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(name = "discount_value")
    private BigDecimal value;

    @Column(name = "buy_quantity")
    private Integer buyQuantity;

    @Column(name = "free_quantity")
    private Integer freeQuantity;

    @Column(name = "target_sku")
    private String targetSku;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Integer getBuyQuantity() {
        return buyQuantity;
    }

    public Integer getFreeQuantity() {
        return freeQuantity;
    }

    public String getTargetSku() {
        return targetSku;
    }

    public int getPriority() {
        return priority;
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
}
