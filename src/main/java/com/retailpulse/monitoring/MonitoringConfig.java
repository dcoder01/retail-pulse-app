package com.retailpulse.monitoring;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Pre-wires the AOP infrastructure that makes {@code @Timed} work.
 *
 * <p>In Spring Boot 3, {@code TimedAspect} is NOT auto-configured — it must be
 * declared as a bean explicitly. Without this class, {@code @Timed} annotations
 * on service methods are silently ignored and no timer metrics are recorded.</p>
 *
 * <p><strong>This file is provided for you. Do not modify it.</strong></p>
 *
 * <p>As part of Task 2 you will:</p>
 * <ol>
 *   <li>Add {@code @Timed} annotations to service methods (uses this bean automatically)</li>
 *   <li>Create {@code OrderMetrics}, {@code NotificationMetrics}, and
 *       {@code InventoryMetrics} in this package (see scaffold files)</li>
 * </ol>
 */
@Configuration
public class MonitoringConfig {

    /**
     * Enables processing of {@code @Timed} annotations via Spring AOP.
     * Required for {@code retailpulse.order.processing.time} and
     * {@code retailpulse.inventory.reserve.time} timers to be recorded.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
