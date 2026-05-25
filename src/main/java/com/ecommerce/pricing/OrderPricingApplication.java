package com.ecommerce.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OrderPricingApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderPricingApplication.class, args);
    }
}
