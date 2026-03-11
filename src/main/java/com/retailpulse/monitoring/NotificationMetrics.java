package com.retailpulse.monitoring;

// TODO (Task 2 — Step 2.2): Implement this class.
//
// Requirements:
//   - Annotate with @Component
//   - Implement MeterBinder (io.micrometer.core.instrument.binder.MeterBinder)
//   - Override bindTo(MeterRegistry registry) and register:
//       * Counter  : "retailpulse.notifications.sent.total"   (tag: channel = EMAIL | SMS)
//       * Counter  : "retailpulse.notifications.failed.total" (tag: channel = EMAIL | SMS)
//   - Pre-initialise counters for both channels at startup (so Grafana shows 0, not "No data")
//   - Expose two public incrementor methods:
//       * incrementSent(String channel)
//       * incrementFailed(String channel)
//
// Refer to Task 2 — Step 2.2 in the README for the full implementation guide.
