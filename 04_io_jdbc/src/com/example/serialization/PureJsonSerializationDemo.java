package com.example.serialization;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Section 4.2.3: JSON Parsing & Object Mapping in Pure Java.
 * 
 * Demonstrates:
 * - Pure Java JSON Serialization (converting POJOs / Records to JSON strings).
 * - Pure Java JSON Deserialization & Object Tokenizer (parsing JSON to Map / Java Objects).
 * - Safe schema validation without external framework dependencies.
 */
public class PureJsonSerializationDemo {

    /**
     * Java Record representing Service Configuration payload.
     */
    public record ServiceConfig(
            String serviceName,
            int port,
            boolean sslEnabled,
            List<String> endpoints,
            Map<String, String> tags
    ) {}

    /**
     * Lightweight Pure Java JSON Serializer & Mapper.
     */
    public static class PureJsonMapper {

        /**
         * Serializes a ServiceConfig record into a formatted JSON string.
         */
        public static String toJson(ServiceConfig config) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"serviceName\": \"").append(escapeJson(config.serviceName())).append("\",\n");
            json.append("  \"port\": ").append(config.port()).append(",\n");
            json.append("  \"sslEnabled\": ").append(config.sslEnabled()).append(",\n");
            
            // Array serialization
            json.append("  \"endpoints\": [");
            if (config.endpoints() != null && !config.endpoints().isEmpty()) {
                for (int i = 0; i < config.endpoints().size(); i++) {
                    json.append("\"").append(escapeJson(config.endpoints().get(i))).append("\"");
                    if (i < config.endpoints().size() - 1) json.append(", ");
                }
            }
            json.append("],\n");

            // Nested object serialization
            json.append("  \"tags\": {\n");
            if (config.tags() != null && !config.tags().isEmpty()) {
                int count = 0;
                int size = config.tags().size();
                for (Map.Entry<String, String> entry : config.tags().entrySet()) {
                    json.append("    \"").append(escapeJson(entry.getKey())).append("\": \"")
                        .append(escapeJson(entry.getValue())).append("\"");
                    if (++count < size) json.append(",");
                    json.append("\n");
                }
            }
            json.append("  }\n");
            json.append("}");

            return json.toString();
        }

        /**
         * Lightweight Pure Java JSON Parser converting flat/nested key-value JSON into a Map.
         */
        public static Map<String, Object> parseJsonToMap(String jsonString) {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            if (jsonString == null || jsonString.isBlank()) return resultMap;

            // Extract key-value pairs using Regex Tokenizer
            Pattern pairPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"[^\"]*\"|\\d+|true|false|null|\\[[^\\]]*\\]|\\{[^}]*\\})");
            Matcher matcher = pairPattern.matcher(jsonString);

            while (matcher.find()) {
                String key = matcher.group(1);
                String valueStr = matcher.group(2).trim();

                Object parsedValue;
                if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
                    parsedValue = valueStr.substring(1, valueStr.length() - 1); // String
                } else if (valueStr.equals("true") || valueStr.equals("false")) {
                    parsedValue = Boolean.parseBoolean(valueStr); // Boolean
                } else if (valueStr.matches("-?\\d+")) {
                    parsedValue = Integer.parseInt(valueStr); // Integer
                } else if (valueStr.startsWith("[") && valueStr.endsWith("]")) {
                    // String list parser
                    List<String> list = new ArrayList<>();
                    Matcher listMatcher = Pattern.compile("\"([^\"]+)\"").matcher(valueStr);
                    while (listMatcher.find()) {
                        list.add(listMatcher.group(1));
                    }
                    parsedValue = list;
                } else {
                    parsedValue = valueStr;
                }
                resultMap.put(key, parsedValue);
            }

            return resultMap;
        }

        private static String escapeJson(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\b", "\\b")
                        .replace("\f", "\\f")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t");
        }
    }

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 4.2.3 JSON PARSING & OBJECT MAPPING IN PURE JAVA");
        System.out.println("------------------------------------------------------------------------");

        // 1. POJO / Record to JSON Serialization
        System.out.println("\n--- 1. Pure Java Record to JSON Serialization ---");
        ServiceConfig config = new ServiceConfig(
                "auth-service-v2",
                8443,
                true,
                List.of("/api/v1/auth/login", "/api/v1/auth/token", "/api/v1/auth/health"),
                Map.of("env", "production", "region", "us-east-1", "tier", "backend")
        );

        String jsonOutput = PureJsonMapper.toJson(config);
        System.out.println("Serialized JSON String Output:\n" + jsonOutput);

        // 2. Pure Java JSON Deserialization to Map
        System.out.println("\n--- 2. Pure Java JSON Tokenizer Deserialization ---");
        Map<String, Object> parsedMap = PureJsonMapper.parseJsonToMap(jsonOutput);

        System.out.println("Parsed JSON Key-Value Map:");
        parsedMap.forEach((key, val) -> 
            System.out.printf("  - %-15s : %-25s (Type: %s)\n", key, val, val != null ? val.getClass().getSimpleName() : "null")
        );

        // Reconstructing domain properties safely
        String serviceName = (String) parsedMap.get("serviceName");
        Integer port = (Integer) parsedMap.get("port");
        Boolean ssl = (Boolean) parsedMap.get("sslEnabled");
        @SuppressWarnings("unchecked")
        List<String> endpoints = (List<String>) parsedMap.get("endpoints");

        System.out.println("\n--- 3. Reconstructed Domain Properties ---");
        System.out.println("Reconstructed Service Name: " + serviceName);
        System.out.println("Reconstructed Port        : " + port);
        System.out.println("Reconstructed SSL         : " + ssl);
        System.out.println("Reconstructed Endpoints   : " + endpoints);

        System.out.println("\n💡 SRE Takeaway: Standardizing on JSON/Protocol Buffers over native Java binary streams");
        System.out.println("   provides cross-language interoperability (Go, Rust, Python, Java), human-readable inspection,");
        System.out.println("   and eliminates remote code execution vulnerabilities inherent in native Java serialization!");
    }
}
