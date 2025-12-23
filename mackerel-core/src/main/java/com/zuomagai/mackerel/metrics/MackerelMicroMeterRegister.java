package com.zuomagai.mackerel.metrics;

import com.zuomagai.mackerel.MackerelCan;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public final class MackerelMicroMeterRegister {

    private MackerelMicroMeterRegister() {
    }

    public static void register(MackerelCan mackerelCan, MeterRegistry registry) {
        if (mackerelCan == null) {
            throw new IllegalArgumentException("mackerelCan is null");
        }
        if (registry == null) {
            throw new IllegalArgumentException("registry is null");
        }
        Tags tags = Tags.of(MetricsEntries.POOL_NAME, mackerelCan.toString());
        Gauge.builder(MetricsEntries.POOL_TOTAL, mackerelCan, MackerelCan::getAliveSize)
                .tags(tags)
                .register(registry);
        Gauge.builder(MetricsEntries.POOL_IDLE, mackerelCan, MackerelCan::getIdleSize)
                .tags(tags)
                .register(registry);
        Gauge.builder(MetricsEntries.POOL_WAITING, mackerelCan, MackerelCan::getWaitingThreadCount)
                .tags(tags)
                .register(registry);
    }
}
