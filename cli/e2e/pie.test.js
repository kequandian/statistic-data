#!/usr/bin/env node
/**
 * E2E Tests for Pie Command
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
 * Test: Pie command help
 */
async function testPieHelp() {
    const result = execCliCapture('pie --help');
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'PIE COMMAND', 'Should show pie command help');
    assertContains(result.stdout, 'rate "<label>" with <value>', 'Should show usage');
}

/**
 * Test: Add single pie data item (should not insert yet)
 */
async function testPieAddSingleItem() {
    const result = execCliCapture(`pie ${testField}_single rate "Category A" with 40`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Added data', 'Should confirm data added');
    assertContains(result.stdout, 'Current total: 40', 'Should show total');
}

/**
 * Test: Add second pie data item (should trigger insert)
 */
async function testPieAddSecondItem() {
    const result = execCliCapture(`pie ${testField}_single rate "Category B" with 40`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Added data', 'Should confirm data added');
    assertContains(result.stdout, 'Data inserted successfully', 'Should confirm insert');
}

/**
 * Test: Query pie data
 */
async function testPieQuery() {
    // Wait a bit for data to be processed
    await sleep(500);

    const result = execCliCapture(`pie ${testField}_single`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Pie Chart', 'Should show pie chart');
    assertContains(result.stdout, 'Category A', 'Should show Category A');
    assertContains(result.stdout, 'Category B', 'Should show Category B');
}

/**
 * Test: Query pie data with JSON output
 */
async function testPieQueryJson() {
    const result = execCliCapture(`pie ${testField}_single --json`);
    assert(result.success, 'Command should succeed');
    assert(result.stdout.startsWith('{'), 'Should output JSON');
    assertContains(result.stdout, '"field"', 'Should contain field data');
}

/**
 * Test: Pie validation - sum too low
 */
async function testPieValidationTooLow() {
    const result = execCliCapture(`pie ${testField}_low rate "A" with 10`);
    assert(!result.success || result.stdout.includes('80-120'), 'Should show validation error');
}

/**
 * Test: Pie validation - sum too high
 */
async function testPieValidationTooHigh() {
    // First add some data
    execCliCapture(`pie ${testField}_high rate "A" with 60`);
    const result = execCliCapture(`pie ${testField}_high rate "B" with 70`);
    assert(!result.success || result.stdout.includes('80-120'), 'Should show validation error');
}

/**
 * Test: Pie with exact 100 total
 */
async function testPieExact100() {
    execCliCapture(`pie ${testField}_exact100 rate "A" with 50`);
    const result = execCliCapture(`pie ${testField}_exact100 rate "B" with 50`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Data inserted successfully', 'Should insert data');
}

/**
 * Test: Pie with multiple categories
 */
async function testPieMultipleCategories() {
    // Add 4 categories: 25 + 25 + 25 + 25 = 100
    execCliCapture(`pie ${testField}_multi rate "Q1" with 25`);
    execCliCapture(`pie ${testField}_multi rate "Q2" with 25`);
    execCliCapture(`pie ${testField}_multi rate "Q3" with 25`);
    const result = execCliCapture(`pie ${testField}_multi rate "Q4" with 25`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Data inserted successfully', 'Should insert data');

    // Query and verify
    await sleep(500);
    const query = execCliCapture(`pie ${testField}_multi`);
    assertContains(query.stdout, 'Q1', 'Should show Q1');
    assertContains(query.stdout, 'Q2', 'Should show Q2');
    assertContains(query.stdout, 'Q3', 'Should show Q3');
    assertContains(query.stdout, 'Q4', 'Should show Q4');
}

/**
 * Run all pie tests
 */
async function runPieTests() {
    return await runSuite('Pie Command E2E Tests', [
        { name: 'Pie help', fn: testPieHelp },
        { name: 'Pie add single item', fn: testPieAddSingleItem },
        { name: 'Pie add second item', fn: testPieAddSecondItem },
        { name: 'Pie query', fn: testPieQuery },
        { name: 'Pie query JSON', fn: testPieQueryJson },
        { name: 'Pie validation too low', fn: testPieValidationTooLow },
        { name: 'Pie validation too high', fn: testPieValidationTooHigh },
        { name: 'Pie exact 100', fn: testPieExact100 },
        { name: 'Pie multiple categories', fn: testPieMultipleCategories }
    ]);
}

// Export for use in test runner
module.exports = { runPieTests };

// Run directly if executed
if (require.main === module) {
    runPieTests().then(({ passed, failed, total }) => {
        process.exit(failed > 0 ? 1 : 0);
    }).catch(err => {
        console.error('Test error:', err);
        process.exit(1);
    });
}
