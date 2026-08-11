package com.example.solid.lsp;

/**
 * LSP Compliance:
 * ReadReplicaPostgresDb ONLY implements ReadableDatabase.
 * It does NOT implement WritableDatabase and does NOT throw UnsupportedOperationException at runtime.
 * Any method taking ReadableDatabase can accept ReadReplicaPostgresDb without risk of unexpected errors.
 */
public class ReadReplicaPostgresDb implements ReadableDatabase {

    private final String connectionUri;

    public ReadReplicaPostgresDb(String connectionUri) {
        this.connectionUri = connectionUri;
    }

    @Override
    public String executeQuery(String sql) {
        System.out.printf("      📖 [Read Replica] Executing READ query on %s: %s%n", connectionUri, sql);
        return "ResultSet{rows=15, source='read_replica'}";
    }

    @Override
    public String getDatabaseUri() {
        return connectionUri;
    }
}
