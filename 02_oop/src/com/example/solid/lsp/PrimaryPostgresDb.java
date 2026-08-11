package com.example.solid.lsp;

public class PrimaryPostgresDb implements WritableDatabase {

    private final String connectionUri;

    public PrimaryPostgresDb(String connectionUri) {
        this.connectionUri = connectionUri;
    }

    @Override
    public String executeQuery(String sql) {
        System.out.printf("      📖 [Primary PG] Executing READ query on %s: %s%n", connectionUri, sql);
        return "ResultSet{rows=15}";
    }

    @Override
    public void executeWrite(String sql) {
        System.out.printf("      ✏️ [Primary PG] Executing WRITE transaction on %s: %s%n", connectionUri, sql);
    }

    @Override
    public String getDatabaseUri() {
        return connectionUri;
    }
}
