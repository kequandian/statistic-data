/**
 * E2E Test Utilities
 * Provides helper functions for E2E testing
 */

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

// Test configuration
const TEST_CONFIG = {
    baseUrl: process.env.STATISTIC_BASE_URL || 'http://localhost:8086',
    token: process.env.STATISTIC_TOKEN || '',
    timeout: 30000
};

// ANSI color codes for output
const colors = {
    reset: '\x1b[0m',
    bright: '\x1b[1m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    magenta: '\x1b[35m',
    cyan: '\x1b[36m'
};

/**
 * Print colored output
 */
function log(color, ...args) {
    console.log(colors[color] || '', ...args, colors.reset);
}

/**
 * Print test section header
 */
function section(name) {
    console.log('');
    log('cyan', '='.repeat(60));
    log('cyan', `  ${name}`);
    log('cyan', '='.repeat(60));
}

/**
 * Print test case header
 */
function test(name) {
    console.log('');
    log('blue', `▶ ${name}`);
}

/**
 * Print success message
 */
function success(message) {
    log('green', `  ✓ ${message}`);
}

/**
 * Print error message
 */
function error(message) {
    log('red', `  ✗ ${message}`);
}

/**
 * Print warning message
 */
function warn(message) {
    log('yellow', `  ⚠ ${message}`);
}

/**
 * Print info message
 */
function info(message) {
    console.log(`  ℹ ${message}`);
}

/**
 * Execute CLI command
 */
function execCli(args, options = {}) {
    const cliPath = path.join(__dirname, '..', 'bin', 'stats-cli.js');
    const cmd = `node ${cliPath} ${args}`;

    const env = {
        ...process.env,
        STATISTIC_BASE_URL: TEST_CONFIG.baseUrl,
        STATISTIC_TOKEN: TEST_CONFIG.token
    };

    try {
        const stdout = execSync(cmd, {
            env,
            encoding: 'utf8',
            stdio: options.silent ? 'pipe' : 'inherit',
            timeout: options.timeout || TEST_CONFIG.timeout
        });
        return { success: true, stdout: stdout || '', stderr: '' };
    } catch (err) {
        return {
            success: false,
            stdout: err.stdout || '',
            stderr: err.stderr || '',
            error: err.message
        };
    }
}

/**
 * Execute CLI command and capture output
 */
function execCliCapture(args) {
    return execCli(args, { silent: true });
}

/**
 * Assert condition is true
 */
function assert(condition, message) {
    if (!condition) {
        throw new Error(`Assertion failed: ${message}`);
    }
}

/**
 * Assert equals
 */
function assertEqual(actual, expected, message) {
    if (actual !== expected) {
        throw new Error(`Assertion failed: ${message}\n  Expected: ${expected}\n  Actual: ${actual}`);
    }
}

/**
 * Assert contains
 */
function assertContains(haystack, needle, message) {
    if (!haystack.includes(needle)) {
        throw new Error(`Assertion failed: ${message}\n  Expected to contain: ${needle}\n  Actual: ${haystack}`);
    }
}

/**
 * Assert greater than
 */
function assertGreaterThan(actual, expected, message) {
    if (actual <= expected) {
        throw new Error(`Assertion failed: ${message}\n  Expected: > ${expected}\n  Actual: ${actual}`);
    }
}

/**
 * Assert less than
 */
function assertLessThan(actual, expected, message) {
    if (actual >= expected) {
        throw new Error(`Assertion failed: ${message}\n  Expected: < ${expected}\n  Actual: ${actual}`);
    }
}

/**
 * Wait for a condition to be true
 */
async function waitFor(condition, timeout = 5000, interval = 100) {
    const start = Date.now();
    while (Date.now() - start < timeout) {
        if (await condition()) {
            return true;
        }
        await new Promise(resolve => setTimeout(resolve, interval));
    }
    throw new Error(`Timeout waiting for condition after ${timeout}ms`);
}

/**
 * Sleep for a specified time
 */
function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * Generate unique test ID
 */
function generateTestId() {
    return `test_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
}

/**
 * Cleanup test data
 */
async function cleanupTest(fieldName) {
    // Note: There's no delete API, so we just log
    info(`Cleanup: Field '${fieldName}' data will remain in database`);
}

/**
 * Parse JSON from output
 */
function parseJson(output) {
    try {
        return JSON.parse(output);
    } catch (e) {
        throw new Error(`Failed to parse JSON: ${output}`);
    }
}

/**
 * Run a test case
 */
async function runTest(name, testFn) {
    test(name);
    try {
        await testFn();
        success(name);
        return true;
    } catch (err) {
        error(`${name}: ${err.message}`);
        if (process.env.DEBUG) {
            console.error(err.stack);
        }
        return false;
    }
}

/**
 * Run test suite
 */
async function runSuite(suiteName, tests) {
    section(suiteName);

    let passed = 0;
    let failed = 0;

    for (const test of tests) {
        const result = await runTest(test.name, test.fn);
        if (result) {
            passed++;
        } else {
            failed++;
        }
    }

    console.log('');
    log('cyan', '-'.repeat(60));
    log('cyan', `  Results: ${passed} passed, ${failed} failed`);
    log('cyan', '-'.repeat(60));

    return { passed, failed, total: passed + failed };
}

module.exports = {
    TEST_CONFIG,
    log,
    section,
    test,
    success,
    error,
    warn,
    info,
    execCli,
    execCliCapture,
    assert,
    assertEqual,
    assertContains,
    assertGreaterThan,
    assertLessThan,
    waitFor,
    sleep,
    generateTestId,
    cleanupTest,
    parseJson,
    runTest,
    runSuite
};
