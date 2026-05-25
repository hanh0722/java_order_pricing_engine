package com.ecommerce.pricing.infrastructure.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Evicts distributed/local promotion caches when rules change in the database.
 * Call these methods from an admin API or after promotion/coupon CRUD operations.
 */
@Service
public class PromotionCacheEvictionService {

    @CacheEvict(value = "activePromotions", allEntries = true)
    public void evictActivePromotions() {}

    @CacheEvict(value = "couponRules", allEntries = true)
    public void evictAllCoupons() {}

    @CacheEvict(value = "couponRules", key = "#couponCode")
    public void evictCoupon(String couponCode) {}

    @CacheEvict(value = {"activePromotions", "couponRules"}, allEntries = true)
    public void evictAll() {}
}
