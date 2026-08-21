package com.example.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

/**
 * Section 4.1.2: Buffered Streams & System Call Performance Benchmarking.
 * 
 * Demonstrates:
 * - Unbuffered vs Buffered Streams (BufferedInputStream, BufferedOutputStream).
 * - BufferedReader / BufferedWriter line-by-line reading & explicit flushing.
 * - Syscall Reduction Benchmarking (Byte-by-Byte vs 8KB Buffered I/O).
 */
public class BufferedStreamsBenchmarkDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 4.1.2 BUFFERED STREAMS & SYSCALL OPTIMIZATION BENCHMARK");
        System.out.println("------------------------------------------------------------------------");

        File tempDir = new File("temp_buffered_demo");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File srcFile = new File(tempDir, "large_source.bin");
        File unbufferedDest = new File(tempDir, "unbuffered_copy.bin");
        File bufferedDest = new File(tempDir, "buffered_copy.bin");
        File textLogFile = new File(tempDir, "server_app.log");

        try {
            // Generate a 2MB dummy binary file
            int payloadSizeBytes = 2 * 1024 * 1024; // 2 Megabytes
            byte[] dummyData = new byte[payloadSizeBytes];
            for (int i = 0; i < dummyData.length; i++) {
                dummyData[i] = (byte) (i % 128);
            }

            try (FileOutputStream fos = new FileOutputStream(srcFile)) {
                fos.write(dummyData);
            }
            System.out.printf("Generated dummy payload file: %s (%.2f MB)\n", srcFile.getName(), payloadSizeBytes / (1024.0 * 1024.0));

            // 1. Unbuffered Byte-by-Byte Copy Benchmark
            System.out.println("\n--- 1. Benchmark: Unbuffered Byte-by-Byte Copying ---");
            long startUnbuffered = System.currentTimeMillis();
            long unbufferedBytes = 0;
            try (FileInputStream fis = new FileInputStream(srcFile);
                 FileOutputStream fos = new FileOutputStream(unbufferedDest)) {
                int b;
                while ((b = fis.read()) != -1) { // Triggers 1 OS read syscall per byte!
                    fos.write(b);                 // Triggers 1 OS write syscall per byte!
                    unbufferedBytes++;
                }
            }
            long timeUnbuffered = System.currentTimeMillis() - startUnbuffered;
            System.out.printf("  Unbuffered Copy Time : %d ms (Total Bytes: %d)\n", timeUnbuffered, unbufferedBytes);
            System.out.printf("  Estimated OS Syscalls: %,d read() + %,d write() syscalls!\n", unbufferedBytes, unbufferedBytes);

            // 2. Buffered Stream Copy Benchmark (8KB Default Internal Buffer)
            System.out.println("\n--- 2. Benchmark: Buffered Stream Copying (8KB Internal Buffer) ---");
            long startBuffered = System.currentTimeMillis();
            long bufferedBytes = 0;
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile));
                 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(bufferedDest))) {
                int b;
                while ((b = bis.read()) != -1) { // Reads from 8KB internal RAM buffer (only 1 syscall per 8192 bytes!)
                    bos.write(b);                 // Writes to 8KB internal RAM buffer
                    bufferedBytes++;
                }
                bos.flush(); // Flush remaining bytes to disk
            }
            long timeBuffered = System.currentTimeMillis() - startBuffered;
            System.out.printf("  Buffered Copy Time   : %d ms (Total Bytes: %d)\n", timeBuffered, bufferedBytes);
            System.out.printf("  Estimated OS Syscalls: ~%,d read() + ~%,d write() syscalls (Reduced by 8192x!)\n", 
                    payloadSizeBytes / 8192, payloadSizeBytes / 8192);

            double speedup = timeUnbuffered > 0 ? (double) timeUnbuffered / Math.max(1, timeBuffered) : 1.0;
            System.out.printf("🚀 Speedup Ratio       : %.2fx Faster using Buffered Streams!\n", speedup);

            // 3. BufferedReader & BufferedWriter Line-Based Text I/O
            System.out.println("\n--- 3. BufferedReader & BufferedWriter Line-Based Log Processing ---");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(textLogFile, StandardCharsets.UTF_8))) {
                writer.write("2026-08-21 10:00:00 [INFO] Server started on port 8080");
                writer.newLine();
                writer.write("2026-08-21 10:00:05 [WARN] High memory usage detected: 85%");
                writer.newLine();
                writer.write("2026-08-21 10:00:10 [ERROR] Connection timeout to database cluster");
                writer.newLine();
                writer.flush(); // Explicit flush
            }

            System.out.println("Reading log lines using BufferedReader.readLine():");
            try (BufferedReader reader = new BufferedReader(new FileReader(textLogFile, StandardCharsets.UTF_8))) {
                String line;
                int lineNum = 1;
                while ((line = reader.readLine()) != null) {
                    System.out.printf("  Line %d: %s\n", lineNum++, line);
                }
            }

        } catch (Exception e) {
            System.err.println("Buffered Demo Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up demo files
            srcFile.delete();
            unbufferedDest.delete();
            bufferedDest.delete();
            textLogFile.delete();
            tempDir.delete();
        }

        System.out.println("\n💡 SRE Performance Insight:");
        System.out.println("   Unbuffered I/O triggers context switches into the OS kernel for every single byte,");
        System.out.println("   causing immense CPU overhead. ALWAYS wrap file/network streams in BufferedInputStream/BufferedReader!");
    }
}
