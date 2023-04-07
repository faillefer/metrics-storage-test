package metrics.generator;

import java.util.Map;

import lombok.Builder;
import lombok.ToString;

/**
 * MetricSample contains all elements which allows to build a metric sample as
 * defined by Prometheus model (https://prometheus.io/docs/concepts/data_model/)
 * @param metricName specifies the general feature of a system that is measured
 *                   (e.g. http_requests_total - the total number of HTTP requests received).
 *                   It may contain ASCII letters and digits, as well as underscores and colons.
 *                   It must match the regex [a-zA-Z_:][a-zA-Z0-9_:]*.
 * @param labels Labels enable Prometheus's dimensional data model: any given combination of labels
 *               for the same metric name identifies a particular dimensional instantiation
 *               of that metric (for example: all HTTP requests that used the method POST to
 *               the /api/tracks handler). The query language allows filtering and aggregation
 *               based on these dimensions. Changing any label value, including adding or removing
 *               a label, will create a new time series.
 * @param value the value
 * @param timestamp a millisecond-precision timestamp (unix epoch)
 */
@Builder
public record MetricSample(
        String metricName,
        Map<String, String> labels,
        double value,
        long timestamp
) {

    /**
     * return this sample in prometheus exposition format
     * @return name{labels} value timestamp
     */
    public String toPromExpositionFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append(metricName);
        appendLabel(sb);
        sb.append(" ");
        sb.append(value);
        sb.append(" ");
        sb.append(timestamp);
        return sb.toString();
    }
    private void appendLabel(StringBuilder sb) {
        sb.append("{");
        labels.forEach(
                (label, value) -> sb.append(label).append("=\"").append(value).append("\",")
        );
        sb.append("}");
    }
}
