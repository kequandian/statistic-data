#!/usr/bin/env node
/**
 * Statistic CLI - 统计数据命令行工具
 *
 * 支持功能:
 * - config: 配置管理
 * - meta: 元数据管理
 * - stat: 统计数据查询
 * - group: 分组管理
 * - field: 字段管理
 * - export: 数据导出
 * - chart: 图表预览
 */

const fs = require('fs');
const path = require('path');
const http = require('http');
const https = require('https');

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

const DEFAULT_TIMEOUT = 60000; // 60 seconds
const CONFIG_DIR = path.join(require('os').homedir(), '.config', 'statistic-cli');
const CONFIG_FILE = path.join(CONFIG_DIR, 'config.json');

// ==================== Configuration Management ====================

function loadConfig() {
    try {
        if (fs.existsSync(CONFIG_FILE)) {
            const content = fs.readFileSync(CONFIG_FILE, 'utf8');
            return JSON.parse(content);
        }
    } catch (e) {
        // Ignore errors
    }
    return {};
}

function saveConfig(config) {
    if (!fs.existsSync(CONFIG_DIR)) {
        fs.mkdirSync(CONFIG_DIR, { recursive: true });
    }
    fs.writeFileSync(CONFIG_FILE, JSON.stringify(config, null, 2));
}

// ==================== HTTP Request Wrapper ====================

function httpRequest(options) {
    return new Promise((resolve, reject) => {
        const url = new URL(options.url);
        const isHttps = url.protocol === 'https:';
        const client = isHttps ? https : http;

        const reqOptions = {
            hostname: url.hostname,
            port: url.port || (isHttps ? 443 : 80),
            path: url.pathname + url.search,
            method: options.method || 'GET',
            headers: options.headers || {},
            timeout: options.timeout || DEFAULT_TIMEOUT
        };

        if (options.body) {
            reqOptions.headers['Content-Length'] = Buffer.byteLength(options.body);
        }

        if (options.verbose) {
            console.error(`[DEBUG] ${reqOptions.method} ${options.url}`);
            if (options.params) {
                console.error(`[DEBUG] Params: ${JSON.stringify(options.params)}`);
            }
        }

        const req = client.request(reqOptions, (res) => {
            let data = '';

            res.on('data', chunk => {
                data += chunk;
            });

            res.on('end', () => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    const contentType = res.headers['content-type'] || '';
                    if (contentType.includes('application/json')) {
                        try {
                            resolve(JSON.parse(data));
                        } catch (e) {
                            resolve(data);
                        }
                    } else {
                        resolve({ data: data, headers: res.headers });
                    }
                } else {
                    const error = new Error(`HTTP ${res.statusCode}`);
                    error.statusCode = res.statusCode;
                    error.response = data;
                    try {
                        error.responseJson = JSON.parse(data);
                    } catch (e) {
                        // Ignore
                    }
                    reject(error);
                }
            });
        });

        req.on('timeout', () => {
            req.destroy();
            reject(new Error(`Request timeout (${reqOptions.timeout}ms)`));
        });

        req.on('error', (err) => {
            if (err.code === 'ECONNREFUSED') {
                reject(new Error(`Cannot connect to server: ${options.url}`));
            } else {
                reject(err);
            }
        });

        if (options.body) {
            req.write(options.body);
        }

        req.end();
    });
}

// ==================== API Client ====================

class StatisticAPIClient {
    constructor(options = {}) {
        const config = loadConfig();

        this.baseUrl = (options.baseUrl ||
                        config.base_url ||
                        process.env.STATISTIC_BASE_URL ||
                        process.env.STATISTIC_CLI_BASE_URL || 'http://localhost:8080/api/adm/stat').replace(/\/$/, '');

        this.token = options.token || config.token || process.env.STATISTIC_TOKEN;
        this.timeout = options.timeout || DEFAULT_TIMEOUT;
        this.verbose = options.verbose || false;

        this.headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        if (this.token) {
            this.headers['Authorization'] = `Bearer ${this.token}`;
        }
    }

    async _request(method, endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const params = new URLSearchParams(options.params || {});
        const queryString = params.toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;
        const silentError = options.silentError || false;

        try {
            return await httpRequest({
                url: fullUrl,
                method: method,
                headers: this.headers,
                timeout: this.timeout,
                verbose: this.verbose,
                body: options.body,
                params: options.params
            });
        } catch (error) {
            if (!silentError) {
                if (error.message.includes('timeout')) {
                    console.error(`Error: Request timeout (${this.timeout / 1000}s)`);
                } else if (error.message.includes('Cannot connect')) {
                    console.error(`Error: ${error.message}`);
                    console.error(`URL: ${url}`);
                } else if (error.statusCode) {
                    console.error(`Error: HTTP ${error.statusCode}`);
                    if (error.responseJson) {
                        console.error(`Details: ${JSON.stringify(error.responseJson)}`);
                    }
                } else {
                    console.error(`Request failed: ${error.message}`);
                }
            }
            throw error;
        }
    }

    // ==================== Meta API ====================
    async getMeta(field) {
        return this._request('GET', `/meta/${encodeURIComponent(field)}`);
    }

    async listMeta() {
        return this._request('GET', '/meta/list');
    }

    // ==================== Stat API ====================
    async getStatisticByGroup(group, identifier = null) {
        const params = {};
        if (identifier) {
            params.identifier = identifier;
        }
        return this._request('GET', `/groups/${encodeURIComponent(group)}`, { params });
    }

    // ==================== Group API ====================
    async listGroups(options = {}) {
        return this._request('GET', '/groups', options);
    }

    async getGroup(groupName, options = {}) {
        return this._request('GET', `/groups/${encodeURIComponent(groupName)}`, options);
    }

    // ==================== Field API ====================
    async listFields(options = {}) {
        return this._request('GET', '/fields', options);
    }

    async getField(field, options = {}) {
        return this._request('GET', `/fields/${encodeURIComponent(field)}`, options);
    }

    // ==================== Export API ====================
    async exportPdf(reportName) {
        const pdfUrl = this.baseUrl.replace('/api/adm/stat', '/api/io/pdf') + `/export/${encodeURIComponent(reportName)}`;
        return httpRequest({
            url: pdfUrl,
            method: 'GET',
            headers: this.headers,
            timeout: this.timeout
        });
    }

    async exportExcel(field) {
        return this._request('GET', `/meta/${encodeURIComponent(field)}/export/excel`);
    }
}

// ==================== Output Formatters ====================

function printJson(data, pretty = true) {
    if (pretty) {
        console.log(JSON.stringify(data, null, 2));
    } else {
        console.log(JSON.stringify(data));
    }
}

function printTable(headers, rows) {
    const widths = headers.map((h, i) => {
        const maxWidth = Math.max(
            h.length,
            ...rows.map(r => String(r[i] || '').length)
        );
        return maxWidth + 2;
    });

    // Print header
    console.log();
    headers.forEach((h, i) => {
        process.stdout.write((h + ' '.repeat(widths[i])).slice(0, widths[i]));
    });
    console.log();

    // Print separator
    headers.forEach((_, i) => {
        process.stdout.write('-'.repeat(widths[i] - 1) + ' ');
    });
    console.log();

    // Print rows
    rows.forEach(row => {
        headers.forEach((_, i) => {
            const cell = String(row[i] || '');
            process.stdout.write((cell + ' '.repeat(widths[i])).slice(0, widths[i]));
        });
        console.log();
    });
    console.log();
}

function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
}

function formatTimestamp(ts) {
    if (!ts) return 'N/A';
    return new Date(ts).toLocaleString();
}

// ==================== Chart Rendering (ASCII) ====================

function renderAsciiChart(data, type = 'bar', width = 60) {
    if (!data || !Array.isArray(data) || data.length === 0) {
        console.log('No data to display');
        return;
    }

    switch (type) {
        case 'pie':
            renderAsciiPie(data);
            break;
        case 'line':
            renderAsciiLine(data, width);
            break;
        default:
            renderAsciiBar(data, width);
    }
}

function renderAsciiBar(data, width) {
    const maxValue = Math.max(...data.map(d => d.value || 0));
    const barWidth = width - 15;

    console.log();
    data.forEach(item => {
        const value = item.value || 0;
        const barLength = maxValue > 0 ? Math.round((value / maxValue) * barWidth) : 0;
        const bar = '█'.repeat(barLength);
        const label = (item.name || item.label || '').padEnd(12);
        const valueStr = String(value).padStart(8);
        console.log(`  ${label} │${bar}${valueStr}`);
    });
    console.log();
}

function renderAsciiPie(data) {
    const total = data.reduce((sum, d) => sum + (d.value || 0), 0);
    if (total === 0) {
        console.log('No data to display');
        return;
    }

    console.log();
    console.log('  ' + '●'.repeat(20));
    data.forEach((item, i) => {
        const value = item.value || 0;
        const percent = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
        const symbols = Math.round((value / total) * 20);
        const symbol = ['●', '○', '◐', '◑', '◒'][i % 5];
        console.log(`  ${symbol} ${item.name || item.label}: ${value} (${percent}%)`);
    });
    console.log();
}

function renderAsciiLine(data, width) {
    const maxValue = Math.max(...data.map(d => d.value || 0));
    const minValue = Math.min(...data.map(d => d.value || 0));
    const range = maxValue - minValue || 1;
    const height = 10;

    console.log();
    // Y-axis labels
    for (let i = height; i >= 0; i--) {
        const yValue = minValue + (range * i / height);
        const yLabel = yValue.toFixed(0).padStart(6);
        process.stdout.write(`${yLabel} │`);

        data.forEach(item => {
            const value = item.value || 0;
            const normalized = (value - minValue) / range;
            const shouldDraw = Math.round(normalized * height) >= i;
            process.stdout.write(shouldDraw ? '●' : ' ');
        });
        console.log();
    }

    // X-axis
    process.stdout.write('       └');
    console.log('─'.repeat(Math.min(data.length, width)));
    console.log();

    // X-axis labels
    process.stdout.write('         ');
    data.forEach((item, i) => {
        if (i % Math.ceil(data.length / 10) === 0) {
            const label = String(item.name || item.label || '').slice(0, 1);
            process.stdout.write(label + ' '.repeat(Math.ceil(data.length / 10) - 1));
        }
    });
    console.log();
    console.log();
}

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
        yaml: false,
        csv: false,
        table: false,
        verbose: false,
        output: null
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
            case '--yaml':
                options.yaml = true;
                break;
            case '--csv':
                options.csv = true;
                break;
            case '--table':
                options.table = true;
                break;
            case '-v':
            case '--verbose':
                options.verbose = true;
                break;
            case '-o':
            case '--output':
                options.output = args[++i];
                break;
            case '-h':
            case '--help':
                options.showHelp = true;
                break;
        }
    }

    return options;
}

function getNamedArg(args, name, alias = null) {
    const idx = args.indexOf(name);
    if (idx === -1 && alias) {
        const aliasIdx = args.indexOf(alias);
        if (aliasIdx !== -1) return args[aliasIdx + 1];
    }
    return idx !== -1 ? args[idx + 1] : null;
}

function hasFlag(args, ...names) {
    return names.some(name => args.includes(name));
}

// ==================== Help ====================

function showHelp(command = null) {
    if (!command) {
        console.log(`
Statistic CLI v2.0.0 - 统计数据命令行工具

USAGE:
  statistic-cli [options] <command> [args]

GLOBAL OPTIONS:
  --url <url>         API base URL
  --token <token>     Auth token
  --timeout <sec>     Request timeout (default: 60s)
  -v, --verbose       Debug mode
  -h, --help          Show help

COMMANDS:
  config              配置管理
  meta                元数据管理
  stat                统计数据查询
  group               分组管理
  field               字段管理
  export              数据导出
  chart               图表预览

Use 'statistic-cli <command> --help' for command-specific help.
`);
    } else {
        showCommandHelp(command);
    }
}

function showCommandHelp(command) {
    const helps = {
        config: `
CONFIG COMMAND - 配置管理

USAGE:
  statistic-cli config <subcommand> [args]

SUBCOMMANDS:
  show                显示当前配置
  set <key> <value>   设置配置项
  get <key>           获取配置项
  list                列出所有配置
  clear               清除配置
  test                测试连接

EXAMPLES:
  statistic-cli config show
  statistic-cli config set base_url http://localhost:8080/api/adm/stat
  statistic-cli config set token your-token-here
  statistic-cli config get base_url
  statistic-cli config test
`,
        meta: `
META COMMAND - 元数据管理

USAGE:
  statistic-cli meta <subcommand> [args]

SUBCOMMANDS:
  show <field>        显示元数据详情
  list [--pattern]    列出所有元数据
  sql <field>         显示元数据的 SQL
  info <field>        显示元数据统计信息

OPTIONS:
  --json              JSON 格式输出
  --yaml              YAML 格式输出

EXAMPLES:
  statistic-cli meta show daily_report
  statistic-cli meta list
  statistic-cli meta list --pattern user
  statistic-cli meta sql user_statistics
  statistic-cli meta info daily_report
`,
        stat: `
STAT COMMAND - 统计数据查询

USAGE:
  statistic-cli stat <subcommand> [args]

SUBCOMMANDS:
  get <group>         获取分组统计数据
  query <field>       实时查询字段数据
  compare <field>     历史数据对比
  trend <field>       趋势分析

OPTIONS:
  --identifier <id>   标识符过滤
  --filter <k=v>      过滤条件
  --threshold <val>   阈值设置
  --from <date>       起始日期
  --to <date>         结束日期
  --period <7d|30d>   时间周期
  --json              JSON 格式输出
  --csv               CSV 格式输出
  --table             表格格式输出

EXAMPLES:
  statistic-cli stat get user_stats
  statistic-cli stat get user_stats --identifier 123
  statistic-cli stat query login_count --threshold 1000
  statistic-cli stat compare daily_report --from 2024-01-01 --to 2024-01-31
  statistic-cli stat trend user_growth --period 30d
`,
        group: `
GROUP COMMAND - 分组管理

USAGE:
  statistic-cli group <subcommand> [args]

SUBCOMMANDS:
  list [--tree]       列出所有分组
  show <group>        显示分组详情
  fields <group>      显示分组下的字段

OPTIONS:
  --json              JSON 格式输出

EXAMPLES:
  statistic-cli group list
  statistic-cli group list --tree
  statistic-cli group show user_stats
  statistic-cli group fields user_stats
`,
        field: `
FIELD COMMAND - 字段管理

USAGE:
  statistic-cli field <subcommand> [args]

SUBCOMMANDS:
  list [--group]      列出所有字段
  show <field>        显示字段详情
  summary <field>     显示字段统计摘要

OPTIONS:
  --json              JSON 格式输出

EXAMPLES:
  statistic-cli field list
  statistic-cli field list --group user_stats
  statistic-cli field show user_login_count
  statistic-cli field summary success_rate
`,
        export: `
EXPORT COMMAND - 数据导出

USAGE:
  statistic-cli export <format> <source> [args]

FORMATS:
  pdf <report>        导出 PDF 报告
  excel <field>       导出 Excel 文件
  data <group>        导出 JSON 数据

OPTIONS:
  -o, --output <file> 输出文件路径
  --format <format>   数据格式 (pretty|compact)

EXAMPLES:
  statistic-cli export pdf daily_report -o report.pdf
  statistic-cli export excel user_statistics -o data.xlsx
  statistic-cli export data user_stats -o stats.json
`,
        chart: `
CHART COMMAND - 图表预览

USAGE:
  statistic-cli chart <subcommand> [args]

SUBCOMMANDS:
  preview <field>     生成图表预览
  compare <fields>    比较多个指标

OPTIONS:
  --type <type>       图表类型 (pie|bar|line)
  --output <file>     输出到文件
  --period <7d|30d>   时间周期

EXAMPLES:
  statistic-cli chart preview user_distribution --type pie
  statistic-cli chart preview login_count --type bar
  statistic-cli chart compare field1,field2,field3 --type bar
`
    };

    console.log(helps[command] || `Unknown command: ${command}`);
}

// ==================== Command Handlers ====================

async function handleConfig(args, options) {
    const config = loadConfig();
    const subcommand = args[0];

    switch (subcommand) {
        case 'show':
            console.log('Current configuration:');
            console.log(JSON.stringify(config, null, 2));
            break;

        case 'set':
            if (args.length < 3) {
                console.error('Usage: statistic-cli config set <key> <value>');
                process.exit(1);
            }
            config[args[1]] = args[2];
            saveConfig(config);
            console.log(`✓ Set: ${args[1]} = ${args[2]}`);
            break;

        case 'get':
            if (args.length < 2) {
                console.error('Usage: statistic-cli config get <key>');
                process.exit(1);
            }
            const value = config[args[1]];
            if (value !== undefined) {
                console.log(value);
            } else {
                console.log(`Key '${args[1]}' not found in config`);
            }
            break;

        case 'list':
            console.log('Configuration:');
            Object.entries(config).forEach(([k, v]) => {
                console.log(`  ${k}: ${v}`);
            });
            break;

        case 'clear':
            if (fs.existsSync(CONFIG_FILE)) {
                fs.unlinkSync(CONFIG_FILE);
                console.log('✓ Configuration cleared');
            } else {
                console.log('No configuration file found');
            }
            break;

        case 'test':
            console.log('Testing connection...');
            try {
                const client = new StatisticAPIClient({
                    baseUrl: options.baseUrl,
                    token: options.token,
                    verbose: options.verbose
                });
                await client._request('GET', '/meta');
                console.log('✓ Connection successful');
            } catch (err) {
                console.error(`✗ Connection failed: ${err.message}`);
                process.exit(1);
            }
            break;

        default:
            showCommandHelp('config');
            break;
    }
}

async function handleMeta(args, options, client) {
    const subcommand = args[0];

    switch (subcommand) {
        case 'show': {
            const field = args[1];
            if (!field) {
                console.error('Usage: statistic-cli meta show <field>');
                process.exit(1);
            }
            const result = await client.getMeta(field);
            if (options.json) {
                printJson(result);
            } else {
                console.log(`\n=== Metadata: ${field} ===`);
                console.log(JSON.stringify(result, null, 2));
            }
            break;
        }

        case 'list': {
            const pattern = getNamedArg(args, '--pattern');
            console.log('Metadata list:');
            console.log('(Note: This endpoint may not be available on all servers)');
            try {
                const result = await client.listMeta();
                printJson(result, true);
            } catch (err) {
                // Handle all errors gracefully for list command
                if (err.statusCode === 404) {
                    console.log('Endpoint not available. Use "meta show <field>" for specific queries.');
                } else if (err.message.includes('Cannot connect') || err.message.includes('timeout')) {
                    console.log('Cannot connect to server. Use "meta show <field>" for specific queries.');
                } else {
                    console.log(`Error: ${err.message}`);
                    console.log('Use "meta show <field>" for specific queries.');
                }
            }
            break;
        }

        case 'sql': {
            const field = args[1];
            if (!field) {
                console.error('Usage: statistic-cli meta sql <field>');
                process.exit(1);
            }
            const result = await client.getMeta(field);
            const meta = result.data || result;
            if (meta.sql) {
                console.log(`\n-- SQL for ${field} --`);
                console.log(meta.sql);
            } else {
                console.log('No SQL found in metadata');
            }
            break;
        }

        case 'info': {
            const field = args[1];
            if (!field) {
                console.error('Usage: statistic-cli meta info <field>');
                process.exit(1);
            }
            const result = await client.getMeta(field);
            const meta = result.data || result;

            console.log(`\n=== Field Info: ${field} ===`);
            console.log(`Chart Type: ${meta.chartType || 'N/A'}`);
            console.log(`Data Pattern: ${meta.dataPattern || 'N/A'}`);
            console.log(`Timeline: ${meta.timeline ? 'Enabled' : 'Disabled'}`);
            console.log(`Cluster: ${meta.cluster ? 'Enabled' : 'Disabled'}`);
            if (meta.description) {
                console.log(`Description: ${meta.description}`);
            }
            break;
        }

        default:
            showCommandHelp('meta');
            break;
    }
}

async function handleStat(args, options, client) {
    const subcommand = args[0];

    switch (subcommand) {
        case 'get': {
            const group = args[1];
            if (!group) {
                console.error('Usage: statistic-cli stat get <group>');
                process.exit(1);
            }
            const identifier = getNamedArg(args, '--identifier');
            const result = await client.getStatisticByGroup(group, identifier);

            if (options.json) {
                printJson(result);
            } else if (options.table) {
                const data = result.data || result;
                if (Array.isArray(data)) {
                    const keys = Object.keys(data[0] || {});
                    const rows = data.map(item => keys.map(k => item[k]));
                    printTable(keys, rows);
                }
            } else {
                console.log(`\n=== Statistics: ${group} ===`);
                if (identifier) {
                    console.log(`Identifier: ${identifier}`);
                }
                console.log(JSON.stringify(result, null, 2));
            }
            break;
        }

        case 'query': {
            const field = args[1];
            if (!field) {
                console.error('Usage: statistic-cli stat query <field>');
                process.exit(1);
            }
            const threshold = getNamedArg(args, '--threshold');
            const filter = getNamedArg(args, '--filter');

            console.log(`\n=== Query: ${field} ===`);
            if (filter) {
                console.log(`Filter: ${filter}`);
            }
            if (threshold) {
                console.log(`Threshold: ${threshold}`);
            }

            const result = await client.getMeta(field);
            if (options.json) {
                printJson(result);
            } else {
                console.log(JSON.stringify(result, null, 2));
            }
            break;
        }

        case 'compare': {
            const field = args[1];
            if (!field) {
                console.error('Usage: statistic-cli stat compare <field> --from <date> --to <date>');
                process.exit(1);
            }
            const fromDate = getNamedArg(args, '--from');
            const toDate = getNamedArg(args, '--to');

            console.log(`\n=== Compare: ${field} ===`);
            console.log(`From: ${fromDate || 'Not specified'}`);
            console.log(`To: ${toDate || 'Not specified'}`);
            console.log('(Note: This feature requires backend support)');
            break;
        }

        case 'trend': {
            const field = args[1];
            if (!field) {
                console.error('Usage: statistic-cli stat trend <field> --period <7d|30d>');
                process.exit(1);
            }
            const period = getNamedArg(args, '--period') || '7d';

            console.log(`\n=== Trend: ${field} (${period}) ===`);
            console.log('(Note: This feature requires backend support)');
            break;
        }

        default:
            showCommandHelp('stat');
            break;
    }
}

async function handleGroup(args, options, client) {
    const subcommand = args[0];

    switch (subcommand) {
        case 'list': {
            const treeMode = hasFlag(args, '--tree');
            if (!options.json) {
                console.log('\n=== Statistics Groups ===');
            }
            try {
                const result = await client.listGroups({ silentError: true });
                if (options.json) {
                    printJson(result);
                } else {
                    const groups = result.data || result;
                    if (Array.isArray(groups)) {
                        groups.forEach(g => {
                            const prefix = treeMode && g.parent ? '  └─ ' : '';
                            console.log(`${prefix}${g.name || g.groupName || g}`);
                        });
                    } else {
                        printJson(result, true);
                    }
                }
            } catch (err) {
                if (options.json) {
                    console.error(JSON.stringify({
                        error: err.message,
                        url: client.baseUrl + '/groups'
                    }, null, 2));
                    process.exit(1);
                } else {
                    if (err.message.includes('Cannot connect') || err.message.includes('timeout')) {
                        console.log('Cannot connect to server.');
                        if (err.message.includes('Cannot connect')) {
                            const url = client.baseUrl + '/groups';
                            console.log(`URL: ${url}`);
                        }
                    }
                    console.log('(Note: This endpoint may not be available on all servers)');
                }
            }
            break;
        }

        case 'show': {
            const group = args[1];
            if (!group) {
                console.error('Usage: statistic-cli group show <group>');
                process.exit(1);
            }
            try {
                const result = await client.getGroup(group, { silentError: true });
                if (options.json) {
                    printJson(result);
                } else {
                    console.log(`\n=== Group: ${group} ===`);
                    console.log(JSON.stringify(result, null, 2));
                }
            } catch (err) {
                if (options.json) {
                    console.error(JSON.stringify({
                        error: err.message,
                        url: client.baseUrl + '/groups/' + encodeURIComponent(group)
                    }, null, 2));
                } else {
                    console.error(`Error: ${err.message}`);
                    if (err.message.includes('Cannot connect')) {
                        console.error(`URL: ${client.baseUrl}/groups/${encodeURIComponent(group)}`);
                    }
                }
                process.exit(1);
            }
            break;
        }

        case 'fields': {
            const group = args[1];
            if (!group) {
                console.error('Usage: statistic-cli group fields <group>');
                process.exit(1);
            }
            if (!options.json) {
                console.log(`\n=== Fields in group: ${group} ===`);
            }
            console.log('(Note: This endpoint may not be available on all servers)');
            break;
        }

        default:
            showCommandHelp('group');
            break;
    }
}

async function handleField(args, options, client) {
    const subcommand = args[0];

    switch (subcommand) {
        case 'list': {
            const group = getNamedArg(args, '--group');
            if (!options.json) {
                console.log('\n=== Statistics Fields ===');
                if (group) {
                    console.log(`Group: ${group}`);
                }
            }
            try {
                const result = await client.listFields({ silentError: true });
                if (options.json) {
                    printJson(result);
                } else {
                    const fields = result.data || result;
                    if (Array.isArray(fields)) {
                        fields.forEach(f => {
                            console.log(`  - ${f.name || f.fieldName || f}`);
                        });
                    } else {
                        printJson(result, true);
                    }
                }
            } catch (err) {
                if (options.json) {
                    console.error(JSON.stringify({
                        error: err.message,
                        url: client.baseUrl + '/fields'
                    }, null, 2));
                    process.exit(1);
                } else {
                    if (err.message.includes('Cannot connect') || err.message.includes('timeout')) {
                        console.log('Cannot connect to server.');
                        if (err.message.includes('Cannot connect')) {
                            const url = client.baseUrl + '/fields';
                            console.log(`URL: ${url}`);
                        }
                    }
                    console.log('(Note: This endpoint may not be available on all servers)');
                }
            }
            break;
        }

        case 'show': {
            const field = args[1];
            if (!field) {
                console.error('Usage: statistic-cli field show <field>');
                process.exit(1);
            }
            try {
                const result = await client.getField(field, { silentError: true });
                if (options.json) {
                    printJson(result);
                } else {
                    console.log(`\n=== Field: ${field} ===`);
                    console.log(JSON.stringify(result, null, 2));
                }
            } catch (err) {
                if (options.json) {
                    console.error(JSON.stringify({
                        error: err.message,
                        url: client.baseUrl + '/fields/' + encodeURIComponent(field)
                    }, null, 2));
                } else {
                    console.error(`Error: ${err.message}`);
                    if (err.message.includes('Cannot connect')) {
                        console.error(`URL: ${client.baseUrl}/fields/${encodeURIComponent(field)}`);
                    }
                }
                process.exit(1);
            }
            break;
        }

        case 'summary': {
            const field = args[1];
            if (!field) {
                console.error('Usage: statistic-cli field summary <field>');
                process.exit(1);
            }
            console.log(`\n=== Summary: ${field} ===`);
            console.log('(Note: This feature requires backend support)');
            break;
        }

        default:
            showCommandHelp('field');
            break;
    }
}

async function handleExport(args, options, client) {
    const format = args[0];
    const source = args[1];

    if (!format || !source) {
        console.error('Usage: statistic-cli export <format> <source>');
        process.exit(1);
    }

    const outputPath = options.output;
    const dataFormat = getNamedArg(args, '--format') || 'pretty';

    switch (format) {
        case 'pdf': {
            console.log(`Exporting PDF: ${source}...`);
            try {
                const result = await client.exportPdf(source);

                if (result.data && result.headers) {
                    const filename = outputPath || (source.endsWith('.pdf') ? source : `${source}.pdf`);
                    fs.writeFileSync(filename, result.data, 'binary');
                    console.log(`✓ Exported to: ${filename} (${formatFileSize(result.data.length)})`);
                } else {
                    console.error('✗ Failed to export PDF: Invalid response');
                    process.exit(1);
                }
            } catch (err) {
                console.error(`✗ Failed to export PDF: ${err.message}`);
                process.exit(1);
            }
            break;
        }

        case 'excel': {
            console.log(`Exporting Excel: ${source}...`);
            try {
                const result = await client._request('GET', `/meta/${encodeURIComponent(source)}/export/excel`, { silentError: true });
                console.log(`✓ Excel export initiated`);
                if (options.json) {
                    printJson(result);
                }
            } catch (err) {
                console.error(`✗ Failed to export Excel: ${err.message}`);
                process.exit(1);
            }
            break;
        }

        case 'data': {
            console.log(`Exporting data: ${source}...`);
            try {
                const result = await client._request('GET', `/groups/${encodeURIComponent(source)}`, { silentError: true });
                const filename = outputPath || `${source}.json`;

                const jsonOutput = dataFormat === 'pretty'
                    ? JSON.stringify(result, null, 2)
                    : JSON.stringify(result);

                fs.writeFileSync(filename, jsonOutput);
                console.log(`✓ Exported to: ${filename}`);
            } catch (err) {
                console.error(`✗ Failed to export data: ${err.message}`);
                process.exit(1);
            }
            break;
        }

        default:
            console.error(`Unknown export format: ${format}`);
            console.log('Supported formats: pdf, excel, data');
            process.exit(1);
    }
}

async function handleChart(args, options, client) {
    const subcommand = args[0];

    switch (subcommand) {
        case 'preview': {
            const field = args[1];
            if (!field) {
                console.error('Usage: statistic-cli chart preview <field>');
                process.exit(1);
            }
            const type = getNamedArg(args, '--type') || 'bar';

            console.log(`\n=== Chart Preview: ${field} (${type}) ===`);

            // Mock data for demo - in real implementation, fetch from API
            const mockData = [
                { name: 'Category A', value: 120 },
                { name: 'Category B', value: 85 },
                { name: 'Category C', value: 150 },
                { name: 'Category D', value: 60 },
                { name: 'Category E', value: 95 }
            ];

            renderAsciiChart(mockData, type);
            break;
        }

        case 'compare': {
            const fieldsStr = args[1];
            if (!fieldsStr) {
                console.error('Usage: statistic-cli chart compare <field1,field2,field3>');
                process.exit(1);
            }
            const type = getNamedArg(args, '--type') || 'bar';

            console.log(`\n=== Chart Comparison (${type}) ===`);
            console.log(`Fields: ${fieldsStr}`);

            // Mock comparison data
            const mockData = [
                { name: 'Metric A', value: 120 },
                { name: 'Metric B', value: 85 },
                { name: 'Metric C', value: 150 }
            ];

            renderAsciiChart(mockData, type);
            break;
        }

        default:
            showCommandHelp('chart');
            break;
    }
}

// ==================== Main ====================

async function main() {
    const options = parseArgs();

    if (options.showHelp || !options.command) {
        showHelp(options.command);
        process.exit(0);
    }

    // Handle config command (doesn't need API client)
    if (options.command === 'config') {
        await handleConfig(options.commandArgs, options);
        return;
    }

    // Create API client for other commands
    let client;
    try {
        client = new StatisticAPIClient({
            baseUrl: options.baseUrl,
            token: options.token,
            timeout: options.timeout,
            verbose: options.verbose
        });
    } catch (err) {
        console.error(`Error: ${err.message}`);
        showHelp();
        process.exit(1);
    }

    const args = options.commandArgs || [];

    // Check for command-specific help
    if (hasFlag(args, '--help', '-h')) {
        showCommandHelp(options.command);
        return;
    }

    // Route to command handlers
    switch (options.command) {
        case 'meta':
            await handleMeta(args, options, client);
            break;
        case 'stat':
            await handleStat(args, options, client);
            break;
        case 'group':
            await handleGroup(args, options, client);
            break;
        case 'field':
            await handleField(args, options, client);
            break;
        case 'export':
            await handleExport(args, options, client);
            break;
        case 'chart':
            await handleChart(args, options, client);
            break;
        default:
            console.error(`Unknown command: ${options.command}`);
            showHelp();
            process.exit(1);
    }
}

// Run main function
main().catch(err => {
    console.error(`Error: ${err.message}`);
    if (err.stack && process.env.DEBUG) {
        console.error(err.stack);
    }
    process.exit(1);
});
