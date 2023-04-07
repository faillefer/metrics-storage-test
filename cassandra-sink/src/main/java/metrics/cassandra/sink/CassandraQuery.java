package metrics.cassandra.sink;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class CassandraQuery {

    public static void main(String[] args) {
        CassandraConnector cassandra = new CassandraConnector();

        cassandra.connect("127.0.0.1", 9042, "metrics");

        ResultSet resultSet = cassandra.execute("SELECT * from uplink limit 1");
        printSample(resultSet.one());

        resultSet = cassandra.execute("SELECT sum(value) from uplink where account ='account-8'");
        System.out.println("sum: "+resultSet.one().getLong(0));

        querySumForAccounts(cassandra);
        cassandra.close();
    }

    private static void querySumForAccounts(CassandraConnector cassandra){
        ResultSet resultSet = cassandra.execute("SELECT account, sum(value) from uplink group by account");
        for(Row row : resultSet.all()){
            System.out.println(row.getString(0) + " : " +row.getLong(1));
        }
    }

    private static void printSample(Row row) {
        StringBuilder sb = new StringBuilder();
        sb.append("uplink");
        sb.append("{account=").append(row.getString("account")).append(", ");
        sb.append("project=").append(row.getString("project")).append("} ");
        sb.append(row.getLong("value")).append(" ");
        sb.append(row.getTimestamp("metricTime"));
        System.out.println(sb.toString());
    }
}
