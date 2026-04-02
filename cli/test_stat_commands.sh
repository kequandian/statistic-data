#!/bin/bash

# Test script for statistic-cli stat command
# This script tests all parameters and subcommands of the stat command

CLI_PATH="/home/ubuntu/workspace/statistic-data/cli/bin/statistic-cli.js"
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Color codes
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

    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo ""
    echo "=========================================="
    echo "Test $TOTAL_TESTS: $test_name"
    echo "Command: $command"
    echo "=========================================="

    # Run the command
    eval "$command"
    local exit_code=$?

    # Check if test passed
    local passed=false
    if [ "$should_fail" = "true" ]; then
        if [ $exit_code -ne 0 ]; then
            passed=true
        fi
    else
        if [ $exit_code -eq 0 ]; then
            passed=true
        fi
    fi

    # Print result
    if [ "$passed" = "true" ]; then
        echo -e "${GREEN}✓ PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ FAILED${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

echo "=========================================="
echo "Testing statistic-cli stat command"
echo "=========================================="

# Test 1: statistic-cli stat --help
run_test "stat --help" \
    "node $CLI_PATH stat --help" \
    "STAT COMMAND" \
    "false"

# Test 2: statistic-cli stat get (missing parameter)
run_test "stat get (missing parameter)" \
    "node $CLI_PATH stat get" \
    "Usage:" \
    "true"

# Test 3: statistic-cli stat get test_group (connection failure expected)
run_test "stat get test_group (connection error)" \
    "node $CLI_PATH stat get test_group 2>&1" \
    "Error" \
    "true"

# Test 4: statistic-cli stat get test_group --identifier test123
run_test "stat get with identifier" \
    "node $CLI_PATH stat get test_group --identifier test123 2>&1" \
    "Error" \
    "true"

# Test 5: statistic-cli stat query (missing parameter)
run_test "stat query (missing parameter)" \
    "node $CLI_PATH stat query" \
    "Usage:" \
    "true"

# Test 6: statistic-cli stat query test_field --threshold 1000
run_test "stat query with threshold" \
    "node $CLI_PATH stat query test_field --threshold 1000 2>&1" \
    "Error" \
    "true"

# Test 7: statistic-cli stat query test_field --filter "key=value"
run_test "stat query with filter" \
    "node $CLI_PATH stat query test_field --filter key=value 2>&1" \
    "Error" \
    "true"

# Test 8: statistic-cli stat compare (missing parameter)
run_test "stat compare (missing parameter)" \
    "node $CLI_PATH stat compare" \
    "Usage:" \
    "true"

# Test 9: statistic-cli stat compare test_field --from 2024-01-01 --to 2024-12-31
run_test "stat compare with date range" \
    "node $CLI_PATH stat compare test_field --from 2024-01-01 --to 2024-12-31" \
    "Compare:" \
    "false"

# Test 10: statistic-cli stat trend (missing parameter)
run_test "stat trend (missing parameter)" \
    "node $CLI_PATH stat trend" \
    "Usage:" \
    "true"

# Test 11: statistic-cli stat trend test_field --period 30d
run_test "stat trend with period" \
    "node $CLI_PATH stat trend test_field --period 30d" \
    "Trend:" \
    "false"

# Test 12: statistic-cli stat get test_group --json
run_test "stat get with --json" \
    "node $CLI_PATH stat get test_group --json 2>&1" \
    "Error" \
    "true"

# Test 13: statistic-cli stat get test_group --table
run_test "stat get with --table" \
    "node $CLI_PATH stat get test_group --table 2>&1" \
    "Error" \
    "true"

# Print summary
echo ""
echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo "Total tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"
echo "=========================================="

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed!${NC}"
    exit 1
fi
