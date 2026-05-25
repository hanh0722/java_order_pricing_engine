-- SUMMER10: unlimited uses (max_uses NULL) for challenge example
-- LIMITED1: single-use coupon for concurrency testing
UPDATE coupons SET max_uses = NULL, used_count = 0 WHERE code = 'SUMMER10';

INSERT INTO coupons (code, discount_amount, active, max_uses, used_count)
VALUES ('LIMITED1', 5.00, TRUE, 1, 0);
