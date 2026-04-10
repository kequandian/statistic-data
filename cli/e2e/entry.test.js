#!/usr/bin/env node
/**
 * E2E Tests for Entry Command
 */

const {
    section, test, success, error, info,
    execCli, execCliCapture,
    assert, assertEqual, assertContains,
    generateTestId, runSuite, sleep, parseJson
} = require('./test-utils');

// Test data
const testGroup = generateTestId();

/**
 * Test: Entry command help
 */
async function testEntryHelp() {
    const result = execCliCapture('entry --help');
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'ENTRY COMMAND', 'Should show entry command help');
    assertContains(result.stdout, 'add <entry-name> with <value>', 'Should show usage');
}

/**
 * Test: Add entry data
 */
async function testEntryAdd() {
    const result = execCliCapture(`entry ${testGroup}_add add errors with 3`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Entry data inserted successfully', 'Should confirm insert');
    assertContains(result.stdout, 'Value: 3', 'Should show value');
}

/**
 * Test: Query entry group
 */
async function testEntryQuery() {
    await sleep(500);

    const result = execCliCapture(`entry ${testGroup}_add`);
    assert(result.success, 'Command should succeed');

    // Parse JSON output
    const data = parseJson(result.stdout);
    assert(data[`${testGroup}_add_errors`] !== undefined, 'Should contain entry data');
    assertEqual(data[`${testGroup}_add_errors`], 3, 'Should have correct value');
}

/**
 * Test: Query with --url option
 */
async function testEntryQueryWithUrl() {
    const result = execCliCapture(`entry ${testGroup}_add --url`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'URL:', 'Should show URL');
    assertContains(result.stdout, '/api/adm/stat/groups/', 'Should contain API path');
}

/**
 * Test: Multiple entries in same group
 */
async function testEntryMultiple() {
    // Add multiple entries
    execCliCapture(`entry ${testGroup}_multi add errors with 3`);
    execCliCapture(`entry ${testGroup}_multi add warning with 4`);
    execCliCapture(`entry ${testGroup}_multi add done with 1`);

    await sleep(500);

    const result = execCliCapture(`entry ${testGroup}_multi`);
    assert(result.success, 'Command should succeed');

    // Parse JSON output
    const data = parseJson(result.stdout);
    assertEqual(data[`${testGroup}_multi_errors`], 3, 'Should have errors value');
    assertEqual(data[`${testGroup}_multi_warning`], 4, 'Should have warning value');
    assertEqual(data[`${testGroup}_multi_done`], 1, 'Should have done value');
}

/**
 * Test: Entry update (overwrite existing)
 */
async function testEntryUpdate() {
    // First insert
    execCliCapture(`entry ${testGroup}_update add count with 100`);
    await sleep(500);

    // Query to verify
    const query1 = execCliCapture(`entry ${testGroup}_update`);
    const data1 = parseJson(query1.stdout);
    assertEqual(data1[`${testGroup}_update_count`], 100, 'Should show initial value');

    // Update with new value
    const result = execCliCapture(`entry ${testGroup}_update add count with 200`);
    assert(result.success, 'Command should succeed');

    await sleep(500);

    // Query to verify update
    const query2 = execCliCapture(`entry ${testGroup}_update`);
    const data2 = parseJson(query2.stdout);
    assertEqual(data2[`${testGroup}_update_count`], 200, 'Should show updated value');
}

/**
 * Test: Entry with zero
 */
async function testEntryWithZero() {
    const result = execCliCapture(`entry ${testGroup}_zero add count with 0`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Entry data inserted successfully', 'Should confirm insert');
}

/**
 * Test: Entry with large number
 */
async function testEntryWithLargeNumber() {
    const result = execCliCapture(`entry ${testGroup}_large add count with 999999`);
    assert(result.success, 'Command should succeed');
    assertContains(result.stdout, 'Entry data inserted successfully', 'Should confirm insert');
}

/**
 * Run all entry tests
 */
async function runEntryTests() {
    return await runSuite('Entry Command E2E Tests', [
        { name: 'Entry help', fn: testEntryHelp },
        { name: 'Entry add', fn: testEntryAdd },
        { name: 'Entry query', fn: testEntryQuery },
        { name: 'Entry query with URL', fn: testEntryQueryWithUrl },
        { name: 'Entry multiple entries', fn: testEntryMultiple },
        { name: 'Entry update', fn: testEntryUpdate },
        { name: 'Entry with zero', fn: testEntryWithZero },
        { name: 'Entry with large number', fn: testEntryWithLargeNumber }
    ]);
}

// Export for use in test runner
module.exports = { runEntryTests };

// Run directly if executed
if (require.main === module) {
    runEntryTests().then(({ passed, failed, total }) => {
        process.exit(failed > 0 ? 1 : 0);
    }).catch(err => {
        console.error('Test error:', err);
        process.exit(1);
    });
}
