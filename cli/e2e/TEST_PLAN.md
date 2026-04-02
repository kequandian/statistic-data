# E2E Test Plan for stats-cli

## Overview

This document outlines the E2E test plan for the stats-cli tool to ensure all commands work correctly with the Java backend API.

## Test Environment

- **Java Application**: Spring Boot application on port 8086
- **Database**: H2 in-memory database (test profile)
- **CLI**: stats-cli (Node.js)
- **Configuration**: .env file for API base URL

## Test Coverage

### 1. Pie Command Tests

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `pie --help` | Display help | Shows usage and examples |
| `pie <name> rate "A" with 40` | Add single item | Data stored, not yet inserted |
| `pie <name> rate "B" with 40` | Add second item | Data inserted (sum=80) |
| `pie <name>` | Query data | Shows pie chart with percentages |
| `pie <name> --json` | Query JSON output | Returns JSON data |
| `pie <name> rate "A" with 10` | Validation: sum too low | Error: sum < 80 |
| `pie <name> rate "A" with 130` | Validation: sum too high | Error: sum > 120 |
| `pie <name> rate "A" with 50` + `rate "B" with 50` | Exact 100 total | Data inserted successfully |

### 2. Bar Command Tests

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `bar --help` | Display help | Shows usage and examples |
| `bar <name> category "Q1" with 15000` | Add single item | Data inserted immediately |
| `bar <name>` | Query data | Shows bar chart with ASCII bars |
| `bar <name> --json` | Query JSON output | Returns JSON data |
| `bar <name> category "X" with 0` | Zero value | Data inserted |
| `bar <name> category "X" with 12345.67` | Decimal value | Data inserted |
| `bar <name> category "X" with -5000` | Negative value | Data inserted |
| `bar <name>` with 4 quarters | Multiple categories | All categories shown |

### 3. Count Command Tests

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `count --help` | Display help | Shows usage and examples |
| `count <name> with 1523` | Add count | Data inserted immediately |
| `count <name>` | Query data | Shows count value |
| `count <name> --json` | Query JSON output | Returns JSON data |
| `count <name> with 0` | Zero value | Data inserted |
| `count <name> with 999999999` | Large number | Data inserted |
| `count <name> with 1234.56` | Decimal value | Data inserted |
| `count <name>` after update | Update value | Shows updated value |

### 4. Double-Bar Command Tests

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `double-bar --help` | Display help | Shows usage and examples |
| `double-bar <name> series "A" "X" with 100` | Add first series | Waiting for 2nd series |
| `double-bar <name> series "B" "X" with 100` | Add second series | Data inserted |
| `double-bar <name>` | Query data | Shows grouped bar chart |
| `double-bar <name> --json` | Query JSON output | Returns JSON data |
| 4 quarters × 2 years | Multiple categories | All data shown grouped by series |
| Year-over-year comparison | Comparison data | Correct grouping |

## Test Execution Order

1. **Setup**: Start Java application with test profile
2. **Configuration**: Set .env with `STATISTIC_BASE_URL=http://localhost:8086`
3. **Run Tests**: Execute test suite
4. **Cleanup**: Review test data in database

## Test Files

- `e2e/test-utils.js` - Test utilities and helpers
- `e2e/pie.test.js` - Pie command tests
- `e2e/bar.test.js` - Bar command tests
- `e2e/count.test.js` - Count command tests
- `e2e/double-bar.test.js` - Double-bar command tests
- `e2e/run-all.js` - Main test runner

## Success Criteria

- All 30+ test cases pass
- No API errors (4xx, 5xx)
- Data correctly stored in database
- JSON output is valid
- ASCII charts render correctly
