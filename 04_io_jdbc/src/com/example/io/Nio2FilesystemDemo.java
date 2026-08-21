package com.example.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

/**
 * Section 4.1.3: Java NIO.2 (New I/O Filesystem API).
 * 
 * Demonstrates:
 * - Path operations (Path.of, normalize, resolve, relativize).
 * - Files utility methods (createDirectories, writeString, readString, copy, move, deleteIfExists).
 * - Memory-Efficient Stream Processing (Files.lines for giant log files).
 * - File System Traversal (Files.walk, Files.find) & Metadata inspection.
 */
public class Nio2FilesystemDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 4.1.3 JAVA NIO.2 (Path, Paths, Files, FileSystem)");
        System.out.println("------------------------------------------------------------------------");

        Path baseDir = Path.of("temp_nio2_demo");
        Path logsDir = baseDir.resolve("logs/nested/app");
        Path mainLogPath = logsDir.resolve("server_audit.log");
        Path backupLogPath = logsDir.resolve("server_audit.log.bak");

        try {
            // 1. Path Manipulation & Normalization
            System.out.println("\n--- 1. Path Operations (Path.of, normalize, resolve) ---");
            Path redundantPath = Path.of("temp_nio2_demo/./logs/../logs/nested/app/server_audit.log");
            System.out.println("Redundant Path  : " + redundantPath);
            System.out.println("Normalized Path : " + redundantPath.normalize());
            System.out.println("Absolute Path   : " + mainLogPath.toAbsolutePath());
            System.out.println("Parent Directory: " + mainLogPath.getParent());

            // 2. Directory Creation & File Writes using Files
            System.out.println("\n--- 2. Directory & File Manipulation (Files.createDirectories, writeString) ---");
            Files.createDirectories(logsDir);
            System.out.println("Created directory hierarchy: " + logsDir);

            List<String> logEntries = List.of(
                    "2026-08-21 12:00:01.102 [INFO] [req-101] GET /api/v1/health 200 2ms",
                    "2026-08-21 12:00:02.450 [ERROR] [req-102] POST /api/v1/pay 500 450ms - Connection Timeout",
                    "2026-08-21 12:00:03.881 [INFO] [req-103] GET /api/v1/users 200 12ms",
                    "2026-08-21 12:00:05.120 [WARN] [req-104] GET /api/v1/metrics 429 15ms - Rate Limit Exceeded",
                    "2026-08-21 12:00:06.900 [ERROR] [req-105] PUT /api/v1/account 503 1200ms - Out of Memory"
            );

            Files.write(mainLogPath, logEntries, StandardCharsets.UTF_8, 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Wrote " + logEntries.size() + " audit log entries to " + mainLogPath.getFileName());

            // 3. Memory-Efficient Large Log Searching with Files.lines()
            System.out.println("\n--- 3. Memory-Efficient Stream Log Inspection (Files.lines) ---");
            System.out.println("Filtering ERROR log lines via Files.lines() Stream:");
            try (Stream<String> lineStream = Files.lines(mainLogPath, StandardCharsets.UTF_8)) {
                lineStream.filter(line -> line.contains("[ERROR]"))
                          .forEach(errorLine -> System.out.println("  🚨 " + errorLine));
            }

            // 4. File Copy & Move Operations
            System.out.println("\n--- 4. File Atomic Copy & Metadata Inspection ---");
            Files.copy(mainLogPath, backupLogPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copied log file to backup: " + backupLogPath.getFileName());

            BasicFileAttributes attr = Files.readAttributes(mainLogPath, BasicFileAttributes.class);
            System.out.println("File Size        : " + attr.size() + " bytes");
            System.out.println("Creation Time    : " + attr.creationTime());
            System.out.println("Last Modified    : " + attr.lastModifiedTime());
            System.out.println("Is Regular File? : " + attr.isRegularFile());

            // 5. FileSystem Directory Walk & Find
            System.out.println("\n--- 5. File System Traversal (Files.walk & Files.find) ---");
            System.out.println("Walking directory tree from baseDir:");
            try (Stream<Path> pathStream = Files.walk(baseDir)) {
                pathStream.forEach(path -> System.out.println("  " + baseDir.relativize(path)));
            }

            System.out.println("\nFinding all '.log' files under baseDir:");
            try (Stream<Path> findStream = Files.find(baseDir, 5, 
                    (path, attributes) -> path.toString().endsWith(".log") && attributes.isRegularFile())) {
                findStream.forEach(p -> System.out.println("  Found Log: " + p.getFileName() + " (" + p + ")"));
            }

            // Default FileSystem inspection
            System.out.println("\nDefault FileSystem Separator: '" + FileSystems.getDefault().getSeparator() + "'");

        } catch (Exception e) {
            System.err.println("NIO.2 Demo Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up created files and directories
            cleanDirectoryTree(baseDir);
        }

        System.out.println("\n💡 SRE Memory Golden Rule:");
        System.out.println("   NEVER use Files.readAllLines() on large production log files (multi-GB); it loads the entire file into Heap!");
        System.out.println("   ALWAYS use Files.lines(path) which streams lines lazily using a BufferedReader behind the scenes.");
    }

    private static void cleanDirectoryTree(Path root) {
        if (!Files.exists(root)) return;
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted((p1, p2) -> p2.compareTo(p1)) // Reverse order so files deleted before parent dirs
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
        } catch (IOException ignored) {}
    }
}
