package metrics.bigtable.sink;

import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.models.Query;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.cloud.bigtable.data.v2.models.RowCell;
import com.google.common.primitives.Longs;

public class BigTableConnection {

    public static void main(String[] args) throws Exception {
        // Instantiates a client
        String projectId = "sigfox-sandbox";
        String instanceId = "poc-metrics";
        String tableId = "uplink";

        // Create the client.
        // Please note that creating the client is a very expensive operation
        // and should only be done once and shared in an application.
        BigtableDataClient dataClient = BigtableDataClient.create(projectId, instanceId);

//        for(int i = 1 ; i <= 10; i++ ){
//            dataClient.mutateRow(
//                    RowMutation.create(tableId, "uplink#AAAB#ZZZ")
//                            .setCell("family-1", "count", i)
//            );
//        }
//
//        for(int i = 1 ; i <= 10; i++ ){
//            ReadModifyWriteRow mutation =
//                    ReadModifyWriteRow.create(tableId, "downlink#AAAB#ZZZ")
//                            .increment("family-1", "connected_cell", 1);
//            Row success = dataClient.readModifyWriteRow(mutation);
//            System.out.printf(
//                    "Successfully updated row %s", success.getKey().toStringUtf8());
//        }

        try {

            // Query a table
            Query query = Query.create(tableId).prefix("account-1#project-1");

            for (Row row : dataClient.readRows(query)) {
                System.out.println(row.getKey().toStringUtf8());
                for(RowCell cell : row.getCells()){

                    System.out.println("\t" + cell.getTimestamp() + " - " + cell.getQualifier().toStringUtf8() + ": " + Longs.fromByteArray(cell.getValue().toByteArray()));
                }
            }
        } finally {
            dataClient.close();
        }
    }
}
