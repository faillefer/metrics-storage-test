package metrics.bigtable.sink;

import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest;

public class CreateTable {

    public static void main(String[] args) throws Exception {
        // gcloud auth application-default login
        // Credentials saved to file: [<home>/.config/gcloud/application_default_credentials.json]
        // Instantiates a client
        String projectId = "sigfox-sandbox";
        String instanceId = "poc-metrics";
        String tableId = "uplink";

        BigtableTableAdminClient tableAdminClient = BigtableTableAdminClient
                .create(projectId, instanceId);

        try {
            tableAdminClient.createTable(
                    CreateTableRequest.of(tableId)
                            .addFamily("v")
            );
        } finally {
            tableAdminClient.close();
        }
    }
}
