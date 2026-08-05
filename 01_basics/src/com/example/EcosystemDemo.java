package com.example;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * Phase 1.1: Java Ecosystem & Runtime Architecture
 * Topic: JDK vs JRE vs JVM Inspection & SRE Observability Basics
 */
public class EcosystemDemo {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("☕ Phase 1.1: Java Ecosystem & Runtime Architecture");
        System.out.println("==========================================================");

        // 1. JDK vs JRE vs JVM Concept Demonstration
        System.out.println("\n1️⃣  JDK, JRE & JVM Concept Summary:");
        System.out.println("   • JVM (Java Virtual Machine) : Executes bytecode (.class files) & manages Heap memory.");
        System.out.println("   • JRE (Java Runtime Environment) : JVM + Core Class Libraries (java.lang, java.util).");
        System.out.println("   • JDK (Java Development Kit)  : JRE + Compiler (javac), Archiver (jar), Profiler (jcmd).");

        // 2. Inspect Runtime Environment Properties
        System.out.println("\n2️⃣  Current System & JVM Properties:");
        System.out.println("   • Java Version    : " + System.getProperty("java.version"));
        System.out.println("   • Java Runtime    : " + System.getProperty("java.runtime.name"));
        System.out.println("   • JVM Name        : " + System.getProperty("java.vm.name"));
        System.out.println("   • JVM Version     : " + System.getProperty("java.vm.version"));
        System.out.println("   • JVM Vendor      : " + System.getProperty("java.vm.vendor"));
        System.out.println("   • ClassPath       : " + System.getProperty("java.class.path"));

        // 3. Senior SRE View: JVM Flags & Operating Environment
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        List<String> vmArgs = runtimeBean.getInputArguments();

        System.out.println("\n3️⃣  Senior SRE Runtime Observability:");
        System.out.println("   • Available Cores : " + Runtime.getRuntime().availableProcessors());
        System.out.println("   • Max Memory Heap : " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MB");
        System.out.println("   • JVM Arguments   : " + (vmArgs.isEmpty() ? "None (Default settings)" : vmArgs));

        System.out.println("\n==========================================================");
        System.out.println("✅ Executed successfully from packaged bytecode!");
        System.out.println("==========================================================");
    }
}
