# RetailPulse — Cloud-Native Monitoring Capstone Project

> **Bootcamp:** Cloud-Native Monitoring and Alerting — Prometheus + Grafana for Kubernetes & Microservices
> **Client:** Oracle | **Trainer:** Vaman Rao Deshmukh | **Vendor:** The Skill Enhancers

---

## Table of Contents

1. [Project Abstract](#1-project-abstract)
2. [Architecture Overview](#2-architecture-overview)
3. [Technology Stack](#3-technology-stack)
4. [Project Structure](#4-project-structure)
5. [REST API Reference](#5-rest-api-reference)
6. [How to Run the Application](#6-how-to-run-the-application)
7. [Capstone Assignment](#7-capstone-assignment)
8. [What You Need to Add](#8-what-you-need-to-add)
9. [Service Port Reference](#9-service-port-reference)
10. [Load Testing Script](#10-load-testing-script)
11. [Evaluation Criteria](#11-evaluation-criteria)
12. [Submission Checklist](#12-submission-checklist)

---

## 1. Project Abstract

**RetailPulse** is a multi-service Spring Boot e-commerce backend application. It simulates a real-world retail platform with three independent business domains:

| Domain | Responsibility |
|---|---|
| **Inventory Service** | Manages product catalogue, stock levels, and low-stock detection |
| **Order Service** | Handles order placement, stock reservation, and order lifecycle |
| **Notification Service** | Sends email/SMS delivery notifications with a configurable simulated failure rate |

The application is provided to you as a **fully functional monolithic Spring Boot service** running on a single port. Your job is not to build the application — it is already built. Your job is to **instrument, monitor, alert, and visualise** it using the observability stack you have learned in this bootcamp.

This project brings together every topic from Days 1 through 5: Prometheus metrics, PromQL, Micrometer instrumentation, Alertmanager routing, and Grafana dashboards — all deployed on a local Kubernetes cluster.

---

## 2. Architecture Overview

### Current State (What You Receive)

```
┌─────────────────────────────────────────────┐
│           RetailPulse Monolith              │
│                  :8888                      │
│                                             │
│  ┌────────────┐  ┌───────────┐  ┌────────┐ │
│  │ /api/      │  │ /api/     │  │ /api/  │ │
│  │ products   │  │ orders    │  │ notif- │ │
│  │            │  │           │  │ ication│ │
│  └────────────┘  └───────────┘  └────────┘ │
│                                             │
│         Spring Boot Actuator :8888          │
│         /actuator/prometheus (to add)       │
└─────────────────────────────────────────────┘
              │
         MySQL :3306
```

### Target State (What You Will Build)

```
┌──────────────────────────────────────────────────────────────┐
│                  Kubernetes Cluster (Minikube/Kind)          │
│                                                              │
│  ┌─────────────────┐     scrapes     ┌──────────────────┐   │
│  │  RetailPulse    │ ◄────────────── │   Prometheus     │   │
│  │  :8888          │                 │   :9090          │   │
│  │  /actuator/     │                 │                  │   │
│  │  prometheus     │                 │  Alert Rules     │   │
│  └─────────────────┘                 └────────┬─────────┘   │
│                                               │ fires        │
│  ┌─────────────────┐                 ┌────────▼─────────┐   │
│  │    Grafana      │ ◄── queries ─── │  Alertmanager    │   │
│  │    :3000        │                 │  :9093           │   │
│  │  Dashboards     │                 │  Email/Slack     │   │
│  └─────────────────┘                 └──────────────────┘   │
│                                                              │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │  Node Exporter  │  │ kube-state-     │                   │
│  │  :9100          │  │ metrics :8888   │                   │
│  └─────────────────┘  └─────────────────┘                   │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Technology Stack

### Application (Pre-built)

| Component | Technology |
|---|---|
| Framework | Spring Boot 3.2.3 |
| Language | Java 17 |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8.0 |
| Build Tool | Maven |
| Containerisation | Docker |
| Orchestration | Docker Compose / Kubernetes |

### Monitoring Stack (To Be Added by You)

| Component | Technology | Purpose |
|---|---|---|
| Metrics instrumentation | Micrometer + `MeterBinder` | Expose JVM + custom business metrics |
| Metrics collection | Prometheus | Scrape and store time-series data |
| Alerting engine | Alertmanager | Route and deliver alerts |
| Visualisation | Grafana | Dashboards and panels |
| Host metrics | Node Exporter | CPU, memory, disk metrics |
| Kubernetes metrics | kube-state-metrics | Pod, deployment, namespace metrics |
| K8s monitoring bundle | kube-prometheus-stack (Helm) | All-in-one Helm deployment |

---

## 4. Project Structure

### Current Structure (What You Receive)

```
retailpulse-monolith/
├── pom.xml                                      # Maven build file
├── Dockerfile                                   # Container image definition
├── docker-compose.yml                           # Local run with MySQL
├── README.md                                    # This file
│
├── src/main/
│   ├── resources/
│   │   ├── application.yml                      # App configuration
│   │   └── data.sql                             # Seed data (10 products)
│   │
│   └── java/com/retailpulse/
│       ├── RetailPulseApplication.java          # Main entry point
│       ├── common/
│       │   └── ApiResponse.java                 # Uniform response envelope
│       ├── exception/
│       │   └── GlobalExceptionHandler.java      # Centralised error handling
│       │
│       ├── inventory/                           # Product & stock domain
│       │   ├── controller/InventoryController
│       │   ├── service/InventoryService
│       │   ├── repository/ProductRepository
│       │   ├── model/Product
│       │   ├── dto/  (ProductRequest, ProductResponse, Stock*)
│       │   └── exception/ (ProductNotFound, InsufficientStock)
│       │
│       ├── order/                               # Order placement domain
│       │   ├── controller/OrderController
│       │   ├── service/OrderService
│       │   ├── repository/OrderRepository
│       │   ├── model/ (Order, OrderStatus)
│       │   └── dto/  (OrderRequest, OrderResponse)
│       │
│       └── notification/                        # Email/SMS domain
│           ├── controller/NotificationController
│           ├── service/NotificationService      # 10% simulated failure rate
│           ├── repository/NotificationRepository
│           ├── model/ (Notification, Channel, Status)
│           └── dto/  (Request, Response, Stats)
```

### Target Structure (After Your Changes)

```
retailpulse-monolith/
├── ... (all existing files unchanged)
│
├── src/main/
│   └── java/com/retailpulse/
│       │
│       └── monitoring/                          # ★ NEW — your entire Java contribution
│           ├── OrderMetrics.java                # Counters + timer for order domain
│           ├── NotificationMetrics.java         # Counters with channel tags
│           └── InventoryMetrics.java            # Low-stock gauge backed by repository
│
├── monitoring/                                  # ★ NEW — Kubernetes manifests
│   ├── helm-values.yml                          # Helm values (keeps passwords out of CLI)
│   ├── prometheus-config.yml
│   ├── prometheus-deployment.yml
│   ├── alertmanager-config.yml
│   ├── alertmanager-deployment.yml
│   ├── alert-rules.yml
│   └── grafana-dashboards/
│       ├── infrastructure-dashboard.json        # Exported Grafana JSON
│       └── application-dashboard.json
│
├── k8s/                                         # ★ NEW — App K8s manifests
│   ├── namespace.yml
│   ├── mysql-secret.yml                         # Credentials as a Secret, not ConfigMap
│   ├── mysql-statefulset.yml                    # StatefulSet, not Deployment
│   ├── mysql-service.yml
│   ├── retailpulse-configmap.yml
│   ├── retailpulse-deployment.yml
│   └── retailpulse-service.yml
│
└── load_test.py                                 # ★ NEW — load test script
```

> **Key principle:** Every existing `service/` file stays **exactly as it is** except for
> the addition of `@Timed` annotations (which are purely declarative). All Micrometer
> registration code lives exclusively in the `monitoring/` package.

---

## 5. REST API Reference

All responses are wrapped in a uniform envelope:

```json
{
  "success": true,
  "message": "optional message",
  "data": { ... }
}
```

### 5.1 Inventory — `/api/products`

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/api/products` | List all products | — |
| `GET` | `/api/products?category=Electronics` | Filter by category | — |
| `GET` | `/api/products/{productCode}` | Get single product | — |
| `POST` | `/api/products` | Create new product | See below |
| `PUT` | `/api/products/{productCode}/stock` | Set stock quantity | `{"quantity": 50}` |
| `POST` | `/api/products/{productCode}/reserve` | Reserve (subtract) stock | `{"quantity": 2}` |
| `GET` | `/api/products/low-stock` | Products at/below threshold | — |
| `GET` | `/api/products/health` | Inventory module health | — |

**Create Product body:**
```json
{
  "productCode": "P011",
  "name": "Wireless Headphones",
  "description": "Noise-cancelling over-ear headphones",
  "category": "Electronics",
  "price": 7500.00,
  "stockQuantity": 30,
  "lowStockThreshold": 5
}
```

**Seeded products available from startup:**

| Code | Name | Category | Price | Stock |
|------|------|----------|-------|-------|
| P001 | Laptop Pro 15 | Electronics | 85000 | 25 |
| P002 | Wireless Mouse | Electronics | 1200 | 150 |
| P003 | USB-C Hub 7-Port | Electronics | 2500 | 8 |
| P004 | Mechanical Keyboard | Electronics | 4500 | 45 |
| P005 | 27-inch Monitor | Electronics | 22000 | 3 |
| P006 | Running Shoes Pro | Sports | 5500 | 60 |
| P007 | Yoga Mat Premium | Sports | 1800 | 200 |
| P008 | Stainless Water Bottle | Sports | 800 | 7 |
| P009 | Java Programming Book | Books | 650 | 90 |
| P010 | Cloud Architecture Guide | Books | 850 | 35 |

---

### 5.2 Orders — `/api/orders`

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `POST` | `/api/orders` | Place an order | See below |
| `GET` | `/api/orders` | List all orders | — |
| `GET` | `/api/orders?status=CONFIRMED` | Filter by status | — |
| `GET` | `/api/orders?customerId=C001` | Filter by customer | — |
| `GET` | `/api/orders/{id}` | Get order by ID | — |
| `GET` | `/api/orders/ref/{orderRef}` | Get order by reference | — |
| `GET` | `/api/orders/stats` | Counts by status | — |
| `GET` | `/api/orders/health` | Order module health | — |

**Place Order body:**
```json
{
  "customerId": "C001",
  "productCode": "P001",
  "quantity": 2
}
```

**Order statuses:** `PENDING` | `CONFIRMED` | `FAILED`

**HTTP response codes for POST /api/orders:**
- `201 Created` — order confirmed, stock reserved
- `422 Unprocessable Entity` — order failed (insufficient stock, product not found)

---

### 5.3 Notifications — `/api/notifications`

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `POST` | `/api/notifications/email` | Send email notification | See below |
| `POST` | `/api/notifications/sms` | Send SMS notification | See below |
| `GET` | `/api/notifications/stats` | Delivery stats by channel | — |
| `GET` | `/api/notifications/health` | Notification module health | — |

**Send Notification body:**
```json
{
  "recipient": "customer@example.com",
  "subject": "Your order is confirmed",
  "message": "Order ORD-20260309-000042 has been placed successfully.",
  "orderId": "ORD-20260309-000042"
}
```

> **Note:** The notification service has a configurable simulated failure rate (default 10%). This is intentional — it gives you real FAILED metrics to observe and alert on. Set `notification.failure-rate` in `application.yml` to adjust.

---

### 5.4 Actuator Endpoints (Monitoring Entry Points)

| Endpoint | Description |
|----------|-------------|
| `GET /actuator/health` | Application health status |
| `GET /actuator/prometheus` | **Prometheus scrape endpoint** *(to be enabled by you)* |

> All other actuator endpoints (`/env`, `/beans`, `/heapdump`, etc.) will be deliberately
> **disabled** as part of Task 1. See the security note there.

---

## 6. How to Run the Application

### Option A — Docker Compose (Recommended for development)

**Prerequisites:** Docker Desktop running

```bash
# Clone / unzip the project
cd retailpulse-monolith

# Build and start (MySQL + app together)
docker compose up --build

# App will be available at:
# http://localhost:8888/api/products
# http://localhost:8888/actuator/health
```

To stop:
```bash
docker compose down
# To also remove the MySQL volume:
docker compose down -v
```

---

### Option B — Maven with external MySQL

**Prerequisites:** Java 17+, Maven, MySQL 8 running locally

```bash
# Create the database
mysql -u root -p -e "CREATE DATABASE retailpulse_db;"
mysql -u root -p -e "CREATE USER 'retailpulse'@'localhost' IDENTIFIED BY 'retailpulse123';"
mysql -u root -p -e "GRANT ALL ON retailpulse_db.* TO 'retailpulse'@'localhost';"

# Run the app
./mvnw spring-boot:run

# Or with custom DB credentials:
./mvnw spring-boot:run \
  -Dspring-boot.run.jvmArguments="\
  -DDB_HOST=localhost \
  -DDB_USERNAME=myuser \
  -DDB_PASSWORD=mypassword"
```

---

### Option C — H2 In-Memory (no MySQL needed)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2

# H2 web console: http://localhost:8888/h2-console
# JDBC URL: jdbc:h2:mem:retailpulse
# Username: sa  |  Password: (blank)
```

---

### Quick smoke test

```bash
# Health check
curl http://localhost:8888/actuator/health

# List products
curl http://localhost:8888/api/products | python3 -m json.tool

# Place a test order
curl -X POST http://localhost:8888/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"C001","productCode":"P002","quantity":3}'

# Check order stats
curl http://localhost:8888/api/orders/stats
```

---

## 7. Capstone Assignment

### Overview

You will add a **complete observability layer** to the RetailPulse application. This means:

1. Instrument the application to expose Prometheus-compatible metrics — **without touching existing business logic**
2. Deploy Prometheus to scrape those metrics
3. Write alert rules that fire under real failure conditions
4. Configure Alertmanager to route alerts
5. Build Grafana dashboards to visualise everything
6. Deploy the whole stack on Kubernetes and simulate a failure scenario

---

### A Guiding Principle Before You Write a Single Line

> **"Monitoring code does not belong in business logic."**

This is the most important design decision in this capstone. Before you touch any file, understand this clearly:

| Layer | Responsibility | Should it contain `MeterRegistry`? |
|---|---|---|
| `controller/` | Handle HTTP, validate input, return responses | No |
| `service/` | Business logic — place orders, reserve stock | **No** |
| `repository/` | Database access | No |
| `monitoring/` | Register and update metrics | **Yes — exclusively here** |

If you find yourself injecting `MeterRegistry` into `OrderService` or `NotificationService`, stop — you are in the wrong place. The `monitoring/` package you will create is the only location where Micrometer code lives.

The **one exception** is the `@Timed` annotation, which may be placed on service methods because it is purely declarative — it carries no imperative logic and does not change the method body in any way.

---

### Action Items

---

#### Task 1 — Enable Micrometer & Prometheus Endpoint

**Step 1.1 — Add the dependency to `pom.xml`:**

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Step 1.2 — Update `application.yml`:**

Follow the **principle of least privilege** when exposing actuator endpoints. Only expose exactly what Prometheus needs — do not open all endpoints indiscriminately.

```yaml
management:
  endpoints:
    web:
      exposure:
        # Expose only what is needed: health for K8s probes, prometheus for scraping
        include: health, prometheus
  endpoint:
    health:
      show-details: when-authorized   # Do not leak internal details publicly
    prometheus:
      enabled: true
  metrics:
    tags:
      application: retailpulse        # Attach app name as a global label on every metric
```

> **Why not `include: "*"`?**
> Exposing all actuator endpoints leaks environment variables (`/actuator/env`), heap dumps
> (`/actuator/heapdump`), thread dumps, bean definitions, and in some configurations a
> shutdown hook — to anyone who can reach port 8888. In production this is a serious
> security vulnerability. Only expose the endpoints you explicitly require.

**Step 1.3 — Verify:**
```bash
curl http://localhost:8888/actuator/prometheus
# Should return Prometheus text format (lines starting with # HELP, # TYPE, etc.)

# Verify that undisclosed endpoints are NOT reachable:
curl http://localhost:8888/actuator/env
# Should return 404
```

---

#### Task 2 — Add Custom Business Metrics via a Dedicated `monitoring` Package

> **Separation of Concerns in action.** All counter, gauge, and timer registrations live
> in one place — the `monitoring/` package. The existing service classes are not modified
> except for the addition of `@Timed` annotations (which are declarative) and injection of
> the metrics beans (which carry zero Micrometer imports).

**Why `MeterBinder`?**

Spring's `MeterBinder` interface is the correct, idiomatic way to register custom metrics in a Spring Boot application. When you implement `MeterBinder`, Spring Boot automatically calls your `bindTo(MeterRegistry registry)` method at startup after the registry is fully initialised. This avoids constructor-ordering issues, keeps all metric registration logic together, and means your service classes never need to import anything from `io.micrometer`.

---

**Step 2.1 — Create `OrderMetrics.java`**

Create at: `src/main/java/com/retailpulse/monitoring/OrderMetrics.java`

```java
package com.retailpulse.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

/**
 * Registers all order-domain metrics.
 *
 * Separation of Concerns: this class is the sole owner of all order metric
 * definitions and registrations. OrderService contains zero Micrometer code.
 *
 * OrderService calls the public incrementor methods on this bean —
 * it never touches MeterRegistry directly.
 */
@Component
public class OrderMetrics implements MeterBinder {

    private Counter ordersPlaced;
    private Counter ordersConfirmed;
    private Counter ordersFailed;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.ordersPlaced = Counter.builder("retailpulse.orders.placed.total")
                .description("Total number of order attempts")
                .register(registry);

        this.ordersConfirmed = Counter.builder("retailpulse.orders.confirmed.total")
                .description("Total number of successfully confirmed orders")
                .register(registry);

        this.ordersFailed = Counter.builder("retailpulse.orders.failed.total")
                .description("Total number of failed orders")
                .register(registry);
    }

    public void incrementPlaced()    { ordersPlaced.increment(); }
    public void incrementConfirmed() { ordersConfirmed.increment(); }
    public void incrementFailed()    { ordersFailed.increment(); }
}
```

---

**Step 2.2 — Create `NotificationMetrics.java`**

Create at: `src/main/java/com/retailpulse/monitoring/NotificationMetrics.java`

```java
package com.retailpulse.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

/**
 * Registers all notification-domain metrics.
 *
 * Uses a 'channel' tag so EMAIL and SMS can be graphed independently
 * in Grafana without needing separate metric names.
 *
 * Counters for both channels are pre-initialised at startup so Grafana
 * panels show 0 rather than "No data" before the first notification fires.
 */
@Component
public class NotificationMetrics implements MeterBinder {

    private MeterRegistry registry;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        // Pre-initialise all channel/status combinations
        notificationsSent("EMAIL");
        notificationsSent("SMS");
        notificationsFailed("EMAIL");
        notificationsFailed("SMS");
    }

    public void incrementSent(String channel) {
        notificationsSent(channel).increment();
    }

    public void incrementFailed(String channel) {
        notificationsFailed(channel).increment();
    }

    private Counter notificationsSent(String channel) {
        return Counter.builder("retailpulse.notifications.sent.total")
                .description("Total notifications successfully delivered")
                .tag("channel", channel)
                .register(registry);
    }

    private Counter notificationsFailed(String channel) {
        return Counter.builder("retailpulse.notifications.failed.total")
                .description("Total notifications that failed to deliver")
                .tag("channel", channel)
                .register(registry);
    }
}
```

---

**Step 2.3 — Create `InventoryMetrics.java`**

Create at: `src/main/java/com/retailpulse/monitoring/InventoryMetrics.java`

```java
package com.retailpulse.monitoring;

import com.retailpulse.inventory.repository.ProductRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

/**
 * Registers all inventory-domain metrics.
 *
 * The low-stock gauge is backed directly by the ProductRepository.
 * Prometheus reads the current database count on every scrape —
 * no manual increment/decrement calls are needed anywhere.
 *
 * InventoryService is not modified at all.
 */
@Component
public class InventoryMetrics implements MeterBinder {

    private final ProductRepository productRepository;

    public InventoryMetrics(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("retailpulse.inventory.low_stock.count", productRepository,
                repo -> repo.findLowStockProducts().size())
            .description("Current number of products at or below their low-stock threshold")
            .register(registry);
    }
}
```

---

**Step 2.4 — Add `@Timed` to service methods**

This is the **only change** to the existing service files. Add the `@Timed` annotation to the methods you want to time. There are no new fields, no constructor changes, and no Micrometer imports.

In `OrderService.java`:
```java
import io.micrometer.core.annotation.Timed;

@Timed(
    value = "retailpulse.order.processing.time",
    description = "Time taken to process an order end-to-end",
    percentiles = {0.5, 0.95, 0.99}
)
@Transactional
public OrderResponse placeOrder(OrderRequest request) {
    // existing code — completely unchanged
}
```

In `InventoryService.java`:
```java
import io.micrometer.core.annotation.Timed;

@Timed(
    value = "retailpulse.inventory.reserve.time",
    description = "Time taken to reserve stock for a product",
    percentiles = {0.5, 0.95, 0.99}
)
@Transactional
public ProductResponse reserveStock(String productCode, int quantity) {
    // existing code — completely unchanged
}
```

> **Why is `@Timed` acceptable in service classes when `MeterRegistry` is not?**
> Because `@Timed` is a pure annotation — it does not introduce any imperative monitoring
> logic into the method body. Spring's AOP infrastructure intercepts the method call and
> records the timing entirely outside your code. The annotation is informational, like
> `@Transactional`.

---

**Step 2.5 — Wire the metrics beans into the service classes**

Inject `OrderMetrics` and `NotificationMetrics` (not `MeterRegistry`) into the service classes that need to record events.

In `OrderService.java` — inject the bean and call its incrementors:
```java
@Service
public class OrderService {

    private final OrderRepository     orderRepository;
    private final InventoryService    inventoryService;
    private final NotificationService notificationService;
    private final OrderMetrics        orderMetrics;       // ← inject the metrics bean

    public OrderService(OrderRepository orderRepository,
                        InventoryService inventoryService,
                        NotificationService notificationService,
                        OrderMetrics orderMetrics) {
        this.orderRepository     = orderRepository;
        this.inventoryService    = inventoryService;
        this.notificationService = notificationService;
        this.orderMetrics        = orderMetrics;
    }

    @Timed(value = "retailpulse.order.processing.time", percentiles = {0.5, 0.95, 0.99})
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        orderMetrics.incrementPlaced();                    // ← call the metrics bean

        // ... all existing logic unchanged ...

        orderMetrics.incrementConfirmed();                 // ← on success path
        return OrderResponse.from(order);
    }

    private OrderResponse saveFailedOrder(...) {
        // ...
        orderMetrics.incrementFailed();                    // ← on failure path
        return OrderResponse.from(order);
    }
}
```

In `NotificationService.java` — inject the bean and call its incrementors:
```java
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMetrics    notificationMetrics;   // ← inject

    public NotificationService(NotificationRepository notificationRepository,
                                NotificationMetrics notificationMetrics) {
        this.notificationRepository = notificationRepository;
        this.notificationMetrics    = notificationMetrics;
    }

    public NotificationResponse send(NotificationChannel channel, NotificationRequest request) {
        // ... existing delivery logic unchanged ...

        if (delivered) {
            notificationMetrics.incrementSent(channel.name());    // ← call
        } else {
            notificationMetrics.incrementFailed(channel.name());  // ← call
        }

        return NotificationResponse.from(notificationRepository.save(notification));
    }
}
```

> **`InventoryService` is not modified at all.** The `InventoryMetrics` gauge reads
> directly from the repository and self-updates on every Prometheus scrape.

---

**Summary of the separation achieved:**

| Class | Contains business logic? | Contains Micrometer code? | Acceptable? |
|---|---|---|---|
| `OrderService` | Yes | Calls `orderMetrics.incrementX()` only — no `io.micrometer` imports | Yes |
| `NotificationService` | Yes | Calls `notificationMetrics.incrementX()` only — no `io.micrometer` imports | Yes |
| `InventoryService` | Yes | Nothing | Yes — completely untouched |
| `OrderMetrics` | No | Yes — all counter definitions | Yes — this is its sole purpose |
| `NotificationMetrics` | No | Yes — counters with channel tags | Yes — this is its sole purpose |
| `InventoryMetrics` | No | Yes — repository-backed gauge | Yes — this is its sole purpose |

---

#### Task 3 — Deploy Prometheus on Kubernetes

Create a `monitoring/` directory in your project. Inside it, create the following Kubernetes manifests:

**`monitoring/prometheus-config.yml`** — ConfigMap with `prometheus.yml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: monitoring
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s

    rule_files:
      - /etc/prometheus/rules/*.yml

    alerting:
      alertmanagers:
        - static_configs:
            - targets: ['alertmanager:9093']

    scrape_configs:
      - job_name: 'retailpulse'
        metrics_path: '/actuator/prometheus'
        static_configs:
          - targets: ['retailpulse-service:8888']

      - job_name: 'node-exporter'
        static_configs:
          - targets: ['node-exporter:9100']

      - job_name: 'kubernetes-pods'
        kubernetes_sd_configs:
          - role: pod
        relabel_configs:
          - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
            action: keep
            regex: "true"
          - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
            action: replace
            target_label: __metrics_path__
          - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_port]
            action: replace
            target_label: __address__
            regex: (.+)
            replacement: $1
```

Deploy Prometheus:

```bash
kubectl create namespace monitoring
kubectl apply -f monitoring/
kubectl port-forward svc/prometheus -n monitoring 9090:9090
```

Verify Prometheus is scraping RetailPulse at `http://localhost:9090/targets`.

---

#### Task 4 — Write Alert Rules

Create `monitoring/alert-rules.yml` with the following rules as a minimum:

| Alert Name | Condition | Severity | Description |
|---|---|---|---|
| `Watchdog` | `vector(1)` — always true | none | Dead-man's switch that proves the alerting pipeline is alive |
| `HighOrderFailureRate` | Order failure rate > 20% over 5m | warning | Too many orders failing |
| `NotificationFailureSpike` | Notification failures > 30% over 5m | warning | Delivery problems |
| `LowStockCritical` | Any product at zero stock | critical | Product out of stock |
| `AppDown` | `up{job="retailpulse"} == 0` | critical | Application is unreachable |
| `HighJvmMemory` | JVM heap > 80% of max | warning | Memory pressure |
| `SlowOrderProcessing` | p99 order latency > 2s | warning | Performance degradation |

> **Why `Watchdog`?**
> The `Watchdog` alert always fires. You configure Alertmanager to send a heartbeat
> notification for it on a regular schedule. If you ever stop receiving that heartbeat,
> you know the alerting pipeline itself has broken — not just your application. It is
> your monitor for the monitor. This is standard practice in every production environment.

**Example alert rule structure:**

```yaml
groups:
  - name: retailpulse.watchdog
    rules:
      - alert: Watchdog
        expr: vector(1)
        labels:
          severity: none
        annotations:
          summary: "Alerting pipeline is alive"
          description: "This alert always fires. If you stop receiving it, the alerting pipeline is broken."

  - name: retailpulse.application
    rules:
      - alert: AppDown
        expr: up{job="retailpulse"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "RetailPulse application is down"
          description: "The RetailPulse scrape target has been unreachable for more than 1 minute."

      - alert: HighOrderFailureRate
        expr: |
          rate(retailpulse_orders_failed_total[5m])
          /
          rate(retailpulse_orders_placed_total[5m]) > 0.20
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High order failure rate detected"
          description: "Order failure rate is {{ $value | humanizePercentage }} over the last 5 minutes."

      - alert: SlowOrderProcessing
        expr: |
          histogram_quantile(0.99,
            rate(retailpulse_order_processing_time_seconds_bucket[5m])
          ) > 2
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "Order processing p99 latency exceeds 2 seconds"
          description: "99th percentile order latency is {{ $value | humanizeDuration }}."
```

---

#### Task 5 — Configure Alertmanager

Create `monitoring/alertmanager-config.yml`:

```yaml
global:
  resolve_timeout: 5m

route:
  group_by: ['alertname', 'severity']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 1h
  receiver: 'default'
  routes:
    - match:
        severity: none          # Watchdog heartbeat — route to its own receiver
      receiver: 'watchdog'
      repeat_interval: 5m       # Expect a heartbeat every 5 minutes

    - match:
        severity: critical
      receiver: 'critical-alerts'
      continue: false

receivers:
  - name: 'watchdog'
    webhook_configs:
      - url: 'http://your-heartbeat-receiver-url'

  - name: 'default'
    webhook_configs:
      - url: 'http://your-webhook-url'   # Replace with Slack webhook or similar

  - name: 'critical-alerts'
    webhook_configs:
      - url: 'http://your-critical-webhook-url'
```

Test Alertmanager by:
1. Temporarily lowering the failure threshold to force an alert
2. Accessing `http://localhost:9093` (port-forward) to verify alert appears
3. Confirming the notification is delivered to your receiver

---

#### Task 6 — Deploy kube-prometheus-stack via Helm

> **Security note:** Never pass passwords as `--set` arguments on the command line.
> They are recorded in your shell history (`~/.bash_history`) and visible in `ps aux`
> output to other users on the same machine. Use a separate values file instead.

**Step 6.1 — Create `monitoring/helm-values.yml`:**

```yaml
grafana:
  adminUser: admin
  adminPassword: "admin123"    # Change this — never commit real passwords to git

prometheus:
  prometheusSpec:
    retention: 15d
    serviceMonitorSelectorNilUsesHelmValues: false

alertmanager:
  alertmanagerSpec:
    retention: 120h
```

**Step 6.2 — Install using the values file:**

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install kube-prometheus-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --values monitoring/helm-values.yml
```

Access the components:
```bash
kubectl port-forward svc/kube-prometheus-stack-grafana -n monitoring 3000:80
kubectl port-forward svc/kube-prometheus-stack-prometheus -n monitoring 9090:9090
kubectl port-forward svc/kube-prometheus-stack-alertmanager -n monitoring 9093:9093
```

---

#### Task 7 — Build Grafana Dashboards

You must create two dashboards in Grafana:

**Dashboard 1 — Infrastructure Dashboard**

Panels to include:
- CPU usage % per node
- Memory usage % per node
- Disk I/O
- Network in/out
- Pod count by namespace
- Pod restart count

**Dashboard 2 — RetailPulse Application Dashboard**

Panels to include:
- Orders placed per minute (rate)
- Orders confirmed vs failed (grouped bar or time series)
- Order failure % (gauge panel with thresholds: green < 10%, yellow < 25%, red ≥ 25%)
- Notification delivery success rate by channel (EMAIL vs SMS)
- Low-stock product count (stat panel — red if > 0)
- JVM heap memory used vs max
- HTTP request rate by endpoint
- p50 / p95 / p99 order processing latency
- Active alert count

For each panel, use variables for time range (`$__range`) and refresh interval. Set dashboard auto-refresh to 30s.

**Export your dashboards as JSON** and save them to `monitoring/grafana-dashboards/`. This follows the infrastructure-as-code principle — dashboards should be reproducible from source control, not just stored in Grafana's UI database.

```bash
# Export via Grafana API after building your dashboard
curl -u admin:admin123 \
  http://localhost:3000/api/dashboards/uid/<dashboard-uid> \
  | python3 -m json.tool > monitoring/grafana-dashboards/application-dashboard.json
```

---

#### Task 8 — Deploy to Kubernetes (Full Stack)

Write Kubernetes manifests for RetailPulse. The following practices are required:

**Use a `Secret` for database credentials — never a `ConfigMap`**

`ConfigMap` values are stored as plain text and are readable by any user or service account with `kubectl get configmap` permission in that namespace. Credentials must always go in a `Secret`.

`k8s/mysql-secret.yml`:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mysql-credentials
  namespace: retailpulse
type: Opaque
stringData:
  username: retailpulse
  password: retailpulse123    # In real projects use Vault, Sealed Secrets, or External Secrets Operator
```

Reference the secret in your deployment — not hardcoded environment variables:
```yaml
env:
  - name: DB_USERNAME
    valueFrom:
      secretKeyRef:
        name: mysql-credentials
        key: username
  - name: DB_PASSWORD
    valueFrom:
      secretKeyRef:
        name: mysql-credentials
        key: password
```

**Use a `StatefulSet` for MySQL — not a `Deployment`**

`Deployment` is designed for stateless workloads. MySQL is stateful: it requires a stable network identity and guaranteed, stable persistent storage binding. A `Deployment` can reschedule the pod to a different node, losing its `PersistentVolumeClaim` binding and corrupting data. Use `StatefulSet`.

`k8s/mysql-statefulset.yml`:
```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
  namespace: retailpulse
spec:
  serviceName: mysql
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
        - name: mysql
          image: mysql:8.0
          env:
            - name: MYSQL_DATABASE
              value: retailpulse_db
            - name: MYSQL_USER
              valueFrom:
                secretKeyRef:
                  name: mysql-credentials
                  key: username
            - name: MYSQL_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-credentials
                  key: password
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-credentials
                  key: password
          volumeMounts:
            - name: mysql-data
              mountPath: /var/lib/mysql
  volumeClaimTemplates:
    - metadata:
        name: mysql-data
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 5Gi
```

**RetailPulse Deployment — with probes, resource limits, and Prometheus annotations**

`k8s/retailpulse-deployment.yml`:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: retailpulse
  namespace: retailpulse
spec:
  replicas: 2
  selector:
    matchLabels:
      app: retailpulse
  template:
    metadata:
      labels:
        app: retailpulse
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/path: "/actuator/prometheus"
        prometheus.io/port: "8888"
    spec:
      containers:
        - name: retailpulse
          image: retailpulse:latest
          ports:
            - containerPort: 8888
          resources:
            requests:
              cpu: "250m"
              memory: "512Mi"
            limits:
              cpu: "500m"
              memory: "1Gi"
          env:
            - name: DB_HOST
              value: mysql
            - name: DB_USERNAME
              valueFrom:
                secretKeyRef:
                  name: mysql-credentials
                  key: username
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-credentials
                  key: password
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8888
            initialDelaySeconds: 30
            periodSeconds: 10
            failureThreshold: 3
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8888
            initialDelaySeconds: 20
            periodSeconds: 5
            failureThreshold: 3
```

> **Liveness vs Readiness probes:**
> - **Liveness** — Kubernetes restarts the container if this fails. Use it to detect a deadlocked or crashed application.
> - **Readiness** — Kubernetes removes the pod from the Service's load balancer if this fails, without restarting it. Use it to detect a temporarily overloaded application that should stop receiving traffic.
>
> Spring Boot Actuator automatically exposes `/actuator/health/liveness` and
> `/actuator/health/readiness` when running inside a container environment.

---

#### Task 9 — Failure Simulation (Capstone Demo)

During your capstone presentation, you must demonstrate a complete failure scenario:

**Scenario: Notification Service Degradation**

1. Increase the notification failure rate to 80%:
   ```bash
   kubectl set env deployment/retailpulse NOTIFICATION_FAILURE_RATE=80 -n retailpulse
   ```

2. Run the load test script (see Section 10) to generate traffic

3. Show in Prometheus that `retailpulse_notifications_failed_total` is rising

4. Show the `NotificationFailureSpike` alert transitioning from **Pending → Firing**

5. Show the alert appearing in Alertmanager at `:9093`

6. Show the Grafana dashboard panel turning red

7. Restore the failure rate to 10% and show the alert **Resolving**:
   ```bash
   kubectl set env deployment/retailpulse NOTIFICATION_FAILURE_RATE=10 -n retailpulse
   ```

---

## 8. What You Need to Add

### Changes to Existing Application Files

| File | Nature of change | What to add |
|---|---|---|
| `pom.xml` | New dependency | `micrometer-registry-prometheus` |
| `src/main/resources/application.yml` | Restrict actuator exposure | Expose `health` and `prometheus` only; add `application` global tag |
| `src/main/java/.../order/service/OrderService.java` | Inject metrics bean + annotation | Inject `OrderMetrics`; add `@Timed` to `placeOrder`; call `orderMetrics.incrementX()` at the right points |
| `src/main/java/.../notification/service/NotificationService.java` | Inject metrics bean | Inject `NotificationMetrics`; call `notificationMetrics.incrementX()` after delivery outcome |
| `src/main/java/.../inventory/service/InventoryService.java` | Annotation only | Add `@Timed` to `reserveStock` — nothing else |

> `InventoryService` does not need any metrics bean injected. The gauge in `InventoryMetrics` reads the repository directly and is entirely self-managing.

### New Java Files to Create

| File | Purpose |
|---|---|
| `src/main/java/com/retailpulse/monitoring/OrderMetrics.java` | All order counters — implements `MeterBinder` |
| `src/main/java/com/retailpulse/monitoring/NotificationMetrics.java` | All notification counters with `channel` tag — implements `MeterBinder` |
| `src/main/java/com/retailpulse/monitoring/InventoryMetrics.java` | Low-stock gauge backed by repository — implements `MeterBinder` |

### New Infrastructure Files to Create

| File | Purpose |
|---|---|
| `monitoring/helm-values.yml` | Helm values file (keeps credentials out of CLI history) |
| `monitoring/prometheus-config.yml` | Prometheus K8s ConfigMap |
| `monitoring/prometheus-deployment.yml` | Prometheus Deployment + Service |
| `monitoring/alertmanager-config.yml` | Alertmanager K8s ConfigMap |
| `monitoring/alertmanager-deployment.yml` | Alertmanager Deployment + Service |
| `monitoring/alert-rules.yml` | Prometheus alerting rules (including `Watchdog`) |
| `monitoring/grafana-dashboards/infrastructure-dashboard.json` | Exported Grafana infrastructure dashboard |
| `monitoring/grafana-dashboards/application-dashboard.json` | Exported Grafana application dashboard |
| `k8s/namespace.yml` | K8s namespace: `retailpulse` |
| `k8s/mysql-secret.yml` | DB credentials as a K8s `Secret` (not `ConfigMap`) |
| `k8s/mysql-statefulset.yml` | MySQL `StatefulSet` + `PersistentVolumeClaim` (not `Deployment`) |
| `k8s/mysql-service.yml` | MySQL headless ClusterIP Service |
| `k8s/retailpulse-configmap.yml` | Non-sensitive environment configuration |
| `k8s/retailpulse-deployment.yml` | App Deployment with probes, resource limits, Prometheus annotations |
| `k8s/retailpulse-service.yml` | App ClusterIP/NodePort Service |
| `load_test.py` | Python load testing script (see Section 10) |

---

## 9. Service Port Reference

| Service | Default Port | Access After Port-Forward |
|---|---|---|
| RetailPulse App | `8888` | `http://localhost:8888` |
| Prometheus UI | `9090` | `http://localhost:9090` |
| Alertmanager UI | `9093` | `http://localhost:9093` |
| Grafana UI | `3000` | `http://localhost:3000` |
| Node Exporter | `9100` | `http://localhost:9100/metrics` |
| MySQL | `3306` | Internal only |

### Grafana Default Credentials
```
Username: admin
Password: admin123  (set in monitoring/helm-values.yml)
```

### Port-Forward Commands (Quick Reference)

```bash
# RetailPulse app
kubectl port-forward svc/retailpulse-service -n retailpulse 8888:8888

# Prometheus
kubectl port-forward svc/kube-prometheus-stack-prometheus -n monitoring 9090:9090

# Alertmanager
kubectl port-forward svc/kube-prometheus-stack-alertmanager -n monitoring 9093:9093

# Grafana
kubectl port-forward svc/kube-prometheus-stack-grafana -n monitoring 3000:80

# Node Exporter
kubectl port-forward svc/node-exporter -n monitoring 9100:9100
```

---

## 10. Load Testing Script

Save the following as `load_test.py` in the root of the project.

```python
#!/usr/bin/env python3
"""
RetailPulse Load Test Script
============================
Generates realistic traffic across all three modules:
  - Inventory reads and stock operations
  - Order placement (mix of successful and stock-exhaustion scenarios)
  - Direct notification sends

Usage:
    python3 load_test.py                         # Default: 60s, 10 RPS, localhost:8888
    python3 load_test.py --duration 120          # Run for 2 minutes
    python3 load_test.py --rps 25                # 25 requests per second
    python3 load_test.py --host http://localhost:8888
    python3 load_test.py --duration 300 --rps 20 --workers 5
    python3 load_test.py --chaos                 # Enable chaos mode (spike orders on P005 to drain stock fast)
"""

import argparse
import json
import random
import time
import threading
from datetime import datetime
from urllib import request, error

# ── Configuration ──────────────────────────────────────────────────────────────

PRODUCTS      = ["P001", "P002", "P003", "P004", "P005", "P006", "P007", "P008", "P009", "P010"]
CUSTOMERS     = [f"C{str(i).zfill(3)}" for i in range(1, 21)]   # C001 to C020
CATEGORIES    = ["Electronics", "Sports", "Books"]

# Weighted endpoint selection: tuples of (weight, scenario_function_name)
SCENARIO_WEIGHTS = [
    (30, "list_products"),
    (10, "get_product"),
    (10, "list_by_category"),
    (5,  "low_stock"),
    (25, "place_order"),
    (10, "list_orders"),
    (5,  "order_stats"),
    (5,  "notification_stats"),
]

# ── Stats tracking ─────────────────────────────────────────────────────────────

class Stats:
    def __init__(self):
        self.lock       = threading.Lock()
        self.total      = 0
        self.success    = 0
        self.failed     = 0
        self.errors     = 0
        self.latencies  = []

    def record(self, status_code: int, latency_ms: float):
        with self.lock:
            self.total += 1
            self.latencies.append(latency_ms)
            if 200 <= status_code < 300:
                self.success += 1
            elif status_code == 422:
                self.failed += 1   # Expected: insufficient stock
            else:
                self.errors += 1

    def record_error(self, latency_ms: float):
        with self.lock:
            self.total  += 1
            self.errors += 1
            self.latencies.append(latency_ms)

    def summary(self) -> dict:
        with self.lock:
            if not self.latencies:
                return {}
            sorted_lat = sorted(self.latencies)
            n = len(sorted_lat)
            return {
                "total_requests" : self.total,
                "success"        : self.success,
                "failed_422"     : self.failed,
                "errors"         : self.errors,
                "avg_ms"         : round(sum(sorted_lat) / n, 1),
                "p50_ms"         : round(sorted_lat[int(n * 0.50)], 1),
                "p95_ms"         : round(sorted_lat[int(n * 0.95)], 1),
                "p99_ms"         : round(sorted_lat[int(n * 0.99)], 1),
                "max_ms"         : round(max(sorted_lat), 1),
            }

stats = Stats()

# ── HTTP helpers ───────────────────────────────────────────────────────────────

def do_get(url: str) -> tuple[int, dict]:
    start = time.monotonic()
    try:
        with request.urlopen(url, timeout=5) as resp:
            latency = (time.monotonic() - start) * 1000
            body    = json.loads(resp.read())
            stats.record(resp.status, latency)
            return resp.status, body
    except error.HTTPError as e:
        latency = (time.monotonic() - start) * 1000
        stats.record(e.code, latency)
        return e.code, {}
    except Exception:
        latency = (time.monotonic() - start) * 1000
        stats.record_error(latency)
        return 0, {}

def do_post(url: str, payload: dict) -> tuple[int, dict]:
    start   = time.monotonic()
    data    = json.dumps(payload).encode("utf-8")
    req     = request.Request(url, data=data,
                               headers={"Content-Type": "application/json"},
                               method="POST")
    try:
        with request.urlopen(req, timeout=5) as resp:
            latency = (time.monotonic() - start) * 1000
            body    = json.loads(resp.read())
            stats.record(resp.status, latency)
            return resp.status, body
    except error.HTTPError as e:
        latency = (time.monotonic() - start) * 1000
        stats.record(e.code, latency)
        return e.code, {}
    except Exception:
        latency = (time.monotonic() - start) * 1000
        stats.record_error(latency)
        return 0, {}

# ── Scenarios ──────────────────────────────────────────────────────────────────

def list_products(base_url, **_):
    do_get(f"{base_url}/api/products")

def get_product(base_url, **_):
    code = random.choice(PRODUCTS)
    do_get(f"{base_url}/api/products/{code}")

def list_by_category(base_url, **_):
    cat = random.choice(CATEGORIES)
    do_get(f"{base_url}/api/products?category={cat}")

def low_stock(base_url, **_):
    do_get(f"{base_url}/api/products/low-stock")

def place_order(base_url, chaos=False, **_):
    # In chaos mode, keep hammering P005 (only 3 in stock) to exhaust it quickly
    product  = "P005" if (chaos and random.random() < 0.6) else random.choice(PRODUCTS)
    quantity = random.randint(1, 5)
    customer = random.choice(CUSTOMERS)
    do_post(f"{base_url}/api/orders", {
        "customerId": customer,
        "productCode": product,
        "quantity": quantity
    })

def list_orders(base_url, **_):
    choice = random.random()
    if choice < 0.33:
        do_get(f"{base_url}/api/orders")
    elif choice < 0.66:
        status = random.choice(["CONFIRMED", "FAILED", "PENDING"])
        do_get(f"{base_url}/api/orders?status={status}")
    else:
        customer = random.choice(CUSTOMERS)
        do_get(f"{base_url}/api/orders?customerId={customer}")

def order_stats(base_url, **_):
    do_get(f"{base_url}/api/orders/stats")

def notification_stats(base_url, **_):
    do_get(f"{base_url}/api/notifications/stats")

SCENARIOS = {
    "list_products"     : list_products,
    "get_product"       : get_product,
    "list_by_category"  : list_by_category,
    "low_stock"         : low_stock,
    "place_order"       : place_order,
    "list_orders"       : list_orders,
    "order_stats"       : order_stats,
    "notification_stats": notification_stats,
}

def weighted_choice() -> str:
    total  = sum(w for w, _ in SCENARIO_WEIGHTS)
    r      = random.uniform(0, total)
    cumul  = 0
    for weight, name in SCENARIO_WEIGHTS:
        cumul += weight
        if r <= cumul:
            return name
    return SCENARIO_WEIGHTS[-1][1]

# ── Worker loop ────────────────────────────────────────────────────────────────

def worker(base_url: str, stop_event: threading.Event, rps: float,
           worker_id: int, chaos: bool):
    interval = 1.0 / rps
    while not stop_event.is_set():
        scenario_name = weighted_choice()
        fn = SCENARIOS[scenario_name]
        fn(base_url, chaos=chaos)
        time.sleep(interval)

# ── Progress reporter ──────────────────────────────────────────────────────────

def reporter(stop_event: threading.Event, interval_sec: int = 10):
    while not stop_event.is_set():
        time.sleep(interval_sec)
        s = stats.summary()
        if s:
            ts = datetime.now().strftime("%H:%M:%S")
            print(f"[{ts}] total={s['total_requests']:>6}  "
                  f"ok={s['success']:>5}  "
                  f"fail422={s['failed_422']:>4}  "
                  f"err={s['errors']:>3}  "
                  f"p50={s['p50_ms']:>6}ms  "
                  f"p95={s['p95_ms']:>6}ms  "
                  f"p99={s['p99_ms']:>6}ms")

# ── Main ───────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="RetailPulse Load Test")
    parser.add_argument("--host",     default="http://localhost:8888",
                        help="Base URL of the RetailPulse service")
    parser.add_argument("--duration", type=int, default=60,
                        help="Test duration in seconds (default: 60)")
    parser.add_argument("--rps",      type=float, default=10.0,
                        help="Requests per second per worker (default: 10)")
    parser.add_argument("--workers",  type=int, default=3,
                        help="Number of concurrent worker threads (default: 3)")
    parser.add_argument("--chaos",    action="store_true",
                        help="Chaos mode: spike orders on low-stock product to exhaust stock")
    args = parser.parse_args()

    print("=" * 65)
    print("  RetailPulse Load Test")
    print("=" * 65)
    print(f"  Target   : {args.host}")
    print(f"  Duration : {args.duration}s")
    print(f"  Workers  : {args.workers}")
    print(f"  RPS/wkr  : {args.rps}  (total ~{args.rps * args.workers:.0f} RPS)")
    print(f"  Chaos    : {'ENABLED' if args.chaos else 'disabled'}")
    print("=" * 65)

    # Warm-up check
    try:
        with request.urlopen(f"{args.host}/actuator/health", timeout=5) as r:
            print(f"  Health   : {r.status} OK\n")
    except Exception as e:
        print(f"\n  ERROR: Cannot reach {args.host}/actuator/health")
        print(f"  {e}")
        print("  Is the application running? Check Section 6 of the README.\n")
        return

    stop_event = threading.Event()

    threads = []
    for i in range(args.workers):
        t = threading.Thread(target=worker,
                             args=(args.host, stop_event, args.rps, i, args.chaos),
                             daemon=True)
        t.start()
        threads.append(t)

    report_thread = threading.Thread(target=reporter, args=(stop_event,), daemon=True)
    report_thread.start()

    print(f"Running load test... (Ctrl+C to stop early)\n")
    try:
        time.sleep(args.duration)
    except KeyboardInterrupt:
        print("\nInterrupted by user.")

    stop_event.set()
    for t in threads:
        t.join(timeout=3)

    s = stats.summary()
    print("\n" + "=" * 65)
    print("  FINAL RESULTS")
    print("=" * 65)
    for k, v in s.items():
        print(f"  {k:<20}: {v}")
    print("=" * 65)


if __name__ == "__main__":
    main()
```

### Running the Load Test

**Requirements:** Python 3.8+ (no external libraries — uses only the standard library)

```bash
# Make executable (Linux/macOS)
chmod +x load_test.py

# Basic 60-second test at ~30 RPS (10 RPS × 3 workers)
python3 load_test.py

# Longer test for sustained metric collection
python3 load_test.py --duration 300 --rps 15 --workers 4

# Point at Kubernetes NodePort
python3 load_test.py --host http://$(minikube ip):30080

# Chaos mode — exhausts P005 stock to trigger LowStockCritical alert
python3 load_test.py --duration 120 --rps 20 --chaos

# Restore P005 stock after chaos run
curl -X PUT http://localhost:8888/api/products/P005/stock \
  -H "Content-Type: application/json" \
  -d '{"quantity": 50}'
```

**Expected output:**
```
=================================================================
  RetailPulse Load Test
=================================================================
  Target   : http://localhost:8888
  Duration : 60s
  Workers  : 3
  RPS/wkr  : 10.0  (total ~30 RPS)
  Chaos    : disabled
=================================================================
  Health   : 200 OK

Running load test... (Ctrl+C to stop early)

[10:05:10] total=   298  ok=  261  fail422=  37  err=  0  p50=  12.4ms  p95=  45.1ms  p99=  89.3ms
[10:05:20] total=   601  ok=  529  fail422=  72  err=  0  p50=  11.8ms  p95=  42.7ms  p99=  83.1ms
...
=================================================================
  FINAL RESULTS
=================================================================
  total_requests      : 1823
  success             : 1608
  failed_422          : 215
  errors              : 0
  avg_ms              : 13.2
  p50_ms              : 11.9
  p95_ms              : 44.6
  p99_ms              : 87.2
  max_ms              : 312.4
=================================================================
```

> **Tip for Prometheus observers:** Run the load test while watching the Prometheus graph for `rate(retailpulse_orders_confirmed_total[1m])` and `rate(retailpulse_orders_failed_total[1m])`. You should see both counters climbing in real time.

---

## 11. Evaluation Criteria

Your capstone submission will be evaluated on the following dimensions:

### 11.1 Code Quality & Separation of Concerns (20 points)

| Criteria | Points |
|---|---|
| A dedicated `monitoring/` package exists containing `OrderMetrics`, `NotificationMetrics`, `InventoryMetrics` | 8 |
| Each metrics class implements `MeterBinder` — `MeterRegistry` is never injected into service classes | 6 |
| Service classes contain zero `import io.micrometer.*` statements (except the `@Timed` annotation import) | 6 |

### 11.2 Micrometer Instrumentation (15 points)

| Criteria | Points |
|---|---|
| `/actuator/prometheus` responds with valid Prometheus text format | 3 |
| All 7 custom business metrics are present in the scrape output | 7 |
| Metrics have correct types (Counter / Gauge / Timer), meaningful descriptions, and `channel` tags on notification metrics | 5 |

### 11.3 Prometheus Configuration (15 points)

| Criteria | Points |
|---|---|
| Prometheus successfully scrapes RetailPulse (target shows UP in `/targets`) | 4 |
| Node Exporter and kube-state-metrics are also scraping | 3 |
| All 7 alert rules are defined and syntactically valid, including `Watchdog` | 5 |
| Recording rules are present for at least 2 business metrics | 3 |

### 11.4 Alertmanager (15 points)

| Criteria | Points |
|---|---|
| Alertmanager is running and connected to Prometheus | 3 |
| `Watchdog` alert is routing to its own receiver with a `repeat_interval` | 4 |
| At least one application alert transitions Pending → Firing during the demo | 4 |
| Routing configuration correctly separates `critical`, `warning`, and `none` severity | 4 |

### 11.5 Grafana Dashboards (20 points)

| Criteria | Points |
|---|---|
| Infrastructure dashboard with ≥ 4 panels (CPU, memory, pods, network) | 5 |
| Application dashboard with ≥ 6 panels covering all three modules | 8 |
| Dashboard uses variables for time range and refresh interval | 4 |
| Both dashboards exported as JSON and committed to `monitoring/grafana-dashboards/` | 3 |

### 11.6 Kubernetes Deployment (10 points)

| Criteria | Points |
|---|---|
| MySQL deployed as a `StatefulSet` with a `PersistentVolumeClaim` | 3 |
| DB credentials stored in a `Secret`, not a `ConfigMap` | 2 |
| RetailPulse deployed with ≥ 2 replicas, resource requests/limits, and separate liveness/readiness probes | 3 |
| Prometheus pod-auto-discovery annotations present on RetailPulse pods | 2 |

### 11.7 Failure Simulation Demo (5 points)

| Criteria | Points |
|---|---|
| Failure is triggered, alert fires, and Grafana panel turns red | 3 |
| Alert resolves after restoring normal conditions | 2 |

**Total: 100 points**

---

## 12. Submission Checklist

Before submitting or presenting, verify all of the following:

**Code — Separation of Concerns**
- [ ] `src/main/java/com/retailpulse/monitoring/` directory exists with exactly 3 files
- [ ] `OrderMetrics`, `NotificationMetrics`, `InventoryMetrics` all implement `MeterBinder`
- [ ] `grep -r "MeterRegistry" src/main/java/com/retailpulse/order/` returns no results
- [ ] `grep -r "MeterRegistry" src/main/java/com/retailpulse/notification/` returns no results
- [ ] `grep -r "MeterRegistry" src/main/java/com/retailpulse/inventory/` returns no results

**Application**
- [ ] `mvn clean package` builds successfully with no errors
- [ ] `/actuator/prometheus` responds with metrics including custom `retailpulse.*` metrics
- [ ] `/actuator/health` is accessible
- [ ] `/actuator/env` returns 404 (confirms least-privilege actuator config is applied)

**Kubernetes**
- [ ] `kubectl get pods -n retailpulse` shows all pods Running
- [ ] `kubectl get pods -n monitoring` shows Prometheus, Alertmanager, Grafana Running
- [ ] `kubectl get statefulset mysql -n retailpulse` confirms MySQL is a `StatefulSet`
- [ ] `kubectl get secret mysql-credentials -n retailpulse` confirms credentials are in a `Secret`
- [ ] `kubectl describe pod <retailpulse-pod>` shows separate liveness and readiness probes

**Prometheus**
- [ ] `http://localhost:9090/targets` shows all targets as UP (green)
- [ ] `http://localhost:9090/rules` shows all 7 alert rules loaded, including `Watchdog`
- [ ] PromQL query `retailpulse_orders_confirmed_total` returns data
- [ ] `Watchdog` alert shows as Firing in Prometheus (this is correct — it should always be firing)

**Alertmanager**
- [ ] `http://localhost:9093` is accessible
- [ ] `Watchdog` alert is visible and routing to the heartbeat receiver
- [ ] At least one application alert has been triggered and is visible in the UI

**Grafana**
- [ ] `http://localhost:3000` is accessible (admin / admin123)
- [ ] Infrastructure dashboard is present with live data
- [ ] Application dashboard is present with live data
- [ ] All panels show data (no "No data" panels)
- [ ] `monitoring/grafana-dashboards/*.json` files exist in the project

**Load Test**
- [ ] `python3 load_test.py` runs for 60 seconds without crashing
- [ ] Grafana dashboard panels show activity during the load test

**Failure Simulation**
- [ ] Can trigger `NotificationFailureSpike` or `HighOrderFailureRate` alert
- [ ] Alert appears in Alertmanager
- [ ] Grafana panel reflects the degraded state
- [ ] Alert resolves after fix

---

*Good luck! You have everything you need — the application is running, the data is there, and the metrics are waiting to be exposed. Now go observe it.*

---

> **Questions during the bootcamp?** Refer to the Day-by-Day schedule in the course materials. The `MeterBinder` pattern and custom business metrics are covered on Day 2, alerting and the `Watchdog` pattern on Day 3, Grafana dashboard export on Day 4, and `StatefulSet` vs `Deployment` on Day 5.
