#!/bin/bash

# Additional edge case tests for statistic-cli stat command

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
    local expected_pattern="${3:-.*}"
    local should_fail="${4:-false}"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo ""
    echo "=========================================="
    echo "Edge Case Test $TOTAL_TESTS: $test_name"
    echo "Command: $command"
    echo "=========================================="

    # Run the command
    output=$(eval "$command" 2>&1)
    local exit_code=$?

    # Print output
    echo "$output"

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

    # Also check if output contains expected pattern
    if [ -n "$expected_pattern" ] && [ "$expected_pattern" != ".*" ]; then
        if echo "$output" | grep -q "$expected_pattern"; then
            passed=true
        else
            passed=false
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
echo "Edge Case Tests for statistic-cli stat"
echo "=========================================="

# Test 1: Multiple options combined
run_test "stat get with multiple options" \
    "node $CLI_PATH stat get test_group --identifier test123 --json 2>&1" \
    "Error" \
    "true"

# Test 2: Empty string parameters
run_test "stat get with empty identifier" \
    "node $CLI_PATH stat get test_group --identifier '' 2>&1" \
    "Error" \
    "true"

# Test 3: Special characters in parameters
run_test "stat query with special chars in filter" \
    "node $CLI_PATH stat query test_field --filter 'key=value&param=test' 2>&1" \
    "Filter:" \
    "true"

# Test 4: Invalid period format
run_test "stat trend with invalid period" \
    "node $CLI_PATH stat trend test_field --period invalid 2>&1" \
    "Trend:" \
    "false"

# Test 5: Date format variations
run_test "stat compare with ISO date format" \
    "node $CLI_PATH stat compare test_field --from 2024-01-01T00:00:00Z --to 2024-12-31T23:59:59Z" \
    "Compare:" \
    "false"

# Test 6: Large threshold value
run_test "stat query with large threshold" \
    "node $CLI_PATH stat query test_field --threshold 999999999 2>&1" \
    "Threshold:" \
    "true"

# Test 7: Unknown subcommand
run_test "stat with unknown subcommand" \
    "node $CLI_PATH stat unknown_subcommand 2>&1" \
    "STAT COMMAND" \
    "false"

# Test 8: Query with both filter and threshold
run_test "stat query with filter and threshold" \
    "node $CLI_PATH stat query test_field --filter type=user --threshold 500 2>&1" \
    "Filter:" \
    "true"

# Test 9: Trend with different period formats
run_test "stat trend with 7d period" \
    "node $CLI_PATH stat trend test_field --period 7d" \
    "Trend:" \
    "false"

# Test 10: Get with table format (should fail gracefully on connection error)
run_test "stat get with table format" \
    "node $CLI_PATH stat get test_group --table 2>&1" \
    "Error" \
    "true"

# Test 11: Compare with only from date
run_test "stat compare with only from date" \
    "node $CLI_PATH stat compare test_field --from 2024-01-01" \
    "Compare:" \
    "false"

# Test 12: Compare with only to date
run_test "stat compare with only to date" \
    "node $CLI_PATH stat compare test_field --to 2024-12-31" \
    "Compare:" \
    "false"

# Print summary
echo ""
echo "=========================================="
echo "Edge Case Test Summary"
echo "=========================================="
echo "Total tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"
echo "=========================================="

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}All edge case tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some edge case tests failed!${NC}"
    exit 1
fi
