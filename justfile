set dotenv-load := true

# List available commands
list:
    @just --list --unsorted

# Check code style and formatting without modifying files
lint:
    ktlint "src/**/*.kt"

# Automatically format code and apply safe style fixes in-place
fix:
    ktlint -F "src/**/*.kt"

# Run test suite
test:
    ./gradlew test

# Start application locally
run:
    ./gradlew bootRun

# Clean build artifacts
clean:
    ./gradlew clean

# Complete verification: lint check followed by tests
check: lint test
