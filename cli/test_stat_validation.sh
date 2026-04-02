#!/bin/bash

# Validation test for statistic-cli stat command
# This script validates error messages and output formats

CLI_PATH="/home/ubuntu/workspace/statistic-data/cli/bin/statistic-cli.js"
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to validate output contains expected text
validate_output() {
    local output="$1"
    local expected="$2"
    local test_name="$3"

    if echo "$output" | grep -q "$expected"; then
        echo -e "${GREEN}✓${NC} $test_name"
        return 0
    else
        echo -e "${RED}✗${NC} $test_name"
        echo -e "  Expected: $expected"
        echo -e "  Got: $output"
        return 1
    fi
}

echo "=========================================="
echo "Validation Tests for statistic-cli stat"
echo "=========================================="

# Test 1: Help contains all subcommands
echo ""
echo "Test 1: Validate help content"
output=$(node $CLI_PATH stat --help)
TOTAL_TESTS=$((TOTAL_TESTS + 1))
if echo "$output" | grep -q "get <group>" && \
   echo "$output" | grep -q "query <field>" && \
   echo "$output" | grep -q "compare <field>" && \
   echo "$output" | grep -q "trend <field>"; then
    echo -e "${GREEN}✓ PASSED${NC} - Help contains all subcommands"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗ FAILED${NC} - Help missing subcommands"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Test 2: Missing parameter error messages
echo ""
echo "Test 2: Validate error messages for missing parameters"

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat get 2>&1)
if validate_output "$output" "Usage:" "stat get missing parameter"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat query 2>&1)
if validate_output "$output" "Usage:" "stat query missing parameter"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat compare 2>&1)
if validate_output "$output" "Usage:" "stat compare missing parameter"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat trend 2>&1)
if validate_output "$output" "Usage:" "stat trend missing parameter"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Test 3: Output format validation
echo ""
echo "Test 3: Validate output formats"

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat get test_group 2>&1)
if validate_output "$output" "Error:" "Connection error displayed"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat query test_field --threshold 1000 2>&1)
if validate_output "$output" "Threshold: 1000" "Threshold displayed correctly"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat query test_field --filter key=value 2>&1)
if validate_output "$output" "Filter: key=value" "Filter displayed correctly"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Test 4: Compare command output validation
echo ""
echo "Test 4: Validate compare command output"

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat compare test_field --from 2024-01-01 --to 2024-12-31 2>&1)
if echo "$output" | grep -q "From: 2024-01-01" && \
   echo "$output" | grep -q "To: 2024-12-31"; then
    echo -e "${GREEN}✓ PASSED${NC} - Compare displays date range correctly"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗ FAILED${NC} - Compare date range not displayed correctly"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Test 5: Trend command output validation
echo ""
echo "Test 5: Validate trend command output"

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat trend test_field --period 30d 2>&1)
if echo "$output" | grep -q "Trend: test_field (30d)"; then
    echo -e "${GREEN}✓ PASSED${NC} - Trend displays period correctly"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗ FAILED${NC} - Trend period not displayed correctly"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Test 6: Unknown subcommand handling
echo ""
echo "Test 6: Validate unknown subcommand handling"

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat unknown_command 2>&1)
if validate_output "$output" "STAT COMMAND" "Unknown command shows help"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Test 7: Identifier parameter validation
echo ""
echo "Test 7: Validate identifier parameter"

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat get test_group --identifier test123 2>&1)
if echo "$output" | grep -q "identifier=test123"; then
    echo -e "${GREEN}✓ PASSED${NC} - Identifier included in request"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗ FAILED${NC} - Identifier not included in request"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Test 8: Backend support notice
echo ""
echo "Test 8: Validate backend support notices"

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat compare test_field --from 2024-01-01 --to 2024-12-31 2>&1)
if validate_output "$output" "requires backend support" "Compare shows backend notice"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 1))
output=$(node $CLI_PATH stat trend test_field --period 30d 2>&1)
if validate_output "$output" "requires backend support" "Trend shows backend notice"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Print summary
echo ""
echo "=========================================="
echo "Validation Test Summary"
echo "=========================================="
echo "Total tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"
echo "=========================================="

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}All validation tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some validation tests failed!${NC}"
    exit 1
fi
