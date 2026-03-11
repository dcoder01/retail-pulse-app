package com.retailpulse.monitoring;

// TODO (Task 2 — Step 2.3): Implement this class.
//
// Requirements:
//   - Annotate with @Component
//   - Accept ProductRepository as a constructor argument
//   - Implement MeterBinder (io.micrometer.core.instrument.binder.MeterBinder)
//   - Override bindTo(MeterRegistry registry) and register:
//       * Gauge : "retailpulse.inventory.low_stock.count"
//                  backed by productRepository.findLowStockProducts().size()
//                  (the gauge is self-updating — no public methods needed)
//
// Note: InventoryService does NOT need to be modified at all.
//       The gauge reads the repository directly on every Prometheus scrape.
//
// Refer to Task 2 — Step 2.3 in the README for the full implementation guide.
