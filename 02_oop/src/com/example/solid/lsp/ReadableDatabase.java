package com.example.solid.lsp;

public interface ReadableDatabase {
    String executeQuery(String sql);
    String getDatabaseUri();
}
