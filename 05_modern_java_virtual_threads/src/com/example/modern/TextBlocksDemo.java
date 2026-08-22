package com.example.modern;

/**
 * Section 5.3.4: Text Blocks & Modern String Formatting (Java 15+).
 * 
 * Demonstrates:
 * - Multiline String Literals (""" ... """).
 * - Incidental Whitespace Stripping & Indentation Rules.
 * - Escape Sequences: \ (Line Continuation), \s (Explicit Space Preservation).
 * - String Methods: .formatted(), .stripIndent(), .translateEscapes().
 * - Real-World SRE Templates: JSON API payloads, SQL Queries, Kubernetes Manifests.
 */
public class TextBlocksDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 5.3.4 TEXT BLOCKS & MODERN STRING TEMPLATING (\"\"\" ... \"\"\")");
        System.out.println("------------------------------------------------------------------------");

        // ==========================================
        // 1. Basic Text Block & Incidental Whitespace
        // ==========================================
        System.out.println("\n--- 1. Basic Text Block vs Legacy Concatenation ---");

        // Legacy multiline string concatenation
        String legacyJson = "{\n" +
                            "  \"service\": \"payment-gateway\",\n" +
                            "  \"status\": \"UP\",\n" +
                            "  \"port\": 8080\n" +
                            "}";

        // Modern Java Text Block
        String modernJson = """
                {
                  "service": "payment-gateway",
                  "status": "UP",
                  "port": 8080
                }
                """;

        System.out.println("Modern Text Block JSON:");
        System.out.println(modernJson);

        // ==========================================
        // 2. Escape Sequences (\ line continuation, \s space preservation)
        // ==========================================
        System.out.println("--- 2. Escape Sequences (\\ and \\s) ---");

        // \ (Line continuation) suppresses the newline character, keeping long text on 1 line
        String singleLineSql = """
                SELECT u.id, u.username, u.email \
                FROM users u \
                WHERE u.status = 'ACTIVE' \
                ORDER BY u.created_at DESC \
                LIMIT 10;
                """;

        System.out.println("Single Line SQL (using \\ line continuation):");
        System.out.println(singleLineSql);

        // \s preserves trailing whitespace at the end of a line
        String paddedText = """
                Line 1   \s
                Line 2   \s
                """;

        System.out.println("Padded Text line length check: " + paddedText.lines().findFirst().orElse("").length());

        // ==========================================
        // 3. Modern Formatting with .formatted()
        // ==========================================
        System.out.println("\n--- 3. Dynamic Templating via .formatted() ---");

        String serviceName = "auth-microservice";
        String namespace = "prod-banking";
        int replicas = 3;
        String containerImage = "docker.internal/banking/auth:v2.4.1";

        // Kubernetes YAML Manifest Template using Text Blocks + .formatted()
        String k8sDeploymentYaml = """
                apiVersion: apps/v1
                kind: Deployment
                metadata:
                  name: %s
                  namespace: %s
                  labels:
                    app: %s
                spec:
                  replicas: %d
                  selector:
                    matchLabels:
                      app: %s
                  template:
                    metadata:
                      labels:
                        app: %s
                    spec:
                      containers:
                      - name: %s
                        image: %s
                        ports:
                        - containerPort: 8080
                """.formatted(serviceName, namespace, serviceName, replicas, serviceName, serviceName, serviceName, containerImage);

        System.out.println("Generated Kubernetes Deployment Manifest:");
        System.out.println(k8sDeploymentYaml);

        // ==========================================
        // 4. Utility Methods (.stripIndent(), .translateEscapes())
        // ==========================================
        System.out.println("--- 4. Text Block Utility Methods ---");

        String indentedCode = "    public void hello() {\n        System.out.println(\"Hello\");\n    }";
        String strippedCode = indentedCode.stripIndent();

        System.out.println("Original Indented String:\n" + indentedCode);
        System.out.println("After .stripIndent():\n" + strippedCode);

        String rawEscaped = "Line1\\nLine2\\tTabbed";
        String translated = rawEscaped.translateEscapes();
        System.out.println("Translated Escapes Result:\n" + translated);

        System.out.println("\n💡 SRE Insight: Text Blocks dramatically improve code readability and maintainability when writing embedded SQL queries,");
        System.out.println("   JSON payloads, and Cloud-Native infrastructure configs (Kubernetes YAML, Terraform JSON) directly in Java code.");
    }
}
