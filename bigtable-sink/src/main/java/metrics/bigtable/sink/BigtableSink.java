package metrics.bigtable.sink;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.models.ReadModifyWriteRow;
import com.google.common.base.Stopwatch;

import lombok.Builder;
import lombok.Data;
import metrics.generator.MetricGenerator;
import metrics.generator.MetricSample;
import metrics.generator.MetricSink;

@Data
@Builder
public class BigtableSink implements MetricSink {
    private final BigtableDataClient dataClient;
    private final AtomicInteger counter = new AtomicInteger();
    private String tableId;
    private String familyName;

    private static BigtableDataClient initClient(String projectId, String instanceId) {
        try {
            // Create the client.
            // Please note that creating the client is a very expensive operation
            // and should only be done once and shared in an application.
            return BigtableDataClient.create(projectId, instanceId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sink(List<MetricSample> samples) {
        long start = System.currentTimeMillis();
        samples.forEach(this::sink);
        long end = System.currentTimeMillis();
        counter.addAndGet(samples.size());
        System.out.println(String.format("count: %d, avgTime: %d", counter.get(), (end - start)  / samples.size()));
    }

    private void sink(MetricSample sample){
        ReadModifyWriteRow mutation = ReadModifyWriteRow.create(tableId, buildKey(sample))
                .increment(familyName, "value", (long) sample.value());
        dataClient.readModifyWriteRow(mutation);
    }

    private String buildKey(MetricSample sample) {
        long minute = sample.timestamp() - (sample.timestamp() % 60000);
        return String.format("%s#%s#%d",
                sample.labels().get("account"),
                sample.labels().get("project"),
                minute);
    }

    public static void main(String[] args) throws Exception {
        long seconds = Duration.ofHours(1).toSeconds();
        Instant startTime = Instant.parse("2023-04-05T12:00:00.00Z");
        Instant stopTime = startTime.plus(seconds, ChronoUnit.SECONDS);
        MetricGenerator metricGenerator = MetricGenerator.builder()
                .metricName("uplink")
                .nbInstances(10)
                .nbAccounts(10)
                .nbProjects(100)
                .nbDevices(100)
                .samplesPerSecond(100)
                .startTime(startTime.toEpochMilli())
                .stopTime(stopTime.toEpochMilli())
                .bulk(true)
                .build();
        Stopwatch watch = Stopwatch.createStarted();
        metricGenerator.generateMetrics(BigtableSink.builder()
                        .familyName("v")
                        .tableId("uplink")
                        .dataClient(initClient("sigfox-sandbox", "poc-metrics"))
                .build());
        watch = watch.stop();
        System.out.println("Elapsed time (ms): "+watch.elapsed().toMillis());
        System.out.println("Samples produced: "+metricGenerator.getNbInstances() * seconds * metricGenerator.getSamplesPerSecond());
    }
}
