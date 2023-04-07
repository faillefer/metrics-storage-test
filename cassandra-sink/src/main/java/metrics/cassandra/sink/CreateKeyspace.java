package metrics.cassandra.sink;

public class CreateKeyspace {

    public static void main(String[] args) throws Exception {
        CassandraConnector cassandra = new CassandraConnector();
        cassandra.connect("127.0.0.1", 9042);
        String keyspaceName = "metrics";
        cassandra.createKeyspace(keyspaceName, "SimpleStrategy", 1);

        cassandra.connect("127.0.0.1", 9042, "metrics");
        cassandra.createTable("uplink");
    }

}
