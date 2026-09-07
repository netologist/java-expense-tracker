# Java Maven Template

> **Note:** This repository is an example fintech project — an expense tracker — created from the [java-maven-template](https://github.com/netologist/java-maven-template). You can inspect it as a reference to see how a real application evolves out of the template.

A production-oriented, Spring-free Java Maven project template with a strong local and CI quality pipeline.

This template is designed as a solid starting point for Java backend and fintech projects that prefer:

* Plain Java over framework magic
* Explicit configuration and dependencies
* Strong compile-time checks
* Automated formatting
* Static analysis
* Separate unit and integration tests
* Architecture validation
* Code coverage reporting
* Container image builds without a Dockerfile
* Simple and reproducible CI

The template intentionally does **not** include Spring or application-specific dependencies.

---

## Philosophy

This project follows a simple principle:

> Start small. Add dependencies and complexity only when they are needed.

The template provides a strong engineering foundation without forcing a specific application framework or architecture.

It avoids:

* Spring Boot
* Dependency injection frameworks
* Premature abstractions
* Speculative dependencies
* Empty architecture layers
* Application-specific libraries that may never be used

Dependencies such as the following should be introduced only when a real feature requires them:

* Picocli
* Jackson
* Database drivers
* ORM frameworks
* AWS SDK
* Testcontainers
* Java Money / Moneta

---

# Technology Stack

* **Java 25**
* **Maven**
* **JUnit**
* **Mockito**
* **AssertJ**
* **ArchUnit**
* **Error Prone**
* **Spotless**
* **JaCoCo**
* **Maven Surefire**
* **Maven Failsafe**
* **Jib**
* **GitHub Actions**

---

# Project Structure

```text
.
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── .mvn/
│   └── jvm.config
│
├── src/
│   ├── main/
│   │   └── java/
│   │
│   ├── test/
│   │   └── java/
│   │
│   └── integration-test/
│       └── java/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

The project intentionally keeps the initial production structure minimal.

Architecture layers should be introduced when real application code requires them.

For example, a future application might evolve into:

```text
cli
 ↓
application
 ↓
domain

infrastructure
```

However, empty packages should not be created merely to represent an intended architecture.

---

# Requirements

## Java

This project requires:

```text
Java 25
```

Check your Java installation:

```bash
java --version
```

## Maven

The project includes the Maven Wrapper, so installing Maven globally is generally unnecessary.

Use:

```bash
./mvnw
```

On Windows:

```bash
mvnw.cmd
```

---

# Quick Start

Clone the repository:

```bash
git clone <repository-url>
cd <repository-name>
```

Run the complete build pipeline:

```bash
./mvnw clean verify
```

This is the primary verification command for the project.

A successful build verifies formatting, compilation, static analysis, tests, architecture rules, integration tests, and coverage report generation.

---

# Build Pipeline

The project uses Maven as the source of truth for build and quality checks.

GitHub Actions does not duplicate these rules.

The pipeline is:

```text
Maven Enforcer
        ↓
Spotless
        ↓
javac -Xlint:all
        ↓
Error Prone
        ↓
Compile
        ↓
Unit Tests
        ↓
Architecture Tests
        ↓
Package
        ↓
Integration Tests
        ↓
JaCoCo Reports
        ↓
Test HTML Reports
```

The complete pipeline runs with:

```bash
./mvnw clean verify
```

---

# Code Formatting

Code formatting is handled by Spotless.

Spotless uses Google Java Format.

The build checks formatting automatically.

To automatically format the project:

```bash
./mvnw spotless:apply
```

To only verify formatting:

```bash
./mvnw spotless:check
```

Formatting violations fail the build.

Checkstyle is intentionally not included because Spotless already owns code formatting and style normalization.

---

# Compiler Warnings

The Maven Compiler Plugin is configured with aggressive compiler warnings:

```text
-Xlint:all
-Werror
```

This means:

* Java compiler warnings are enabled aggressively.
* Compiler warnings fail the build.

This is intentional.

Warnings should be addressed rather than silently ignored.

---

# Error Prone

Error Prone provides additional static analysis during compilation.

It helps detect programming mistakes that standard compiler warnings may not catch.

Error Prone is integrated into the Maven Compiler Plugin and participates in the normal build pipeline.

The project also contains the required JVM module exports and opens configuration for modern JDK compatibility.

Error Prone findings can fail the build.

---

# Unit Tests

Unit tests are located in:

```text
src/test/java
```

Maven Surefire executes unit tests.

Run unit tests with:

```bash
./mvnw test
```

Unit test reports are generated in:

```text
target/surefire-reports/
```

These reports typically include XML and text output suitable for CI systems and debugging.

---

# Integration Tests

Integration tests are intentionally separated from unit tests.

Integration test sources are located in:

```text
src/integration-test/java
```

Integration tests follow the naming convention:

```text
*IT.java
```

Maven Failsafe runs these tests during the Maven lifecycle.

The relevant lifecycle phases are:

```text
integration-test
verify
```

Run the complete integration test pipeline with:

```bash
./mvnw verify
```

Integration test reports are generated in:

```text
target/failsafe-reports/
```

---

# Test Reports

The project generates both raw test reports and human-readable HTML reports.

## Unit Test Reports

Raw reports:

```text
target/surefire-reports/
```

HTML reports:

```text
target/site/test-reports/
```

## Integration Test Reports

Raw reports:

```text
target/failsafe-reports/
```

HTML reports:

```text
target/site/test-reports/
```

The HTML reports provide a browser-friendly view of test execution results.

---

# Architecture Tests

ArchUnit is used to validate architectural rules.

Architecture tests ensure that the codebase evolves according to explicit dependency and package rules.

The current architecture rules should remain meaningful.

The project intentionally avoids rules that silently test nothing.

As the application grows, architecture rules can evolve to enforce boundaries such as:

```text
domain
application
infrastructure
cli
```

For example, future rules may ensure that:

```text
domain
```

does not depend on:

```text
application
infrastructure
cli
```

Architecture rules should be introduced or strengthened when the corresponding production packages and code actually exist.

---

# Code Coverage

JaCoCo generates code coverage reports.

After running:

```bash
./mvnw clean verify
```

coverage reports are available under:

```text
target/site/jacoco/
```

Important files include:

```text
target/site/jacoco/index.html
target/site/jacoco/jacoco.xml
target/site/jacoco/jacoco.csv
```

Open the HTML report in a browser:

```text
target/site/jacoco/index.html
```

The project currently focuses on reliable report generation.

Coverage thresholds should be introduced when the application contains meaningful production code and meaningful tests.

A coverage percentage alone should not become a goal at the expense of test quality.

---

# Container Images

The project can build container images without requiring a Dockerfile.

This is handled by Jib.

Jib builds optimized Java container images directly from the Maven project.

## Build a Docker Image Locally

If Docker is running locally:

```bash
./mvnw compile jib:dockerBuild
```

This creates a local Docker image.

You can inspect it with:

```bash
docker images
```

## Build an Image Tar Archive

To build a container image without requiring the Docker daemon:

```bash
./mvnw compile jib:buildTar
```

This is particularly useful in CI environments.

The generated image archive can later be loaded into Docker if needed.

---

# GitHub Actions CI

The repository includes a GitHub Actions workflow.

The workflow runs on:

```text
push
pull_request
```

The CI pipeline:

1. Checks out the repository
2. Sets up Java 25
3. Uses Maven dependency caching
4. Runs the complete Maven verification pipeline
5. Uploads generated reports as artifacts

The main build command is:

```bash
./mvnw clean verify
```

Maven remains responsible for the quality pipeline.

GitHub Actions is responsible for executing Maven and publishing build artifacts.

---

# CI Artifacts

The GitHub Actions workflow uploads build reports for inspection.

## JaCoCo Coverage Report

Artifact:

```text
jacoco-report
```

Contains:

```text
target/site/jacoco/
```

Including HTML, XML, and CSV coverage reports.

## Unit Test Reports

Artifact:

```text
unit-test-reports
```

Contains:

```text
target/surefire-reports/
```

## Integration Test Reports

Artifact:

```text
integration-test-reports
```

Contains:

```text
target/failsafe-reports/
```

## HTML Test Reports

Artifact:

```text
test-reports
```

Contains:

```text
target/site/test-reports/
```

---

# Dependency Strategy

This template follows a need-driven dependency strategy.

Do not add dependencies because they might be useful in the future.

Add them when a concrete feature requires them.

Examples:

| Dependency          | Add When                                               |
| ------------------- | ------------------------------------------------------ |
| Picocli             | CLI commands are implemented                           |
| Jackson             | JSON serialization is required                         |
| Database driver     | Database persistence is introduced                     |
| Testcontainers      | Real external integration infrastructure is tested     |
| AWS SDK             | AWS integration is implemented                         |
| Java Money / Moneta | Domain requirements require specialized money modeling |

The goal is to keep the dependency graph intentional and understandable.

---

# Suggested Development Workflow

A typical workflow is:

### 1. Make changes

Implement a small, focused change.

### 2. Format the code

```bash
./mvnw spotless:apply
```

### 3. Run the complete verification pipeline

```bash
./mvnw clean verify
```

### 4. Fix all failures

Do not ignore:

* formatting failures
* compiler warnings
* Error Prone findings
* failing tests
* architecture violations

### 5. Commit only when the build is clean

```bash
git status
git add .
git commit
```

---

# Quality Principles

This template prefers:

* Simple solutions
* Explicit dependencies
* Small changes
* Deterministic tests
* Strong compile-time checks
* Meaningful architecture rules
* Reproducible builds
* Verifiable configuration

It avoids:

* Premature abstractions
* Framework dependencies without a concrete need
* Empty architecture layers
* Fake tests
* Rules that silently test nothing
* Duplicate quality tooling without clear value

---

# Optional Future Improvements

The following tools and improvements can be evaluated when the project requires them:

* Additional Error Prone checks
* NullAway
* PMD
* Dependency vulnerability scanning
* Reproducible build improvements
* Coverage enforcement
* GitHub PR annotations
* Branch protection rules

These should be evaluated based on their actual value.

Do not add tools simply to increase the number of tools in the build.

For example, PMD should only be added if it provides meaningful value beyond:

```text
Spotless
javac lint
Error Prone
```

---

# Creating a Project From This Template

This repository can be used as a GitHub template.

Using GitHub CLI:

```bash
gh repo create my-new-project \
  --template netologist/java-maven-template \
  --public \
  --clone \
  --description "A Java-based project"
```

For a private repository:

```bash
gh repo create my-new-project \
  --template netologist/java-maven-template \
  --private \
  --clone \
  --description "A Java-based project"
```

After creating a project from the template, update:

* Maven coordinates
* Package names
* Application name
* Repository description
* README
* Application-specific configuration

Then run:

```bash
./mvnw clean verify
```

before starting development.

---

# Core Principle

> Maven owns the build and quality pipeline.
> CI executes Maven.
> Dependencies are added only when needed.
> Architecture evolves with real code.

This template provides the foundation.

Your application defines the rest.
