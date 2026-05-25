# Order Pricing & Promotion Engine

Spring Boot service that calculates e-commerce order totals by applying configurable promotion rules. Built with clean architecture, SOLID principles, and production-ready concurrency controls for coupon usage.

## Features

- REST API: `POST /orders/calculate`
- PostgreSQL persistence with Flyway migrations
- Promotion engine (**Strategy** + **Chain of Responsibility**)
- **Coupon usage limits** with database pessimistic locking (multi-thread & multi-instance safe)
- **Distributed caching** via Redis (`redis` profile) or local Caffeine (`dev` profile)
- Unit, integration, and concurrency tests
- Horizontally scalable, stateless design

## Table of Contents

- [Quick Start](#quick-start)
- [API](#api)
- [Request Flow](#request-flow)
- [Architecture](#architecture)
- [SOLID Principles](#solid-principles)
- [Design Patterns](#design-patterns)
- [Promotion Rules](#promotion-rules)
- [Database Design](#database-design)
- [Coupon Concurrency & Locking](#coupon-concurrency--locking)
- [Redis Distributed Cache](#redis-distributed-cache)
- [Scalability](#scalability)
- [Trade-offs](#trade-offs)
- [Project Structure](#project-structure)

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker (optional, for PostgreSQL and Redis)

### Run with in-memory H2 (fastest, local cache)

```bash
cd order-pricing-engine
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Uses **Caffeine** in-process cache. No Redis required.

### Run with PostgreSQL

```bash
docker compose up -d postgres
mvn spring-boot:run
```

### Run with PostgreSQL + Redis (production-like)

```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=redis
```

| Profile | Cache | Database | Use case |
|---------|-------|----------|----------|
| `dev` | Caffeine (local) | H2 in-memory | Local development |
| *(default)* | None configured | PostgreSQL | Single instance without Redis |
| `redis` | Redis (shared) | PostgreSQL | Multiple app instances |

> **Important:** Do not activate `dev` and `redis` together—they register conflicting `CacheManager` beans.

### Run tests

```bash
mvn test
```

Includes `CouponUsageConcurrencyIntegrationTest` (10 threads, single-use coupon).

## API

### Calculate order price

**`POST /orders/calculate`**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `customerType` | `REGULAR` \| `VIP` | Yes | Customer tier |
| `items` | array | Yes | Line items (`sku`, `price`, `quantity`) |
| `couponCode` | string | No | Coupon to apply (e.g. `SUMMER10`) |

**Example request:**

```bash
curl -X POST http://localhost:8080/orders/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "customerType": "VIP",
    "items": [
      { "sku": "A100", "price": 100, "quantity": 2 },
      { "sku": "B200", "price": 50, "quantity": 1 }
    ],
    "couponCode": "SUMMER10"
  }'
```

**Success response (`200`):**

```json
{
  "subtotal": 250.00,
  "discount": 35.00,
  "finalPrice": 215.00,
  "orderId": 1,
  "appliedPromotions": [
    { "code": "ORDER10PCT", "amount": 25.00 },
    { "code": "SUMMER10", "amount": 10.00 }
  ]
}
```

Calculation: 10% order discount ($25) + `SUMMER10` coupon ($10) = **$35** off.

### Error responses

| Status | Title | When |
|--------|-------|------|
| `400` | Validation failed | Invalid request body |
| `404` | Coupon not found | Unknown, inactive, or out-of-schedule coupon |
| `409` | Coupon exhausted | `used_count >= max_uses` under concurrent access |

**Example (`409`):**

```json
{
  "type": "about:blank",
  "title": "Coupon exhausted",
  "status": 409,
  "detail": "Coupon usage limit reached: LIMITED1"
}
```

## Request Flow

End-to-end processing for a request with a coupon:

```
Client
  │
  ▼
OrderPricingController
  │
  ▼
OrderPricingApplicationService  (@Transactional)
  │
  ├─1─► CouponUsagePort.reserve()     ← DB row lock (FOR UPDATE)
  │         increment coupons.used_count
  │
  ├─2─► OrderPricingService.calculatePrice()
  │         PromotionPipeline (Strategy + Chain of Responsibility)
  │         reads rules from cache → DB on miss
  │
  └─3─► OrderPersistencePort.saveCalculatedOrder()
            persist orders + order_items
  │
  ▼
Commit (or rollback all steps on failure)
```

## Architecture

**Clean Architecture** (hexagonal) with four layers:

| Layer | Package | Responsibility |
|-------|---------|----------------|
| API | `api` | REST controllers, exception handling |
| Application | `application` | Use cases, DTOs, transaction boundaries |
| Domain | `domain` | Pricing logic, ports, promotion engine (no Spring/JPA) |
| Infrastructure | `infrastructure` | JPA, Redis/Caffeine cache, adapters |

**Dependency rule:** outer layers depend inward; domain never depends on frameworks.

```
┌─────────────────────────────────────────┐
│  OrderPricingController               │
└─────────────────┬───────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│  OrderPricingApplicationService         │
│  • reserve coupon (CouponUsagePort)     │
│  • calculate price                      │
│  • persist order                        │
└─────────────────┬───────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│  OrderPricingService + PromotionPipeline│
└─────────────────┬───────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│  Ports (interfaces)                     │
│  PromotionRulePort │ CouponUsagePort     │
│  OrderPersistencePort                   │
└─────────────────┬───────────────────────┘
                  ▼
┌─────────────────────────────────────────┐
│  Adapters (JPA, cache)                  │
└─────────────────────────────────────────┘
```

## SOLID Principles

| Principle | How it is applied |
|-----------|-------------------|
| **S** – Single Responsibility | Strategies handle one promotion type; `CouponUsageAdapter` only manages coupon inventory; application service orchestrates |
| **O** – Open/Closed | New promotions via new `PromotionStrategy` + DB row—no changes to existing strategies |
| **L** – Liskov Substitution | Strategies and ports are interchangeable via interfaces |
| **I** – Interface Segregation | Narrow ports: `PromotionRulePort`, `CouponUsagePort`, `OrderPersistencePort` |
| **D** – Dependency Inversion | Domain defines ports; infrastructure implements them (Spring DI) |

## Design Patterns

### Strategy Pattern

Each promotion type has a dedicated strategy implementing `PromotionStrategy`:

| Type | Class |
|------|-------|
| `PERCENTAGE_ORDER` | `PercentageOrderStrategy` |
| `BUY_X_GET_Y` | `BuyXGetYStrategy` |
| `VIP_CUSTOMER` | `VipCustomerStrategy` |
| `COUPON_FIXED` | `CouponFixedStrategy` |

### Chain of Responsibility

Handlers process rules in priority order:

1. **Item-level** (`ItemLevelPromotionHandler`) – Buy X Get Y
2. **Order-level** (`OrderLevelPromotionHandler`) – Percentage, VIP
3. **Coupon** (`CouponPromotionHandler`) – Fixed coupon discount

Each handler calls `supports(rule)` before delegating to the matching strategy.

## Promotion Rules

Seeded in Flyway (`V2`, `V3`):

| Code | Type | Description | Active by default |
|------|------|-------------|-------------------|
| `ORDER10PCT` | PERCENTAGE_ORDER | 10% off running total | Yes |
| `BXGY_A100` | BUY_X_GET_Y | Buy 2, get 1 free on SKU A100 | No |
| `VIP5PCT` | VIP_CUSTOMER | Extra 5% for VIP customers | No |
| `SUMMER10` | COUPON_FIXED | $10 off (unlimited uses) | Yes |
| `LIMITED1` | COUPON_FIXED | $5 off (`max_uses = 1`) | Yes |

Enable optional promotions:

```sql
UPDATE promotions SET active = TRUE WHERE code IN ('BXGY_A100', 'VIP5PCT');
```

Set coupon usage limits:

```sql
UPDATE coupons SET max_uses = 100, used_count = 0 WHERE code = 'SUMMER10';
```

## Database Design

### Tables

| Table | Purpose | Key columns |
|-------|---------|-------------|
| `products` | Product catalog | `sku`, `price`, `active` |
| `promotions` | Promotion rules | `type`, `value`, `priority`, `active` |
| `coupons` | Coupon codes & inventory | `code`, `discount_amount`, `max_uses`, `used_count`, `version` |
| `orders` | Calculation snapshots | `subtotal`, `discount`, `final_price`, `coupon_code` |
| `order_items` | Line items | `order_id`, `sku`, `quantity`, `line_total` |

### Coupon inventory columns

| Column | Description |
|--------|-------------|
| `max_uses` | Maximum redemptions (`NULL` = unlimited) |
| `used_count` | Current redemption count |
| `version` | Optimistic locking (`@Version`) |

### Relationships

- `orders` 1 → N `order_items`
- Promotions and coupons are resolved at runtime (not FK-linked to orders) so rules can change without migrating historical orders

### Indexes

- `promotions(active, priority)`
- `coupons(code, active)`
- `order_items(order_id)`

### Migrations

| Version | Content |
|---------|---------|
| `V1` | Schema |
| `V2` | Seed products, promotions, coupons |
| `V3` | Coupon usage limits (`LIMITED1`) |

## Coupon Concurrency & Locking

When many threads or app instances redeem the same coupon, `used_count` must never exceed `max_uses`.

### Why not `synchronized`?

```java
// Only safe within ONE JVM — breaks with multiple pods
public synchronized void reserve(String code) { ... }
```

| Approach | Single JVM | Multiple instances |
|----------|------------|-------------------|
| `synchronized` | Yes | **No** |
| DB pessimistic lock (`FOR UPDATE`) | Yes | **Yes** |
| Redis distributed lock | Yes | Yes (optional) |

### Implementation

| Component | Role |
|-----------|------|
| `CouponJpaRepository.findByCodeForUpdate()` | `PESSIMISTIC_WRITE` — blocks other transactions on the same row |
| `CouponUsagePort.reserve()` | Verify schedule, check `used_count < max_uses`, increment |
| `CouponEntity.@Version` | Optimistic lock on concurrent saves |
| `@Transactional` on `calculate()` | Rolls back coupon increment if pricing or order save fails |
| `PromotionCacheEvictionService.evictCoupon()` | Invalidates stale cached coupon rules after increment |

### Lock sequence

```
Thread A                          Thread B
   │                                 │
   ├─ SELECT ... FOR UPDATE          │
   │  (row locked)                   ├─ SELECT ... FOR UPDATE (waits…)
   ├─ used_count++                    │
   ├─ COMMIT                         │
   │                                 ├─ sees updated count
   │                                 └─ 409 CouponExhaustedException
```

### Transaction order

1. **`reserve(couponCode)`** — lock row, increment `used_count`
2. **`calculatePrice()`** — apply promotion pipeline
3. **`saveCalculatedOrder()`** — write `orders` + `order_items`

All three steps share one transaction.

## Redis Distributed Cache

For multiple app instances, in-process Caffeine is **not shared**—each pod holds its own copy. Use the `redis` profile for a shared cache.

### Components

| Class | Profile | Role |
|-------|---------|------|
| `CaffeineCacheConfig` | `dev` | Local in-process cache |
| `RedisCacheConfig` | `redis` | Shared Redis cache with JSON serialization |
| `PromotionCacheEvictionService` | all | Manual cache invalidation |

### Configuration (`redis` profile)

```yaml
spring:
  cache:
    type: redis
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

| Environment variable | Default | Description |
|---------------------|---------|-------------|
| `REDIS_HOST` | `localhost` | Redis hostname |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | *(empty)* | Optional password |

### Cache entries

| Cache name | Redis key prefix | TTL |
|------------|------------------|-----|
| `activePromotions` | `pricing:activePromotions::` | 5 min |
| `couponRules` | `pricing:couponRules::` | 10 min |

### Cache invalidation

Call after admin updates to promotions or coupons:

```java
@Autowired
private PromotionCacheEvictionService cacheEviction;

cacheEviction.evictAll();
cacheEviction.evictCoupon("SUMMER10");
```

`CouponUsageAdapter` automatically evicts the coupon cache after each successful `reserve()`.

### Multi-instance topology

```
                    ┌─────────────┐
  Client ──► [LB] ──┤  App Pod 1  ├──┐
                    │  App Pod 2  ├──┼──► Redis (shared cache)
                    │  App Pod 3  ├──┘         │
                    └─────────────┘              ▼
                                          PostgreSQL
                                     (coupon locks + orders)
```

## Scalability

| Concern | Approach |
|---------|----------|
| Horizontal scaling | Stateless API; any instance handles any request |
| Coupon inventory | PostgreSQL row locks (authoritative across all instances) |
| Promotion reads | Caffeine (`dev`) or Redis (`redis`) with TTL + explicit eviction |
| Database | HikariCP pool, indexed lookups, `open-in-view: false` |
| Per-request isolation | New `PromotionContext` per request (no shared mutable state) |

## Trade-offs

| Decision | Benefit | Cost |
|----------|---------|------|
| DB pessimistic lock for coupons | Correct under high concurrency; works across all instances | Row lock contention on hot coupons |
| Reserve-before-calculate | Prevents showing a price for an exhausted coupon | Coupon slot consumed even if pricing fails (rolled back in same TX) |
| Sequential discount stacking | Predictable, auditable totals | Handler order affects final price |
| Redis / Caffeine for rules | Fewer DB reads for promotion metadata | Cache can be stale until TTL or eviction |
| Persist every calculation | Audit trail and analytics | Extra write per request |
| Flyway + `ddl-auto: validate` | Safe schema evolution | Requires disciplined migrations |

## Project Structure

```
order-pricing-engine/
├── docker-compose.yml          # PostgreSQL + Redis
├── pom.xml
└── src/main/java/com/ecommerce/pricing/
    ├── OrderPricingApplication.java
    ├── api/
    │   ├── OrderPricingController.java
    │   └── GlobalExceptionHandler.java
    ├── application/
    │   ├── OrderPricingApplicationService.java
    │   └── dto/
    ├── domain/
    │   ├── exception/          # CouponNotFoundException, CouponExhaustedException
    │   ├── model/
    │   ├── port/               # PromotionRulePort, CouponUsagePort, OrderPersistencePort
    │   ├── promotion/
    │   │   ├── strategy/       # Strategy pattern
    │   │   └── chain/          # Chain of Responsibility
    │   └── service/
    └── infrastructure/
        ├── cache/              # PromotionCacheEvictionService
        ├── config/             # CaffeineCacheConfig, RedisCacheConfig, PromotionConfig
        └── persistence/
            ├── entity/
            ├── repository/
            └── adapter/        # PromotionRuleAdapter, CouponUsageAdapter, OrderPersistenceAdapter
```

### Tests

```
src/test/java/.../
├── domain/promotion/strategy/   # Unit tests per strategy
├── domain/service/              # Challenge example pricing test
├── infrastructure/persistence/adapter/
│   ├── CouponUsageAdapterTest
│   └── CouponUsageConcurrencyIntegrationTest
└── api/
    └── OrderPricingControllerIntegrationTest
```

## License

MIT (challenge submission)
