# Nim Game API — Design & Specification

A RESTful API for the single-heap **Nim** game, supporting both the **normal** and **misère** variants, built with **Kotlin** and **Spring Boot**.

---

## Game Theory & Specification

The system implements the single-heap subtraction game **Nim**. The **misère** variant is specified below (the normal variant will be added):
- **Rules:** The game is played with a single heap of matches. Two players (Human vs. Computer) take turns removing **1, 2, or 3 matches**.
- **Misère Condition:** The player forced to take the **last match loses**.

---

## Technical Stack & Architectural Decisions

- **Kotlin & JVM 25 (LTS):** Chosen for modern language ergonomics (immutability, data classes, null safety) on the latest LTS runtime.
- **Spring Boot 4.1.1:** Latest release track, avoiding milestones or unstable snapshots.
- **Minimal Dependencies:** Core framework starters limited to `org.springframework.boot:spring-boot-starter-webmvc` and `org.springframework.boot:spring-boot-starter-validation`. `kotlin-reflect` and `jackson-module-kotlin` are included for Kotlin serialization support.
- **Dev Environment Isolation (NixOS / Flakes):** The project includes a `flake.nix` providing JDK 25, `curl`, `just`, and `ktlint`. Gradle caches are scoped strictly to `.gradle-home/` to ensure zero pollution of the host environment.

---

## Prerequisites

| Tool | Version | Required for |
|------|---------|--------------|
| JDK | 25 (LTS) | Compilation & runtime |
| `just` | any recent | Command runner |
| `ktlint` | latest | Lint / format |

> **Gradle** is not required — the project ships a Gradle wrapper (`./gradlew`).

**Nix users:** Enter the development shell via `nix develop`. The `flake.nix` provisions all of the above automatically.

**Non-Nix users:** Install the tools independently. The JDK must be available on `$JAVA_HOME` or `$PATH` for the Gradle wrapper to resolve the toolchain.

---

## Available Commands

Build and development tasks are managed via [`just`](https://github.com/casey/just). Run `just` or `just list` to display all available recipes.

| Recipe | Description |
|--------|-------------|
| `just list` | List all available recipes |
| `just lint` | Check code style and formatting without modifying files |
| `just fix` | Automatically format code and apply safe style fixes in-place |
| `just test` | Run the test suite |
| `just run` | Start the application locally |
| `just clean` | Clean build artifacts |
| `just check` | Complete verification: lint check followed by tests |

---

## Project Inception (Scaffolding)

The initial baseline was generated via the official Spring Initializr API:

```bash
curl https://start.spring.io/starter.tgz \
  -d type=gradle-project-kotlin \
  -d language=kotlin \
  -d bootVersion=4.1.1 \
  -d baseDir=. \
  -d groupId=com.nim \
  -d artifactId=nim-spring-kotlin \
  -d name=nim-spring-kotlin \
  -d packageName=com.nim.game \
  -d javaVersion=25 \
  -d dependencies=web,validation \
  | tar -xzvf -
```
