#!/bin/bash

# Test script for statistic-cli meta command
# This script tests all parameters and subcommands

CLI_PATH="/home/ubuntu/workspace/statistic-data/cli/bin/statistic-cli.js"
FAILED_TESTS=()
PASSED_TESTS=()

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to run a test
run_test() {
    local test_name="$1"
    local command="$2"
    local expected_pattern="$3"
    local should_fail="${4:-false}"

    echo -e "\n${YELLOW}Testing: $test_name${NC}"
    echo "Command: $command"

    # Run the command and capture output
    output=$(eval "$command" 2>&1)
    exit_code=$?

    # For tests that should fail, we expect non-zero exit code and usage message
    if [ "$should_fail" = "true" ]; then
        if [ $exit_code -ne 0 ] && echo "$output" | grep -q "Usage:"; then
            echo -e "${GREEN}✓ PASSED: $test_name (failed with usage message as expected)${NC}"
            PASSED_TESTS+=("$test_name")
        else
            echo -e "${RED}✗ FAILED: $test_name${NC}"
            echo "  Expected to fail with usage message"
            echo "  Exit code: $exit_code"
            echo "  Output: $output"
            FAILED_TESTS+=("$test_name")
        fi
    else
        # For tests that should succeed, check if output matches expected pattern
        if echo "$output" | grep -q "$expected_pattern"; then
            echo -e "${GREEN}✓ PASSED: $test_name${NC}"
            PASSED_TESTS+=("$test_name")
        else
            echo -e "${RED}✗ FAILED: $test_name${NC}"
            echo "  Expected pattern not found: $expected_pattern"
            echo "  Actual output:"
            echo "$output" | head -20
            FAILED_TESTS+=("$test_name")
        fi
    fi
}

# Start testing
echo "=========================================="
echo "Testing statistic-cli meta command"
echo "=========================================="

# Test 1: Help command
run_test "meta --help" \
    "node $CLI_PATH meta --help" \
    "META COMMAND"

# Test 2: meta show without parameter (should fail)
run_test "meta show without parameter" \
    "node $CLI_PATH meta show" \
    "Usage:" \
    "true"

# Test 3: meta show with field (connection error expected)
run_test "meta show test_field (connection error)" \
    "node $CLI_PATH meta show test_field" \
    "Cannot connect to server"

# Test 4: meta list command
run_test "meta list command" \
    "node $CLI_PATH meta list" \
    "Metadata list"

# Test 5: meta sql without parameter (should fail)
run_test "meta sql without parameter" \
    "node $CLI_PATH meta sql" \
    "Usage:" \
    "true"

# Test 6: meta sql with field (connection error expected)
run_test "meta sql test_field (connection error)" \
    "node $CLI_PATH meta sql test_field" \
    "Cannot connect to server"

# Test 7: meta info without parameter (should fail)
run_test "meta info without parameter" \
    "node $CLI_PATH meta info" \
    "Usage:" \
    "true"

# Test 8: meta info with field (connection error expected)
run_test "meta info test_field (connection error)" \
    "node $CLI_PATH meta info test_field" \
    "Cannot connect to server"

# Test 9: meta show with --json flag
run_test "meta show test_field --json" \
    "node $CLI_PATH meta show test_field --json" \
    "Cannot connect to server"

# Test 10: meta show with --yaml flag
run_test "meta show test_field --yaml" \
    "node $CLI_PATH meta show test_field --yaml" \
    "Cannot connect to server"

# Test 11: meta list with --pattern option
run_test "meta list --pattern user" \
    "node $CLI_PATH meta list --pattern user" \
    "Metadata list"

# Test 12: Invalid subcommand
run_test "meta invalid_subcommand" \
    "node $CLI_PATH meta invalid_command" \
    "META COMMAND"

# Print summary
echo -e "\n=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "${GREEN}Passed: ${#PASSED_TESTS[@]}${NC}"
echo -e "${RED}Failed: ${#FAILED_TESTS[@]}${NC}"

if [ ${#FAILED_TESTS[@]} -gt 0 ]; then
    echo -e "\n${RED}Failed tests:${NC}"
    for test in "${FAILED_TESTS[@]}"; do
        echo "  - $test"
    done
    exit 1
else
    echo -e "\n${GREEN}All tests passed!${NC}"
    exit 0
fi
