# ☕ Java Mastery & Senior SRE Production Repository

[![Java Version](https://img.shields.io/badge/Java-21%20LTS-orange.svg?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%26%20SRE%20Ready-blue.svg?style=flat-square)](./ROADMAP.md)
[![Status](https://img.shields.io/badge/Status-Active%20Learning-brightgreen.svg?style=flat-square)]()
[![License](https://img.shields.io/badge/License-MIT-purple.svg?style=flat-square)](LICENSE)

Welcome to the **Java Learn & Production Engineering** repository. This project serves as a comprehensive hands-on curriculum and codebase for mastering **Pure Java (JDK 21 LTS)**, JVM Internals, Multithreading, Database Persistence, and Cloud-Native SRE Observability before transitioning to Enterprise **Spring Boot** microservices.

---

## 🎯 Overview & Objectives

This repository tracks the step-by-step progress from foundational Java CLI compilation to high-throughput production services and SRE engineering principles.

* **Pure Java Core First:** Deep dive into memory allocation (Stack vs. Heap vs. Metaspace), Object-Oriented Design, and Collections before introducing framework magic.
* **Modern Java Features:** Hands-on experience with Java 21 features, including **Virtual Threads (Project Loom)**, Records, Pattern Matching, and Sealed Classes.
* **Concurrency & JVM Diagnostics:** Deep understanding of the Java Memory Model (JMM), GC tuning (G1GC/ZGC), thread dumps, and heap profiling.
* **Production Persistence & Architecture:** Native JDBC, HikariCP connection pooling, ACID transaction management, and microservice observability.

---

## 📁 Repository Directory Structure

```
java-production/
├── README.md                      <-- Project overview & documentation
├── ROADMAP.md                     <-- Detailed 10-Phase Curriculum & checklist
├── 01_basics/                     <-- JDK Ecosystem, manual CLI compilation, Stack/Heap
│   └── src/
│       └── com/example/
│           └── Main.java
├── 02_oop/                        <-- OOP, SOLID principles, domain models
├── 03_collections_exceptions/     <-- Collections Framework internals & fault tolerance
├── 04_io_jdbc/                    <-- Java NIO.2, native JDBC & transaction management
├── 05_modern_java_virtual_threads/ <-- Java 8-21+ features, Streams, Virtual Threads
├── 06_concurrency_jvm/            <-- JMM, Thread pools, GC tuning & JVM profiling
├── 07_design_patterns/            <-- GoF Design Patterns & JUnit 5 / Mockito
├── 08_pure_java_projects/         <-- Capstone Projects (Multi-threaded HTTP Server, Banking Engine)
└── 09_spring_boot_sre/            <-- Spring Boot REST, Security, K8s & Observability
```

---

## 🗺️ High-Level Learning Roadmap

For a granular breakdown with complete progress checkboxes, see [**`ROADMAP.md`**](./ROADMAP.md).

```
[Phase 1: CLI, Syntax & Stack/Heap]
      ↓
[Phase 2: OOP & Clean Architecture]
      ↓
[Phase 3: Core Internals, Collections & Exceptions]
      ↓
[Phase 4: I/O, NIO.2, JDBC & Data Persistence]
      ↓
[Phase 5: Modern Java 8-21 & Virtual Threads]
      ↓
[Phase 6: Multithreading, Memory & JVM Profiling (SRE)]
      ↓
[Phase 7: Design Patterns & Unit Testing]
      ↓
🎯 [PHASE 8: PURE JAVA CAPSTONE PROJECTS]
      ↓
[Phase 9: Spring Boot & Enterprise Architecture]
      ↓
[Phase 10: Senior SRE, Cloud Native, Observability & K8s]
```

---

## 🚀 Quick Start Guide

### Prerequisites

* **Java Development Kit (JDK):** Version 21 LTS (e.g., Eclipse Temurin, OpenJDK)
* **Build Tool:** Apache Maven or Gradle (optional for basic modules)
* **Git:** Version control

### Running Phase 1 (Basics) Code

1. **Clone the Repository:**
   ```bash
   git clone git@github.com:awoladhossain/Java_Learn.git
   cd Java_Learn
   ```

2. **Compile manually via terminal (`javac`):**
   ```bash
   javac -d 01_basics/out 01_basics/src/com/example/Main.java
   ```

3. **Execute the compiled bytecode (`java`):**
   ```bash
   java -cp 01_basics/out com.example.Main
   ```

4. **Package into an Executable JAR:**
   ```bash
   jar -cvfe 01_basics/EcosystemDemo.jar com.example.Main -C 01_basics/out .
   java -jar 01_basics/EcosystemDemo.jar
   ```

---

## 💡 Senior SRE Golden Rules for Java

1. **Memory Discipline:** Understand Stack vs Heap vs Metaspace layout to minimize Garbage Collection (GC) pauses.
2. **Thread Safety:** Prefer immutable domain objects (`record`), lock-free atomic structures, or Virtual Threads over ad-hoc synchronization.
3. **Container Tuning:** Always configure `-XX:MaxRAMPercentage=75.0` when executing inside Docker / Kubernetes environments to prevent OS OOMKills.
4. **Observability:** Use SLF4J with Mapped Diagnostic Context (MDC) trace keys instead of `System.out.println()`.

---

## 📜 License

This repository is licensed under the [MIT License](LICENSE).
