package metrics.cassandra.sink;

public class TestUpdate {

    public static void main(String[] args) throws Exception {
        CassandraConnector cassandra = new CassandraConnector();

        cassandra.connect("127.0.0.1", 9042, "metrics");

        cassandra.updateCounter("uplink", "a", "p", 1000, 5);
        cassandra.close();

    }

}
