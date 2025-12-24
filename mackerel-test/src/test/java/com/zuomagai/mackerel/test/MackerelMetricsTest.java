package com.zuomagai.mackerel.test;

import com.zuomagai.mackerel.Mackerel;
import com.zuomagai.mackerel.MackerelCan;
import com.zuomagai.mackerel.MackerelConfig;
import com.zuomagai.mackerel.metrics.MackerelMicroMeterRegister;
import com.zuomagai.mackerel.metrics.MetricsEntries;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MackerelMetricsTest {

    @Test
    public void registerMetricsExposesPoolValues() throws Exception {
        MackerelConfig config = new MackerelConfig();
        config.setPoolName("metricsTest");
        config.setJdbcUrl("jdbc:h2:mem:metrics_test;DB_CLOSE_DELAY=-1");
        config.setUserName("sa");
        config.setPassword("");

        MackerelCan mackerelCan = new MackerelCan(config);
        Mackerel mackerel = null;
        try (Connection connection = DriverManager.getConnection(config.getJdbcUrl(), config.getUserName(),
                config.getPassword())) {
            mackerel = new Mackerel(mackerelCan, connection);
            mackerelCan.add(mackerel);

            SimpleMeterRegistry registry = new SimpleMeterRegistry();
            MackerelMicroMeterRegister.register(mackerelCan, registry);

            Gauge total = registry.get(MetricsEntries.POOL_TOTAL)
                    .tags(MetricsEntries.POOL_NAME, config.getPoolName())
                    .gauge();
            Gauge idle = registry.get(MetricsEntries.POOL_IDLE)
                    .tags(MetricsEntries.POOL_NAME, config.getPoolName())
                    .gauge();
            Gauge waiting = registry.get(MetricsEntries.POOL_WAITING)
                    .tags(MetricsEntries.POOL_NAME, config.getPoolName())
                    .gauge();

            assertNotNull(total);
            assertNotNull(idle);
            assertNotNull(waiting);
            assertEquals(1.0, total.value(), 0.0001);
            assertEquals(1.0, idle.value(), 0.0001);
            assertEquals(0.0, waiting.value(), 0.0001);
        } finally {
            if (mackerel != null) {
                mackerel.closeQuietly();
            }
        }
    }
}
