#!/usr/bin/env node
/**
 * E2E Tests for Count Command
 */

const {
    section, test, success, error, info,
    execCli, execCliCapture,
    assert, assertEqual, assertContains,
    generateTestId, runSuite, sleep
} = require('./test-utils');

// Test data
const testField = generateTestId();

/**
 * Test: Count command help
 */
async function testCountHelp() {
    const result = execCliCapture('count --help');
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'COUNT COMMAND', 'Should show count command help');
    assertContains(result.stdout, 'with <value>', 'Should show usage');
}

/**
 * Test: Add count data
 */
async function testCountAdd() {
    const result = execCliCapture(`count ${testField}_add with 1523`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Count data inserted successfully', 'Should confirm insert');
    assertContains(result.stdout, 'Value: 1523', 'Should show value');
}

/**
 * Test: Query count data
 */
async function testCountQuery() {
    await sleep(500);

    const result = execCliCapture(`count ${testField}_add`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Count:', 'Should show count label');
    assertContains(result.stdout, '1523', 'Should show value');
}

/**
 * Test: Query count data with JSON output
 */
async function testCountQueryJson() {
    const result = execCliCapture(`count ${testField}_add --json`);
    assert(result.success, 'Command should succeed');
    assert(result.stdout.startsWith('{'), 'Should output JSON');
    assertContains(result.stdout, '"field"', 'Should contain field data');
}

/**
 * Test: Count with zero
 */
async function testCountWithZero() {
    const result = execCliCapture(`count ${testField}_zero with 0`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Count data inserted successfully', 'Should confirm insert');
}

/**
 * Test: Count with large number
 */
async function testCountWithLargeNumber() {
    const result = execCliCapture(`count ${testField}_large with 999999999`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Count data inserted successfully', 'Should confirm insert');
}

/**
 * Test: Count with decimal
 */
async function testCountWithDecimal() {
    const result = execCliCapture(`count ${testField}_decimal with 1234.56`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Count data inserted successfully', 'Should confirm insert');
}

/**
 * Test: Count update (overwrite existing)
 */
async function testCountUpdate() {
    // First insert
    execCliCapture(`count ${testField}_update with 100`);
    await sleep(500);

    // Query to verify
    const query1 = execCliCapture(`count ${testField}_update`);
    assertContains(query1.stdout, '100', 'Should show initial value');

    // Update with new value
    const result = execCliCapture(`count ${testField}_update with 200`);
    assert(result.success, 'Command should succeed');

    await sleep(500);

    // Query to verify update
    const query2 = execCliCapture(`count ${testField}_update`);
    assertContains(query2.stdout, '200', 'Should show updated value');
}

/**
 * Run all count tests
 */
async function runCountTests() {
    return await runSuite('Count Command E2E Tests', [
        { name: 'Count help', fn: testCountHelp },
        { name: 'Count add', fn: testCountAdd },
        { name: 'Count query', fn: testCountQuery },
        { name: 'Count query JSON', fn: testCountQueryJson },
        { name: 'Count with zero', fn: testCountWithZero },
        { name: 'Count with large number', fn: testCountWithLargeNumber },
        { name: 'Count with decimal', fn: testCountWithDecimal },
        { name: 'Count update', fn: testCountUpdate }
    ]);
}

// Export for use in test runner
module.exports = { runCountTests };

// Run directly if executed
if (require.main === module) {
    runCountTests().then(({ passed, failed, total }) => {
        process.exit(failed > 0 ? 1 : 0);
    }).catch(err => {
        console.error('Test error:', err);
        process.exit(1);
    });
}
