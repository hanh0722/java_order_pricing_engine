package com.ecommerce.pricing.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank String sku,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotNull @Min(1) Integer quantity) {}
