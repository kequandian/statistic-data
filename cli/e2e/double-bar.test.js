#!/usr/bin/env node
/**
 * E2E Tests for Double-Bar Command
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
 * Test: Double-bar command help
 */
async function testDoubleBarHelp() {
    const result = execCliCapture('double-bar --help');
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'DOUBLE-BAR COMMAND', 'Should show double-bar command help');
    assertContains(result.stdout, 'series "<series>" "<category>" with <value>', 'Should show usage');
}

/**
 * Test: Add first series data
 */
async function testDoubleBarAddFirstSeries() {
    const result = execCliCapture(`double-bar ${testField}_two series "2024" "Q1" with 15000`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Added data', 'Should confirm data added');
    assertContains(result.stdout, 'Waiting for more data', 'Should show waiting message');
    assertContains(result.stdout, 'Current series: 1/2', 'Should show series count');
}

/**
 * Test: Add second series data (should trigger insert)
 */
async function testDoubleBarAddSecondSeries() {
    const result = execCliCapture(`double-bar ${testField}_two series "2023" "Q1" with 12000`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Added data', 'Should confirm data added');
    assertContains(result.stdout, 'Data inserted successfully', 'Should confirm insert');
}

/**
 * Test: Query double-bar data
 */
async function testDoubleBarQuery() {
    await sleep(500);

    const result = execCliCapture(`double-bar ${testField}_two`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Double-Bar Chart', 'Should show double-bar chart');
    assertContains(result.stdout, 'Series: 2024', 'Should show 2024 series');
    assertContains(result.stdout, 'Series: 2023', 'Should show 2023 series');
}

/**
 * Test: Query double-bar data with JSON output
 */
async function testDoubleBarQueryJson() {
    const result = execCliCapture(`double-bar ${testField}_two --json`);
    assert(result.success, 'Command should succeed');
    assert(result.stdout.startsWith('{'), 'Should output JSON');
    assertContains(result.stdout, '"field"', 'Should contain field data');
}

/**
 * Test: Double-bar with multiple categories per series
 */
async function testDoubleBarMultipleCategories() {
    // Add 2024 series with 4 quarters
    execCliCapture(`double-bar ${testField}_multi series "2024" "Q1" with 10000`);
    execCliCapture(`double-bar ${testField}_multi series "2024" "Q2" with 12000`);
    execCliCapture(`double-bar ${testField}_multi series "2024" "Q3" with 11000`);
    execCliCapture(`double-bar ${testField}_multi series "2024" "Q4" with 14000`);

    // Add 2023 series with 4 quarters
    execCliCapture(`double-bar ${testField}_multi series "2023" "Q1" with 9000`);
    execCliCapture(`double-bar ${testField}_multi series "2023" "Q2" with 10000`);
    execCliCapture(`double-bar ${testField}_multi series "2023" "Q3" with 9500`);
    const result = execCliCapture(`double-bar ${testField}_multi series "2023" "Q4" with 12000`);

    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Data inserted successfully', 'Should confirm insert');

    await sleep(500);

    // Query and verify
    const query = execCliCapture(`double-bar ${testField}_multi`);
    assertContains(query.stdout, 'Series: 2024', 'Should show 2024 series');
    assertContains(query.stdout, 'Series: 2023', 'Should show 2023 series');
    assertContains(query.stdout, 'Q1', 'Should show Q1');
    assertContains(query.stdout, 'Q2', 'Should show Q2');
    assertContains(query.stdout, 'Q3', 'Should show Q3');
    assertContains(query.stdout, 'Q4', 'Should show Q4');
}

/**
 * Test: Double-bar with year-over-year comparison
 */
async function testDoubleBarYearOverYear() {
    // Add current year data
    execCliCapture(`double-bar ${testField}_yoy series "2024" "Jan" with 5000`);
    execCliCapture(`double-bar ${testField}_yoy series "2024" "Feb" with 5500`);
    execCliCapture(`double-bar ${testField}_yoy series "2024" "Mar" with 6000`);

    // Add previous year data
    execCliCapture(`double-bar ${testField}_yoy series "2023" "Jan" with 4500`);
    execCliCapture(`double-bar ${testField}_yoy series "2023" "Feb" with 4800`);
    const result = execCliCapture(`double-bar ${testField}_yoy series "2023" "Mar" with 5200`);

    assert(result.success, 'Command should succeed');

    await sleep(500);

    const query = execCliCapture(`double-bar ${testField}_yoy`);
    assertContains(query.stdout, '2024', 'Should show 2024 series');
    assertContains(query.stdout, '2023', 'Should show 2023 series');
}

/**
 * Test: Double-bar with zero values
 */
async function testDoubleBarWithZeroValues() {
    execCliCapture(`double-bar ${testField}_zero series "A" "X" with 100`);
    const result = execCliCapture(`double-bar ${testField}_zero series "B" "X" with 0`);

    assert(result.success, 'Command should succeed');
}

/**
 * Run all double-bar tests
 */
async function runDoubleBarTests() {
    return await runSuite('Double-Bar Command E2E Tests', [
        { name: 'Double-bar help', fn: testDoubleBarHelp },
        { name: 'Double-bar add first series', fn: testDoubleBarAddFirstSeries },
        { name: 'Double-bar add second series', fn: testDoubleBarAddSecondSeries },
        { name: 'Double-bar query', fn: testDoubleBarQuery },
        { name: 'Double-bar query JSON', fn: testDoubleBarQueryJson },
        { name: 'Double-bar multiple categories', fn: testDoubleBarMultipleCategories },
        { name: 'Double-bar year-over-year', fn: testDoubleBarYearOverYear },
        { name: 'Double-bar with zero values', fn: testDoubleBarWithZeroValues }
    ]);
}

// Export for use in test runner
module.exports = { runDoubleBarTests };

// Run directly if executed
if (require.main === module) {
    runDoubleBarTests().then(({ passed, failed, total }) => {
        process.exit(failed > 0 ? 1 : 0);
    }).catch(err => {
        console.error('Test error:', err);
        process.exit(1);
    });
}
