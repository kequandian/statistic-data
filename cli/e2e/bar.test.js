#!/usr/bin/env node
/**
 * E2E Tests for Bar Command
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
 * Test: Bar command help
 */
async function testBarHelp() {
    const result = execCliCapture('bar --help');
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'BAR COMMAND', 'Should show bar command help');
    assertContains(result.stdout, 'column "<label>" with <value>', 'Should show usage');
}

/**
 * Test: Add single bar data item
 */
async function testBarAddSingleItem() {
    const result = execCliCapture(`bar ${testField}_single column "Q1" with 15000`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Added data', 'Should confirm data added');
    assertContains(result.stdout, 'Data inserted successfully', 'Should confirm insert');
}

/**
 * Test: Add multiple bar data items
 */
async function testBarAddMultipleItems() {
    execCliCapture(`bar ${testField}_multi column "Q1" with 15000`);
    const result = execCliCapture(`bar ${testField}_multi column "Q2" with 23000`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Added data', 'Should confirm data added');
    assertContains(result.stdout, 'Data inserted successfully', 'Should confirm insert');
}

/**
 * Test: Query bar data
 */
async function testBarQuery() {
    await sleep(500);

    const result = execCliCapture(`bar ${testField}_multi`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Bar Chart', 'Should show bar chart');
    assertContains(result.stdout, 'Q1', 'Should show Q1');
    assertContains(result.stdout, 'Q2', 'Should show Q2');
}

/**
 * Test: Query bar data with JSON output
 */
async function testBarQueryJson() {
    const result = execCliCapture(`bar ${testField}_multi --json`);
    assert(result.success, 'Command should succeed');
    assert(result.stdout.startsWith('{'), 'Should output JSON');
    assertContains(result.stdout, '"field"', 'Should contain field data');
}

/**
 * Test: Bar with zero value
 */
async function testBarWithZeroValue() {
    const result = execCliCapture(`bar ${testField}_zero column "Empty" with 0`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Added data', 'Should confirm data added');
}

/**
 * Test: Bar with decimal value
 */
async function testBarWithDecimalValue() {
    const result = execCliCapture(`bar ${testField}_decimal column "Decimal" with 12345.67`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Added data', 'Should confirm data added');
}

/**
 * Test: Bar with negative value
 */
async function testBarWithNegativeValue() {
    const result = execCliCapture(`bar ${testField}_negative column "Loss" with -5000`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Added data', 'Should confirm data added');
}

/**
 * Test: Bar with multiple categories (quarterly data)
 */
async function testBarQuarterlyData() {
    execCliCapture(`bar ${testField}_quarterly column "Q1" with 10000`);
    execCliCapture(`bar ${testField}_quarterly column "Q2" with 15000`);
    execCliCapture(`bar ${testField}_quarterly column "Q3" with 12000`);
    const result = execCliCapture(`bar ${testField}_quarterly column "Q4" with 18000`);
    assert(result.success, 'Command should succeed');

    await sleep(500);
    const query = execCliCapture(`bar ${testField}_quarterly`);
    assertContains(query.stdout, 'Q1', 'Should show Q1');
    assertContains(query.stdout, 'Q2', 'Should show Q2');
    assertContains(query.stdout, 'Q3', 'Should show Q3');
    assertContains(query.stdout, 'Q4', 'Should show Q4');
}

/**
 * Run all bar tests
 */
async function runBarTests() {
    return await runSuite('Bar Command E2E Tests', [
        { name: 'Bar help', fn: testBarHelp },
        { name: 'Bar add single item', fn: testBarAddSingleItem },
        { name: 'Bar add multiple items', fn: testBarAddMultipleItems },
        { name: 'Bar query', fn: testBarQuery },
        { name: 'Bar query JSON', fn: testBarQueryJson },
        { name: 'Bar with zero value', fn: testBarWithZeroValue },
        { name: 'Bar with decimal value', fn: testBarWithDecimalValue },
        { name: 'Bar with negative value', fn: testBarWithNegativeValue },
        { name: 'Bar quarterly data', fn: testBarQuarterlyData }
    ]);
}

// Export for use in test runner
module.exports = { runBarTests };

// Run directly if executed
if (require.main === module) {
    runBarTests().then(({ passed, failed, total }) => {
        process.exit(failed > 0 ? 1 : 0);
    }).catch(err => {
        console.error('Test error:', err);
        process.exit(1);
    });
}
