package com.example;

import java.util.Arrays;
import java.util.List;

/**
 * Phase 1.3: Control Flow & Operators
 * 
 * Topics Covered:
 * 1. Arithmetic, Logical, Bitwise (&, |, ^, ~, <<, >>, >>>), and Ternary Operators
 * 2. Short-Circuit Evaluation Dynamics vs Non-Short-Circuit Operations
 * 3. Decision Making: if-else, Traditional Switch, & Modern Switch Expressions (Java 14+)
 * 4. Iteration Constructs: for, while, do-while, enhanced for-each, & Labeled Control Statements
 * 5. Senior SRE Insights: Branch Prediction, Bitfield Operations, & Spin-Wait Loops
 */
public class ControlFlowAndOperatorsDemo {

    // Status flags for SRE bitmasking demonstration
    private static final int FLAG_SERVICE_ACTIVE   = 1 << 0; // 0001 (1)
    private static final int FLAG_DB_CONNECTED     = 1 << 1; // 0010 (2)
    private static final int FLAG_CACHE_READY      = 1 << 2; // 0100 (4)
    private static final int FLAG_METRICS_ENABLED  = 1 << 3; // 1000 (8)

    public enum SystemState {
        INITIALIZING,
        HEALTHY,
        DEGRADED,
        CRITICAL,
        MAINTENANCE
    }

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("☕ Phase 1.3: Control Flow & Operators Deep Dive");
        System.out.println("========================================================================");

        demonstrateOperators();
        demonstrateBitwiseOperatorsAndMasking();
        demonstrateDecisionMaking();
        demonstrateModernSwitchExpressions();
        demonstrateIterationConstructs();
        demonstrateSreControlFlowInsights();

        System.out.println("\n========================================================================");
        System.out.println("✅ Phase 1.3 Execution Completed Successfully!");
        System.out.println("========================================================================");
    }

    /**
     * 1️⃣ Arithmetic, Logical, and Ternary Operators
     */
    private static void demonstrateOperators() {
        System.out.println("\n1️⃣  ARITHMETIC, LOGICAL & TERNARY OPERATORS:");
        System.out.println("------------------------------------------------------------------------");

        // A. Arithmetic Operators
        int a = 15;
        int b = 4;
        System.out.println("   📍 Arithmetic Operations (a = 15, b = 4):");
        System.out.println("      • Addition (a + b)        : " + (a + b));
        System.out.println("      • Subtraction (a - b)     : " + (a - b));
        System.out.println("      • Multiplication (a * b)  : " + (a * b));
        System.out.println("      • Integer Division (a / b): " + (a / b) + " (Truncates fractional part)");
        System.out.println("      • Floating Division (a / 4.0): " + (a / 4.0));
        System.out.println("      • Modulo / Remainder (a % b) : " + (a % b));

        // Prefix vs Postfix Increment/Decrement
        int count = 10;
        int preInc = ++count;  // Increment first, then return count (11)
        int postInc = count++; // Return count (11), then increment (12)
        System.out.printf("      • Prefix (++count) -> %d, Postfix (count++) -> %d, Final count -> %d%n",
                preInc, postInc, count);

        // B. Short-Circuit Logical Operators (&&, ||) vs Bitwise/Boolean (&, |)
        System.out.println("\n   📍 Logical & Short-Circuit Mechanics:");
        boolean sideEffectExecuted = false;

        // Short-circuit AND (&&): Evaluation stops because first operand is false
        boolean shortCircuitResult = (false && triggerSideEffect());
        System.out.println("      • (false && triggerSideEffect()) -> " + shortCircuitResult 
                + " [Side effect triggered? false]");

        // Non-short-circuit AND (&): BOTH operands evaluated regardless of first operand
        boolean nonShortCircuitResult = (false & triggerSideEffect());
        System.out.println("      • (false & triggerSideEffect())  -> " + nonShortCircuitResult 
                + " [Side effect triggered? TRUE!]");

        // C. Ternary Operator (? :)
        int latencyMs = 250;
        String SLA_Status = (latencyMs < 100) ? "EXCELLENT" : (latencyMs < 300) ? "ACCEPTABLE" : "SLA_BREACH";
        System.out.println("\n   📍 Ternary Operator:");
        System.out.println("      • Latency: " + latencyMs + "ms -> SLA Evaluation: " + SLA_Status);
    }

    private static boolean triggerSideEffect() {
        System.out.print(" <[SIDE-EFFECT EXECUTED]> ");
        return true;
    }

    /**
     * 2️⃣ Bitwise Operators (&, |, ^, ~, <<, >>, >>>) & Bitmasking
     */
    private static void demonstrateBitwiseOperatorsAndMasking() {
        System.out.println("\n2️⃣  BITWISE OPERATORS & BITMASKING:");
        System.out.println("------------------------------------------------------------------------");

        int x = 0b1010; // 10 decimal
        int y = 0b1100; // 12 decimal

        System.out.println("   📍 Bitwise Operations on x (10 / 0b1010) and y (12 / 0b1100):");
        System.out.printf("      • AND  (x & y)   : %2d | Binary: %4s%n", (x & y), Integer.toBinaryString(x & y));
        System.out.printf("      • OR   (x | y)   : %2d | Binary: %4s%n", (x | y), Integer.toBinaryString(x | y));
        System.out.printf("      • XOR  (x ^ y)   : %2d | Binary: %4s%n", (x ^ y), Integer.toBinaryString(x ^ y));
        System.out.printf("      • NOT  (~x)      : %2d | Binary: %32s (2's complement)%n", ~x, Integer.toBinaryString(~x));

        // Bit Shifts: Left Shift (<<), Signed Right Shift (>>), Unsigned Right Shift (>>>)
        int pos = 20;  // 0001 0100
        int neg = -20; // 1111 1111 1111 1111 1111 1111 1110 1100

        System.out.println("\n   📍 Bit Shift Operations:");
        System.out.println("      • Left Shift (20 << 2)          : " + (pos << 2) + " (Equivalent to 20 * 2^2)");
        System.out.println("      • Signed Right Shift (20 >> 2)   : " + (pos >> 2) + " (Equivalent to 20 / 2^2)");
        System.out.println("      • Signed Right Shift (-20 >> 2)  : " + (neg >> 2) + " (Preserves negative sign bit)");
        System.out.println("      • Unsigned Right Shift (-20 >>> 2): " + (neg >>> 2) + " (Fills MSB with 0s!)");

        // Practical SRE Application: Bitmasking for Zero-Allocation State Flags
        System.out.println("\n   📍 Practical SRE Application: System Status Bitmasking:");
        int systemStatus = 0; // All flags off

        // Set flags: Service Active & DB Connected
        systemStatus |= FLAG_SERVICE_ACTIVE;
        systemStatus |= FLAG_DB_CONNECTED;

        System.out.printf("      • Set Flags [ACTIVE | DB_CONNECTED]: Status Mask = 0b%s%n",
                Integer.toBinaryString(systemStatus));

        // Check if DB is Connected
        boolean isDbUp = (systemStatus & FLAG_DB_CONNECTED) != 0;
        boolean isCacheUp = (systemStatus & FLAG_CACHE_READY) != 0;
        System.out.println("      • Check DB Connected?   " + isDbUp);
        System.out.println("      • Check Cache Ready?    " + isCacheUp);

        // Toggle Metrics Flag
        systemStatus ^= FLAG_METRICS_ENABLED;
        System.out.printf("      • Toggle METRICS_ENABLED ON: Status Mask = 0b%s%n",
                Integer.toBinaryString(systemStatus));
    }

    /**
     * 3️⃣ Decision Making: if-else & Traditional Switch Statements
     */
    private static void demonstrateDecisionMaking() {
        System.out.println("\n3️⃣  DECISION MAKING: IF-ELSE & TRADITIONAL SWITCH:");
        System.out.println("------------------------------------------------------------------------");

        // A. If-Else Control Flow
        double cpuUsage = 87.5;
        System.out.println("   📍 If-Else Branching (CPU Usage: " + cpuUsage + "%):");
        if (cpuUsage > 90.0) {
            System.out.println("      • ACTION: Trigger Immediate PagerDuty Alert & Auto-scale!");
        } else if (cpuUsage > 80.0) {
            System.out.println("      • ACTION: Log Warning & Prepare Secondary Worker Nodes.");
        } else {
            System.out.println("      • ACTION: System Operating Within Normal Range.");
        }

        // B. Traditional Switch Statement with Intentional Fall-through
        String logLevel = "WARN";
        System.out.println("\n   📍 Traditional Switch Statement (Level: " + logLevel + " with Fall-Through Logging):");
        switch (logLevel) {
            case "DEBUG":
                System.out.println("      [DEBUG] Detailed trace enabled.");
            case "INFO":
                System.out.println("      [INFO] Operational metrics recorded.");
            case "WARN":
                System.out.println("      [WARN] Potential anomaly detected.");
                // Intentional missing break for fallback logging
            case "ERROR":
                System.out.println("      [ERROR] Event queued for diagnostic audit.");
                break;
            default:
                System.out.println("      [UNKNOWN] Log level unrecognised.");
                break;
        }
    }

    /**
     * 4️⃣ Modern Switch Expressions (Java 14+)
     */
    private static void demonstrateModernSwitchExpressions() {
        System.out.println("\n4️⃣  MODERN SWITCH EXPRESSIONS (Java 14+ Arrow Syntax & Yield):");
        System.out.println("------------------------------------------------------------------------");

        SystemState state = SystemState.DEGRADED;

        // A. Switch Expression returning value directly with Arrow Syntax -> (No fall-through risk!)
        String actionPlan = switch (state) {
            case INITIALIZING -> "Wait for readiness probe verification";
            case HEALTHY      -> "Route 100% incoming user traffic";
            case DEGRADED     -> "Shed non-critical payload & activate rate limiting";
            case CRITICAL     -> "Failover traffic to standby region immediately";
            case MAINTENANCE  -> "Drain active connections and return HTTP 503";
        };

        System.out.println("   📍 Switch Expression Result (State: " + state + "):");
        System.out.println("      • Action Plan: " + actionPlan);

        // B. Switch Expression with Block Syntax and 'yield' Keyword
        int httpStatusCode = 429;
        String responseCategory = switch (httpStatusCode) {
            case 200, 201, 204 -> "2xx Success";
            case 400, 401, 403, 404 -> "4xx Client Error";
            case 429 -> {
                System.out.println("      [Switch Block] Rate limit breach detected! Calculating retry-after delay...");
                yield "4xx Rate Limited (HTTP 429)";
            }
            case 500, 502, 503, 504 -> "5xx Server Error";
            default -> {
                yield "Unknown HTTP Status Code (" + httpStatusCode + ")";
            }
        };

        System.out.println("      • HTTP " + httpStatusCode + " Category: " + responseCategory);
    }

    /**
     * 5️⃣ Iteration Constructs: for, while, do-while, enhanced for-each & Labeled Control
     */
    private static void demonstrateIterationConstructs() {
        System.out.println("\n5️⃣  ITERATION CONSTRUCTS & LABELED CONTROL FLOW:");
        System.out.println("------------------------------------------------------------------------");

        // A. Standard For Loop
        System.out.print("   📍 Standard For Loop (Counting 1 to 5): ");
        for (int i = 1; i <= 5; i++) {
            System.out.print(i + (i < 5 ? ", " : "\n"));
        }

        // B. Enhanced For-Each Loop
        List<String> microservices = Arrays.asList("auth-service", "payment-service", "inventory-service");
        System.out.print("   📍 Enhanced For-Each Loop (Iterating Services): ");
        for (String service : microservices) {
            System.out.print("[" + service + "] ");
        }
        System.out.println();

        // C. While Loop (Simulating Queue Draining)
        int queueLength = 3;
        System.out.println("   📍 While Loop (Draining Message Queue):");
        while (queueLength > 0) {
            System.out.println("      • Processing item... Remaining items: " + (--queueLength));
        }

        // D. Do-While Loop (Guaranteed At Least One Execution - e.g. Exponential Retry)
        int retryAttempts = 0;
        int maxRetries = 3;
        boolean requestSuccessful = false;
        System.out.println("   📍 Do-While Loop (API Connection Retry):");
        do {
            retryAttempts++;
            System.out.println("      • Attempt #" + retryAttempts + " to connect to remote upstream...");
            if (retryAttempts == 2) {
                requestSuccessful = true;
                System.out.println("        -> Connection established on attempt #" + retryAttempts + "!");
            }
        } while (!requestSuccessful && retryAttempts < maxRetries);

        // E. Labeled Break & Continue for Multi-Dimensional / Nested Matrix Scanning
        System.out.println("\n   📍 Labeled Break / Continue (Matrix Inspection for Target Node):");
        String[][] nodeClusterGrid = {
            {"node-1a", "node-1b", "node-1c"},
            {"node-2a", "CORRUPT_NODE", "node-2c"},
            {"node-3a", "node-3b", "node-3c"}
        };

        gridSearch:
        for (int row = 0; row < nodeClusterGrid.length; row++) {
            for (int col = 0; col < nodeClusterGrid[row].length; col++) {
                String nodeName = nodeClusterGrid[row][col];
                if ("CORRUPT_NODE".equals(nodeName)) {
                    System.out.println("      ⚠️ Found " + nodeName + " at [" + row + "][" + col + "]! Aborting grid scan via labeled break.");
                    break gridSearch; // Breaks completely out of outer gridSearch loop
                }
            }
        }
    }

    /**
     * 6️⃣ Senior SRE Control Flow Insights
     */
    private static void demonstrateSreControlFlowInsights() {
        System.out.println("\n6️⃣  SENIOR SRE CONTROL FLOW INSIGHTS:");
        System.out.println("------------------------------------------------------------------------");

        System.out.println("   💡 Short-Circuit Null Safety Pattern:");
        System.out.println("      • Use 'if (obj != null && obj.isValid())' to prevent NullPointerException.");
        System.out.println("      • Replacing with non-short-circuit '&' will force evaluation and CRASH with NPE!");

        System.out.println("\n   💡 Modern Switch Exhaustiveness:");
        System.out.println("      • Modern switch expressions over enums enforce compile-time exhaustiveness checks.");
        System.out.println("      • Adding a new Enum value without updating the switch results in a compilation error,");
        System.out.println("        preventing silent uncaught bugs in production pipelines.");

        System.out.println("\n   💡 Branch Prediction & CPU Cache Alignment:");
        System.out.println("      • Modern CPUs use branch predictors for loops and if-else branches.");
        System.out.println("      • Sorting data prior to tight loop processing can drastically decrease branch mispredictions");
        System.out.println("        and boost execution speed by up to 3x in high-throughput engines.");

        System.out.println("\n   💡 Bitwise Bitmasks for High-Throughput Memory Efficiency:");
        System.out.println("      • Storing 32 boolean flags in a single 'int' bitmask uses 4 bytes of memory,");
        System.out.println("        whereas 'boolean[]' or 'Set<Enum>' creates significant object & memory overhead.");
    }
}
