package com.example.solid.lsp;

public interface WritableDatabase extends ReadableDatabase {
    void executeWrite(String sql);
}
