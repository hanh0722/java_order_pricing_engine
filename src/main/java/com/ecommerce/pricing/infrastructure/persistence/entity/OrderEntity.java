package com.ecommerce.pricing.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_type", nullable = false)
    private String customerType;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal discount;

    @Column(name = "final_price", nullable = false)
    private BigDecimal finalPrice;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(nullable = false)
    private String status = "CALCULATED";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }
}
