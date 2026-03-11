package com.retailpulse.monitoring;

// TODO (Task 2 — Step 2.1): Implement this class.
//
// Requirements:
//   - Annotate with @Component
//   - Implement MeterBinder (io.micrometer.core.instrument.binder.MeterBinder)
//   - Override bindTo(MeterRegistry registry) and register:
//       * Counter  : "retailpulse.orders.placed.total"
//       * Counter  : "retailpulse.orders.confirmed.total"
//       * Counter  : "retailpulse.orders.failed.total"
//   - Expose three public incrementor methods:
//       * incrementPlaced()
//       * incrementConfirmed()
//       * incrementFailed()
//
// Refer to Task 2 — Step 2.1 in the README for the full implementation guide.
