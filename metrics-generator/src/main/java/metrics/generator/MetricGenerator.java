package metrics.generator;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetricGenerator {
    private int nbInstances;
    private int nbProjects;
    private int nbAccounts;
    private int nbDevices;
    private long startTime;
    private long stopTime;
    private int samplesPerSecond;
    private String metricName;
    private boolean bulk;

    public void generateMetrics(MetricSink sink) throws InterruptedException {
        ExecutorService svc = Executors.newFixedThreadPool(nbInstances);
        createAppInstances(sink).forEach( instance -> svc.submit(instance) );
        svc.shutdown();
        svc.awaitTermination(1, TimeUnit.DAYS);
    }

    private List<AppInstance> createAppInstances(MetricSink sink) {
        List<AppInstance> instances = new ArrayList<>();
        for(int i = 1; i <= nbInstances; i++){
            AppInstance instance = AppInstance.builder()
                    .metricSink(sink)
                    .instanceName(String.format("%s-%d", "instance", i))
                    .bulk(bulk)
                    .nbAccounts(nbAccounts)
                    .nbProjects(nbProjects)
                    .nbDevices(nbDevices)
                    .metricName(metricName)
                    .startTime(startTime)
                    .stopTime(stopTime)
                    .samplesPerSecond(samplesPerSecond)
                    .build();
            instances.add(instance);
        }
        return instances;
    }

    public static void main(String[] args) throws InterruptedException {
        long seconds = Duration.ofHours(1).toSeconds();
        Instant startTime = Instant.parse("2023-04-05T12:00:00.00Z");
        Instant stopTime = startTime.plus(seconds, ChronoUnit.SECONDS);
        MetricGenerator metricGenerator = MetricGenerator.builder()
                .metricName("uplink")
                .nbInstances(10)
                .nbAccounts(1000)
                .nbProjects(120)
                .nbDevices(100)
                .samplesPerSecond(1000)
                .startTime(startTime.toEpochMilli())
                .stopTime(stopTime.toEpochMilli())
                .bulk(true)
                .build();
        metricGenerator.generateMetrics(new LogMetricSink());
        Stopwatch watch = Stopwatch.createStarted();
        watch = watch.stop();
        System.out.println("Elapsed time (ms): "+watch.elapsed().toMillis());
        System.out.println("Samples produced: "+metricGenerator.getNbInstances() * seconds * metricGenerator.getSamplesPerSecond());
    }
}
