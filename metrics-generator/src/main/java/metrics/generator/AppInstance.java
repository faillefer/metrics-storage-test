package metrics.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
class AppInstance implements Runnable {
    private String instanceName;
    private String metricName;
    private MetricSink metricSink;
    private int nbProjects;
    private int nbAccounts;
    private int nbDevices;
    private long startTime;
    private long stopTime;
    private int samplesPerSecond;
    private boolean bulk;
    private transient final Random random = new Random();

    @Override
    public void run() {
        trace(String.format("[%s] starting", instanceName));
        for(long time = startTime; time < stopTime; time = time + 1000) {
            List<MetricSample> samplesOnSecond = generateSamples(samplesPerSecond, time, time + 1000);
            metricSink.sink(samplesOnSecond);
//            if(time % 60000 ==0) {
//                trace(String.format("%s : %s", instanceName, time));
//            }
        }
//        trace(String.format("[%s] stopping", instanceName));
    }

    private List<MetricSample> generateSamples(int nbSamples, long startTimeInclusive, long endTimeExclusive){
        List<MetricSample> samples = new ArrayList<>(nbSamples);
        for(int i = 0; i < nbSamples; i++) {
            samples.add(generateSample(random.nextLong(startTimeInclusive, endTimeExclusive)));
        }
        return samples;
    }

    private MetricSample generateSample(long timestamp){
        return MetricSample.builder()
                .metricName(metricName)
                .value(1.0)
                .timestamp(timestamp)
                .labels(randomLabels())
                .build();
    }

    private Map<String, String> randomLabels() {
        int accountId = random.nextInt(nbAccounts);
        int projectId = getRandom().nextInt(accountId * nbProjects, (accountId + 1 ) * nbProjects);
        int deviceId = getRandom().nextInt(projectId * nbDevices, (projectId + 1 ) * nbDevices);
        return Map.of("account", "account-"+accountId,
                "project", "project-"+projectId
//                "device", "device-"+deviceId,
//                "instance", instanceName
            );
    }

    private void trace(String message) {
        System.out.println(message);
    }
}
