package metrics.victoria.sink;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.reactive.function.client.WebClient;

import com.google.common.base.Stopwatch;

import metrics.generator.MetricGenerator;
import metrics.generator.MetricSample;
import metrics.generator.MetricSink;
import reactor.core.publisher.Mono;

public class VictoriaSink implements MetricSink {
    private final WebClient client;
    private final AtomicInteger counter = new AtomicInteger();
    public VictoriaSink() {
        client = webClient();
        long seconds = Duration.ofHours(48).toSeconds();
        Instant startTime = Instant.parse("2023-04-05T12:00:00.00Z");
        Instant stopTime = startTime.plus(seconds, ChronoUnit.SECONDS);
        MetricGenerator metricGenerator = MetricGenerator.builder()
                .metricName("uplink")
                .nbInstances(10)
                .nbAccounts(1000)
                .nbProjects(120)
                .nbDevices(100)
                .samplesPerSecond(100)
                .startTime(startTime.toEpochMilli())
                .stopTime(stopTime.toEpochMilli())
                .build();
        Stopwatch watch = Stopwatch.createStarted();
        try {
            metricGenerator.generateMetrics(this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        watch = watch.stop();
        System.out.println("Elapsed time (ms): "+watch.elapsed().toMillis());
        System.out.println("Samples produced: "+metricGenerator.getNbInstances() * seconds * metricGenerator.getSamplesPerSecond());
    }

    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8428/api/v1/import/prometheus")
                .build();
    }

    private void sink(MetricSample metric) {
        String response = client.post()
                .uri(uri -> uri.queryParam("timestamp", metric.timestamp()).build())
                .bodyValue(metric.toPromExpositionFormat())
                .retrieve()
                .onStatus(status -> status.is5xxServerError(), status -> Mono.just(new RuntimeException(status+" error!")))
                .onStatus(status -> status.is4xxClientError(), status -> Mono.just(new RuntimeException(status+" error!")))
                .bodyToMono(String.class)
                .doOnError(t -> t.printStackTrace())
                .block();
        counter.incrementAndGet();
    }

    @Override
    public void sink(List<MetricSample> samples) {
        samples.forEach(this::sink);
    }

    public static void main(String[] args) {
        new VictoriaSink();
    }
}
