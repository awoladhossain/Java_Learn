package com.example.solid.isp;

public interface Deployable {
    void deploy(String version);
    void rollback();
}
