#!/usr/bin/env node
/**
 * Stats CLI - 统计数据命令行工具
 *
 * 支持功能:
 * - pie: 饼图数据管理
 * - bar: 柱状图数据管理
 * - count: 总数统计
 * - double-bar: 双柱图数据管理
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
const { handlePie } = require('../lib/commands/pie');
const { handleBar } = require('../lib/commands/bar');
const { handleCount } = require('../lib/commands/count');
const { handleDoubleBar } = require('../lib/commands/double-bar');

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
        verbose: false
    };

    // First pass: find the command
    for (let i = 0; i < args.length; i++) {
        const arg = args[i];
        if (!arg.startsWith('-')) {
            options.command = arg;
            options.commandArgs = args.slice(i + 1);
            break;
        }
    }

    // Second pass: parse all options (including those after command)
    for (let i = 0; i < args.length; i++) {
        const arg = args[i];

        switch (arg) {
            case '--url':
                options.baseUrl = args[++i];
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
  pie                 饼图数据管理
  bar                 柱状图数据管理
  count               总数统计
  double-bar          双柱图数据管理

Use 'stats-cli <command> --help' for command-specific help.
`);
    } else {
        showCommandHelp(command);
    }
}

function showCommandHelp(command) {
    const helps = {
        pie: `
PIE COMMAND - 饼图数据管理

USAGE:
  stats-cli pie <name> rate "<label>" with <value>
  stats-cli pie <name> [--json]

DESCRIPTION:
  添加饼图数据。数据总和必须在 80-120 之间。

EXAMPLES:
  stats-cli pie user_dist rate "Category A" with 40
  stats-cli pie user_dist rate "Category B" with 40
  stats-cli pie user_dist
  stats-cli pie user_dist --json
`,
        bar: `
BAR COMMAND - 柱状图数据管理

USAGE:
  stats-cli bar <name> category "<label>" with <value>
  stats-cli bar <name> [--json]

DESCRIPTION:
  添加柱状图数据。

EXAMPLES:
  stats-cli bar monthly_sales category "Q1" with 15000
  stats-cli bar monthly_sales category "Q2" with 23000
  stats-cli bar monthly_sales
  stats-cli bar monthly_sales --json
`,
        count: `
COUNT COMMAND - 总数统计

USAGE:
  stats-cli count <name> with <value>
  stats-cli count <name> [--json]

DESCRIPTION:
  添加或查询总数统计数据。

EXAMPLES:
  stats-cli count total_users with 1523
  stats-cli count total_users
  stats-cli count total_users --json
`,
        'double-bar': `
DOUBLE-BAR COMMAND - 双柱图数据管理

USAGE:
  stats-cli double-bar <name> series "<series>" "<category>" with <value>
  stats-cli double-bar <name> [--json]

DESCRIPTION:
  添加双柱图数据。需要恰好 2 个系列的数据。

EXAMPLES:
  stats-cli double-bar sales series "2024" "Q1" with 15000
  stats-cli double-bar sales series "2023" "Q1" with 12000
  stats-cli double-bar sales
  stats-cli double-bar sales --json
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
            case 'pie':
                await handlePie(args, options);
                break;
            case 'bar':
                await handleBar(args, options);
                break;
            case 'count':
                await handleCount(args, options);
                break;
            case 'double-bar':
                await handleDoubleBar(args, options);
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
