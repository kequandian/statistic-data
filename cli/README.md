# Statistic CLI

统计数据 CLI 工具 - 用于访问统计数据 API

## Installation

```bash
cd /home/ubuntu/workspace/clis/statistic-cli
npm install
npm link
```

## Configuration

Configuration priority: CLI args → config file → environment variables

### Config file location
`~/.config/statistic-cli/config.json`

### Environment variables
- `STATISTIC_BASE_URL` - API base URL
- `STATISTIC_TOKEN` - Auth token

### .env file
```bash
STATISTIC_BASE_URL=http://your-server/api/adm/stat
STATISTIC_TOKEN=your-token
```

## Commands

### meta - Get statistical data metadata
```bash
statistic-cli meta <fileName>
```

### export - Export report as PDF
```bash
statistic-cli export <reportName> [-o output.pdf]
```

### list - List available reports
```bash
statistic-cli list
```

### config - Manage configuration
```bash
statistic-cli config --show           # Show current config
statistic-cli config --set <k> <v>    # Set config value
statistic-cli config --clear          # Clear config
```

## Options

- `--url <url>` - API base URL
- `--token <token>` - Auth token
- `--json` - Output in JSON format
- `-v, --verbose` - Enable verbose output
- `--timeout <sec>` - Request timeout in seconds (default: 60s for PDF export)
- `-o, --output <file>` - Output file path (for export command)
- `--save-config` - Save CLI args to config file

## Examples

```bash
# Get metadata for a report
statistic-cli meta daily_report

# Export report as PDF
statistic-cli export daily_report -o report.pdf

# Export with default filename
statistic-cli export monthly_summary

# List available reports
statistic-cli list

# JSON output
statistic-cli meta daily_report --json
```

## Notes

- PDF export uses a longer default timeout (60 seconds) since report generation can take time
- Use `--timeout` option to increase timeout for large reports
- The `list` command may not be available on all server configurations
