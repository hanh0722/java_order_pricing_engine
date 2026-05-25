INSERT INTO products (sku, name, price) VALUES
    ('A100', 'Product A', 100.00),
    ('B200', 'Product B', 50.00),
    ('C300', 'Product C', 30.00);

-- Active global promotions (challenge example uses ORDER10PCT)
INSERT INTO promotions (code, name, type, discount_value, buy_quantity, free_quantity, target_sku, priority, active) VALUES
    ('ORDER10PCT', '10% Off Order Total', 'PERCENTAGE_ORDER', 10.0000, NULL, NULL, NULL, 200, TRUE),
    ('BXGY_A100', 'Buy 2 Get 1 Free - A100', 'BUY_X_GET_Y', NULL, 2, 1, 'A100', 100, FALSE),
    ('VIP5PCT', 'VIP Extra 5% Discount', 'VIP_CUSTOMER', 5.0000, NULL, NULL, NULL, 300, FALSE);

INSERT INTO coupons (code, discount_amount, active) VALUES
    ('SUMMER10', 10.00, TRUE);
