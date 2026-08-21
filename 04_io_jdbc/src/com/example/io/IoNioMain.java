package com.example.io;

/**
 * Main Runner Class for Phase 4.1: Java I/O & NIO.2 (New I/O) Deep-Dive.
 * 
 * Executes comprehensive demonstrations covering:
 * - 4.1.1 Byte Streams vs Character Streams (InputStream, OutputStream, Reader, Writer, UTF-8 multi-byte decoding).
 * - 4.1.2 Buffered Streams & Syscall Optimization (BufferedReader, BufferedWriter, performance benchmarking).
 * - 4.1.3 Java NIO.2 Filesystem API (Path, Paths, Files, Files.lines stream processing, directory traversal).
 * - 4.1.4 Non-Blocking I/O Concepts (Channels, Direct/Heap ByteBuffers, Zero-Copy transferTo, Selectors).
 */
public class IoNioMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ PHASE 4.1: JAVA I/O & NIO.2 (NEW I/O) DEEP-DIVE DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Byte Streams vs Character Streams
        ByteVsCharacterStreamsDemo.runDemo();

        // 2. Buffered Streams & Syscall Reduction Benchmark
        BufferedStreamsBenchmarkDemo.runDemo();

        // 3. Java NIO.2 Path & Files API
        Nio2FilesystemDemo.runDemo();

        // 4. Non-Blocking I/O (Channels, Buffers, Selectors)
        NonBlockingChannelsBuffersDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 4.1 JAVA I/O & NIO.2 EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
