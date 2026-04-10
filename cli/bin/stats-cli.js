#!/usr/bin/env node
/**
 * Stats CLI - 统计数据命令行工具
 *
 * 支持功能:
 * - total: 总数统计
 * - pie: 饼图数据管理
 * - bar: 柱状图数据管理
 */

const fs = require('fs');
const path = require('path');

// Load .env file if exists
const dotenv = require('dotenv');
const envPaths = [
    path.join(__dirname, '..', '.env'),
    path.join(process.cwd(), '.env'),
    path.join(require('os').homedir(), '.env')
];
for (const envPath of envPaths) {
    if (fs.existsSync(envPath)) {
        dotenv.config({ path: envPath });
        break;
    }
}

// Import command handlers
const { handleTotal } = require('../lib/commands/total');
const { handlePie } = require('../lib/commands/pie');
const { handleBar } = require('../lib/commands/bar');

// ==================== Argument Parser ====================

function parseArgs() {
    const args = process.argv.slice(2);
    const options = {
        command: null,
        commandArgs: [],
        baseUrl: null,
        token: null,
        timeout: null,
        json: false,
        verbose: false,
        showUrl: false
    };

    // First pass: find the command (skip options and their values)
    for (let i = 0; i < args.length; i++) {
        const arg = args[i];
        if (arg.startsWith('-')) {
            // Skip option value for options that take values
            if (arg === '--url' || arg === '--token' || arg === '--timeout') {
                i++; // Skip next arg (the value)
            }
        } else {
            // Found the command
            options.command = arg;
            options.commandArgs = args.slice(i + 1);
            break;
        }
    }

    // Second pass: parse all options (including values after command)
    for (let i = 0; i < args.length; i++) {
        const arg = args[i];

        switch (arg) {
            case '--url':
                // Check if next arg is a value (not another option)
                const nextArg = args[i + 1];
                if (nextArg && !nextArg.startsWith('-')) {
                    options.baseUrl = nextArg;
                    i++; // Skip next arg
                } else {
                    options.showUrl = true;
                }
                break;
            case '--token':
                options.token = args[++i];
                break;
            case '--timeout':
                options.timeout = parseInt(args[++i]) * 1000;
                break;
            case '--json':
                options.json = true;
                break;
            case '-v':
            case '--verbose':
                options.verbose = true;
                break;
            case '-h':
            case '--help':
                options.showHelp = true;
                break;
        }
    }

    return options;
}

// ==================== Help ====================

function showHelp(command = null) {
    if (!command) {
        console.log(`
Stats CLI v2.0.0 - 统计数据命令行工具

USAGE:
  stats-cli [options] <command> [args]

GLOBAL OPTIONS:
  --url <url>         API base URL
  --token <token>     Auth token
  --timeout <sec>     Request timeout (default: 60s)
  -v, --verbose       Debug mode
  --json              JSON format output
  -h, --help          Show help

COMMANDS:
  total               总数统计
  pie                 饼图数据管理
  bar                 柱状图数据管理

Use 'stats-cli <command> --help' for command-specific help.
`);
    } else {
        showCommandHelp(command);
    }
}

function showCommandHelp(command) {
    const helps = {
        total: `
TOTAL COMMAND - 总数统计

USAGE:
  stats-cli total <name> with <value>
  stats-cli total <name> [--json]

DESCRIPTION:
  添加或查询总数统计数据。

EXAMPLES:
  stats-cli total total_users with 1523
  stats-cli total total_users
  stats-cli total total_users --json
`,
        pie: `
PIE COMMAND - 饼图数据管理

USAGE:
  stats-cli pie <name> add rate "<label>" with <value>
  stats-cli pie <name> to percent [--json]
  stats-cli pie <name> [--json]

DESCRIPTION:
  添加或转换饼图数据：
  - add rate: 添加数据项，不检查总数
  - to percent: 强制将所有数据转换为百分比，总值为100

EXAMPLES:
  stats-cli pie user_dist add rate "Category A" with 40
  stats-cli pie user_dist add rate "Category B" with 60
  stats-cli pie user_dist to percent
  stats-cli pie user_dist
  stats-cli pie user_dist --json
`,
        bar: `
BAR COMMAND - 柱状图数据管理

USAGE:
  stats-cli bar <name> add column "<label>" with <value>
  stats-cli bar <name> [--json]

DESCRIPTION:
  添加柱状图数据。

EXAMPLES:
  stats-cli bar monthly_sales add column "Q1" with 15000
  stats-cli bar monthly_sales add column "Q2" with 23000
  stats-cli bar monthly_sales
  stats-cli bar monthly_sales --json
`
    };

    console.log(helps[command] || `Unknown command: ${command}`);
}

// ==================== Main ====================

async function main() {
    const options = parseArgs();

    if (options.showHelp || !options.command) {
        showHelp(options.command);
        process.exit(0);
    }

    const args = options.commandArgs || [];

    // Check for command-specific help
    if (args.includes('--help') || args.includes('-h')) {
        showCommandHelp(options.command);
        return;
    }

    // Route to command handlers
    try {
        switch (options.command) {
            case 'total':
                await handleTotal(args, options);
                break;
            case 'pie':
                await handlePie(args, options);
                break;
            case 'bar':
                await handleBar(args, options);
                break;
            default:
                console.error(`Unknown command: ${options.command}`);
                showHelp();
                process.exit(1);
        }
    } catch (error) {
        console.error(`Error: ${error.message}`);
        if (error.stack && process.env.DEBUG) {
            console.error(error.stack);
        }
        process.exit(1);
    }
}

// Run main function
main().catch(err => {
    console.error(`Unexpected error: ${err.message}`);
    process.exit(1);
});
