package metrics.cassandra.sink;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

public class CassandraConnector {
    private Cluster cluster;
    private Session session;

    public void connect(String node, Integer port, String keyspace) {
        Cluster.Builder b = Cluster.builder().withoutJMXReporting().addContactPoint(node);
        if (port != null) {
            b.withPort(port);
        }
        cluster = b.build();
        session = cluster.connect(keyspace);
    }
    public void connect(String node, Integer port) {
        Cluster.Builder b = Cluster.builder().withoutJMXReporting().addContactPoint(node);
        if (port != null) {
            b.withPort(port);
        }
        cluster = b.build();
        session = cluster.connect();
    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        session.close();
        cluster.close();
    }

    public void createKeyspace(
            String keyspaceName, String replicationStrategy, int replicationFactor) {
        StringBuilder sb =
                new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
                        .append(keyspaceName).append(" WITH replication = {")
                        .append("'class':'").append(replicationStrategy)
                        .append("','replication_factor':").append(replicationFactor)
                        .append("};");

        String query = sb.toString();
        session.execute(query);
        System.out.println("Keyspace created");
    }

    public void createTable(String tableName) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(tableName).append("(")
                .append("account text,")
                .append("project text,")
                .append("metricTime timestamp,")
                .append("value counter,")
                .append("PRIMARY KEY (account, project, metricTime));");

        String query = sb.toString();
        session.execute(query);

        System.out.println("Table created");
    }

    public void updateCounter(String tableName, String account, String project, long time, long value){
        StringBuilder sb = new StringBuilder("UPDATE ")
                .append(tableName)//.append("(account, project, metricTime, value) ")
                .append(" SET value = value + ").append(value)
                .append(" WHERE ")
                .append("account = '").append(account).append("' AND ")
                .append("project = '").append(project).append("' AND ")
                .append("metricTime = ").append(time)
                .append(";");
        //System.out.println(sb.toString());
        String query = sb.toString();
        session.execute(query);
    }

    public ResultSet execute(String query) {
        return session.execute(query);
    }
}
