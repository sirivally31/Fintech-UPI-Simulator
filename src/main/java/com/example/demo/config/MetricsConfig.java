package com.example.demo.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enterprise Micrometer Configuration for the Fintech UPI Payment Simulator.
 * <p>
 * Configures the global {@link MeterRegistry} with standardized application tags,
 * ensuring seamless integration with Prometheus monitoring infrastructure.
 * </p>
 *
 * @author Fintech UPI Simulator Architecture Team
 * @version 1.0
 * @since Phase 8 - Step 1
 */
@Configuration
public class MetricsConfig {

    /**
     * Customizes the default {@link MeterRegistry} by attaching common tags
     * such as application name, environment, and tier to all emitted metrics.
     *
     * @return a {@link MeterRegistryCustomizer} bean configured with global common tags.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(
                        "application", "upi-payment-simulator",
                        "environment", "production",
                        "tier", "backend"
                );
    }
}
