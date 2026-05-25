CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    sku         VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    price       NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE promotions (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(32)  NOT NULL,
    discount_value  NUMERIC(12, 4),
    buy_quantity    INT,
    free_quantity   INT,
    target_sku      VARCHAR(64),
    priority        INT NOT NULL DEFAULT 100,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at       TIMESTAMP,
    ends_at         TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE coupons (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL UNIQUE,
    discount_amount NUMERIC(12, 2) NOT NULL CHECK (discount_amount >= 0),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at       TIMESTAMP,
    ends_at         TIMESTAMP,
    max_uses        INT,
    used_count      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    customer_type   VARCHAR(32) NOT NULL,
    subtotal        NUMERIC(12, 2) NOT NULL,
    discount        NUMERIC(12, 2) NOT NULL,
    final_price     NUMERIC(12, 2) NOT NULL,
    coupon_code     VARCHAR(64),
    status          VARCHAR(32) NOT NULL DEFAULT 'CALCULATED',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    sku         VARCHAR(64) NOT NULL,
    unit_price  NUMERIC(12, 2) NOT NULL,
    quantity    INT NOT NULL CHECK (quantity > 0),
    line_total  NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_promotions_active ON promotions(active, priority);
CREATE INDEX idx_coupons_code_active ON coupons(code, active);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
