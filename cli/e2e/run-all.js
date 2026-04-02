#!/usr/bin/env node
/**
 * E2E Test Runner
 * Runs all E2E tests for stats-cli
 */

const {
    section, log
} = require('./test-utils');

const { runPieTests } = require('./pie.test');
const { runBarTests } = require('./bar.test');
const { runCountTests } = require('./count.test');
const { runDoubleBarTests } = require('./double-bar.test');

async function main() {
    console.log('');
    log('cyan', '╔════════════════════════════════════════════════════════════╗');
    log('cyan', '║          Stats CLI E2E Test Suite                          ║');
    log('cyan', '╚════════════════════════════════════════════════════════════╝');
    console.log('');

    const startTime = Date.now();
    let totalPassed = 0;
    let totalFailed = 0;
    const results = [];

    // Run all test suites
    const suites = [
        { name: 'Pie Command', fn: runPieTests },
        { name: 'Bar Command', fn: runBarTests },
        { name: 'Count Command', fn: runCountTests },
        { name: 'Double-Bar Command', fn: runDoubleBarTests }
    ];

    for (const suite of suites) {
        try {
            const result = await suite.fn();
            results.push({ suite: suite.name, ...result });
            totalPassed += result.passed;
            totalFailed += result.failed;
        } catch (error) {
            log('red', `Error running ${suite.name}: ${error.message}`);
            results.push({ suite: suite.name, passed: 0, failed: 1, total: 1 });
            totalFailed++;
        }
    }

    const duration = Date.now() - startTime;

    // Print summary
    console.log('');
    console.log('');
    log('cyan', '╔════════════════════════════════════════════════════════════╗');
    log('cyan', '║                      Test Summary                          ║');
    log('cyan', '╚════════════════════════════════════════════════════════════╝');
    console.log('');

    for (const result of results) {
        const status = result.failed === 0 ? '✓' : '✗';
        const color = result.failed === 0 ? 'green' : 'red';
        log(color, `  ${status} ${result.suite}: ${result.passed}/${result.total} passed`);
    }

    console.log('');
    log('cyan', `  Total: ${totalPassed} passed, ${totalFailed} failed`);
    log('cyan', `  Duration: ${(duration / 1000).toFixed(2)}s`);
    console.log('');

    if (totalFailed === 0) {
        log('green', '╔════════════════════════════════════════════════════════════╗');
        log('green', '║                   All Tests Passed! ✓                      ║');
        log('green', '╚════════════════════════════════════════════════════════════╝');
        console.log('');
        process.exit(0);
    } else {
        log('red', '╔════════════════════════════════════════════════════════════╗');
        log('red', '║                   Some Tests Failed ✗                      ║');
        log('red', '╚════════════════════════════════════════════════════════════╝');
        console.log('');
        process.exit(1);
    }
}

main().catch(err => {
    console.error('Fatal error:', err);
    process.exit(1);
});
