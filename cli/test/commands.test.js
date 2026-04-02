#!/usr/bin/env node
/**
 * Statistic CLI Commands Test Suite
 *
 * Tests all commands and their arguments
 */

const { spawn } = require('child_process');
const path = require('path');

const CLI_PATH = path.join(__dirname, '..', 'bin', 'statistic-cli.js');

// ANSI color codes
const colors = {
    reset: '\x1b[0m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    magenta: '\x1b[35m',
    cyan: '\x1b[36m',
};

function colorize(color, text) {
    return `${colors[color]}${text}${colors.reset}`;
}

function runCommand(args, options = {}) {
    return new Promise((resolve) => {
        const proc = spawn('node', [CLI_PATH, ...args], {
            cwd: path.join(__dirname, '..'),
            stdio: options.silent ? 'pipe' : 'inherit',
            env: {
                ...process.env,
                STATISTIC_BASE_URL: 'http://localhost:8080/api/adm/stat',
                STATISTIC_TOKEN: 'test-token'
            }
        });

        let stdout = '';
        let stderr = '';

        if (options.silent) {
            proc.stdout.on('data', (data) => {
                stdout += data.toString();
            });
            proc.stderr.on('data', (data) => {
                stderr += data.toString();
            });
        }

        proc.on('close', (code) => {
            resolve({ code, stdout, stderr });
        });
    });
}

// Test cases organized by command
const tests = {
    'help': [
        { name: 'Show main help', args: [], expectCode: 0 },
        { name: 'Show main help with --help', args: ['--help'], expectCode: 0 },
        { name: 'Show main help with -h', args: ['-h'], expectCode: 0 },
    ],

    'config': [
        { name: 'config --help', args: ['config', '--help'], expectCode: 0 },
        { name: 'config show', args: ['config', 'show'], expectCode: 0 },
        { name: 'config list', args: ['config', 'list'], expectCode: 0 },
        { name: 'config get (missing key)', args: ['config', 'get'], expectCode: 1, silent: true },
        { name: 'config set', args: ['config', 'set', 'test_key', 'test_value'], expectCode: 0 },
        { name: 'config get after set', args: ['config', 'get', 'test_key'], expectCode: 0 },
        { name: 'config clear', args: ['config', 'clear'], expectCode: 0 },
    ],

    'meta': [
        { name: 'meta --help', args: ['meta', '--help'], expectCode: 0 },
        { name: 'meta show (missing field)', args: ['meta', 'show'], expectCode: 1, silent: true },
        { name: 'meta list', args: ['meta', 'list'], expectCode: 0, silent: true },
        { name: 'meta sql (missing field)', args: ['meta', 'sql'], expectCode: 1, silent: true },
        { name: 'meta info (missing field)', args: ['meta', 'info'], expectCode: 1, silent: true },
    ],

    'stat': [
        { name: 'stat --help', args: ['stat', '--help'], expectCode: 0 },
        { name: 'stat get (missing group)', args: ['stat', 'get'], expectCode: 1, silent: true },
        { name: 'stat query (missing field)', args: ['stat', 'query'], expectCode: 1, silent: true },
        { name: 'stat compare (missing field)', args: ['stat', 'compare'], expectCode: 1, silent: true },
        { name: 'stat trend (missing field)', args: ['stat', 'trend'], expectCode: 1, silent: true },
    ],

    'group': [
        { name: 'group --help', args: ['group', '--help'], expectCode: 0 },
        { name: 'group list', args: ['group', 'list'], expectCode: 0, silent: true },
        { name: 'group show (missing group)', args: ['group', 'show'], expectCode: 1, silent: true },
        { name: 'group fields (missing group)', args: ['group', 'fields'], expectCode: 1, silent: true },
    ],

    'field': [
        { name: 'field --help', args: ['field', '--help'], expectCode: 0 },
        { name: 'field list', args: ['field', 'list'], expectCode: 0, silent: true },
        { name: 'field show (missing field)', args: ['field', 'show'], expectCode: 1, silent: true },
        { name: 'field summary (missing field)', args: ['field', 'summary'], expectCode: 1, silent: true },
    ],

    'export': [
        { name: 'export --help (as subcommand)', args: ['export'], expectCode: 1, silent: true },
        { name: 'chart --help', args: ['chart', '--help'], expectCode: 0 },
    ],

    'chart': [
        { name: 'chart --help', args: ['chart', '--help'], expectCode: 0 },
        { name: 'chart preview (missing field)', args: ['chart', 'preview'], expectCode: 1, silent: true },
        { name: 'chart compare (missing fields)', args: ['chart', 'compare'], expectCode: 1, silent: true },
    ],

    'invalid': [
        { name: 'unknown command', args: ['unknown-command'], expectCode: 1, silent: true },
    ],
};

async function runTests(category, testList) {
    console.log(`\n${colorize('cyan', '═══════════════════════════════════════')}`);
    console.log(`${colorize('cyan', `  Testing: ${category.toUpperCase()}`)}`);
    console.log(`${colorize('cyan', '═══════════════════════════════════════')}\n`);

    let passed = 0;
    let failed = 0;

    for (const test of testList) {
        const { name, args, expectCode, silent = false } = test;

        process.stdout.write(`  ${colorize('blue', '◍')} ${name}... `);

        try {
            const result = await runCommand(args, { silent });

            if (result.code === expectCode) {
                console.log(colorize('green', '✓ PASS'));
                passed++;
            } else {
                console.log(colorize('red', `✗ FAIL`));
                console.log(`    Expected exit code ${expectCode}, got ${result.code}`);
                if (result.stderr) {
                    console.log(`    Error: ${result.stderr.trim()}`);
                }
                failed++;
            }
        } catch (error) {
            console.log(colorize('red', `✗ ERROR`));
            console.log(`    ${error.message}`);
            failed++;
        }
    }

    console.log(`\n  ${colorize('green', `Passed: ${passed}`)} | ${colorize('red', `Failed: ${failed}`)}`);

    return { passed, failed };
}

async function main() {
    console.log(`\n${colorize('magenta', '╔═══════════════════════════════════════╗')}`);
    console.log(`${colorize('magenta', '║   Statistic CLI Test Suite v2.0.0    ║')}`);
    console.log(`${colorize('magenta', '╚═══════════════════════════════════════╝')}`);

    let totalPassed = 0;
    let totalFailed = 0;

    for (const [category, testList] of Object.entries(tests)) {
        const { passed, failed } = await runTests(category, testList);
        totalPassed += passed;
        totalFailed += failed;
    }

    console.log(`\n${colorize('cyan', '═══════════════════════════════════════')}`);
    console.log(`${colorize('cyan', '  TOTAL RESULTS')}`);
    console.log(`${colorize('cyan', '═══════════════════════════════════════')}`);
    console.log(`  ${colorize('green', `Total Passed: ${totalPassed}`)} | ${colorize('red', `Total Failed: ${totalFailed}`)}`);

    if (totalFailed === 0) {
        console.log(`\n${colorize('green', '✓ All tests passed!')}\n`);
        process.exit(0);
    } else {
        console.log(`\n${colorize('red', `✗ ${totalFailed} test(s) failed`)}\n`);
        process.exit(1);
    }
}

main().catch(err => {
    console.error(colorize('red', `Test suite error: ${err.message}`));
    process.exit(1);
});
