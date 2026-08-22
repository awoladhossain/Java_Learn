package com.example.functional;

/**
 * Main Runner Class for Phase 5.1: Functional Programming Essentials.
 * 
 * Executes comprehensive demonstrations covering:
 * - 5.1.1 Lambda Expressions & Method References (Class::method).
 * - 5.1.2 Built-in Functional Interfaces (Supplier, Consumer, Function, Predicate, UnaryOperator).
 * - 5.1.3 Custom @FunctionalInterface definitions & Checked Exception adapters.
 */
public class FunctionalMain {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("⚡ PHASE 5.1: JAVA FUNCTIONAL PROGRAMMING ESSENTIALS DEMONSTRATION");
        System.out.println("========================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Lambda Expressions & Method References
        LambdaAndMethodRefDemo.runDemo();

        // 2. Built-in Functional Interfaces
        BuiltInFunctionalInterfacesDemo.runDemo();

        // 3. Custom @FunctionalInterface Definitions & Exception Handling
        CustomFunctionalInterfaceDemo.runDemo();

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\n========================================================================");
        System.out.printf("✅ PHASE 5.1 FUNCTIONAL PROGRAMMING ESSENTIALS EXECUTED SUCCESSFULLY IN %d ms!\n", elapsedTime);
        System.out.println("========================================================================");
    }
}
