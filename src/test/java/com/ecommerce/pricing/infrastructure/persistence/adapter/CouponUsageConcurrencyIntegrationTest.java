package com.ecommerce.pricing.infrastructure.persistence.adapter;

import com.ecommerce.pricing.domain.exception.CouponExhaustedException;
import com.ecommerce.pricing.domain.exception.CouponNotFoundException;
import com.ecommerce.pricing.domain.port.CouponUsagePort;
import com.ecommerce.pricing.infrastructure.persistence.repository.CouponJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("dev")
class CouponUsageConcurrencyIntegrationTest {

    @Autowired
    private CouponUsagePort couponUsagePort;

    @Autowired
    private CouponJpaRepository couponRepository;

    @BeforeEach
    void resetLimitedCoupon() {
        couponRepository.findByCodeIgnoreCaseAndActiveTrue("LIMITED1")
                .ifPresent(coupon -> {
                    coupon.setUsedCount(0);
                    couponRepository.save(coupon);
                });
    }

    @Test
    void onlyOneThreadCanReserveSingleUseCoupon() throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                start.await();
                try {
                    couponUsagePort.reserve("LIMITED1");
                    return true;
                } catch (CouponExhaustedException | CouponNotFoundException e) {
                    return false;
                } finally {
                    done.countDown();
                }
            }));
        }

        start.countDown();
        done.await();
        executor.shutdown();

        long successes = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successes++;
            }
        }

        assertEquals(1, successes, "Exactly one reservation should succeed for max_uses=1");
    }
}
