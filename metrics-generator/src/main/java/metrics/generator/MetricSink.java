package metrics.generator;

import java.util.List;

public interface MetricSink {
    void sink(List<MetricSample> samples);
}
