package metrics.generator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogMetricSink implements MetricSink{
    private static final Logger LOG = LoggerFactory.getLogger(LogMetricSink.class);
    @Override
    public void sink(List<MetricSample> samples) {
        samples.forEach(
                sample -> LOG.info(sample.toPromExpositionFormat())
        );
    }
}
