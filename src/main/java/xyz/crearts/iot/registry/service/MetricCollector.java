package xyz.crearts.iot.registry.service;

import com.google.common.util.concurrent.AtomicDouble;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetricCollector {
    private final MeterRegistry metrics;
    private final Map<String, AtomicDouble> metricsGauges = new HashMap<>();

    public void updateGauge(String name, String product, String device, double value) {
        var key = product + "." + device + "." + name;
        metricsGauges.computeIfAbsent(key, k -> this.metrics.gauge(
                        name, List.of(Tag.of("product", product), Tag.of("device", device)),
                        new AtomicDouble(value))).set(value);
    }
}
