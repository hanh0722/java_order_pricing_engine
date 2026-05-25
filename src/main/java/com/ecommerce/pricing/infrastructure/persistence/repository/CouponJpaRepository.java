package com.ecommerce.pricing.infrastructure.persistence.repository;

import com.ecommerce.pricing.infrastructure.persistence.entity.CouponEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {

    Optional<CouponEntity> findByCodeIgnoreCaseAndActiveTrue(String code);

    /**
     * Pessimistic write lock: blocks other transactions until this one commits.
     * Safe across threads and horizontally scaled instances (PostgreSQL row lock).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponEntity c WHERE UPPER(c.code) = UPPER(:code) AND c.active = true")
    Optional<CouponEntity> findByCodeForUpdate(@Param("code") String code);
}
