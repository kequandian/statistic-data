/**
 * Bar Command Handler
 * Handles bar chart related commands
 */

const { StatsAPIClient } = require('../api/client');

// Store data in memory for each field
const fieldData = new Map();

/**
 * Show bar command help
 */
function showBarHelp() {
    console.log(`
BAR COMMAND - 柱状图数据管理

USAGE:
  statistic-cli bar <name> add column "<label>" with <value>
  statistic-cli bar <name> [--json]

SUBCOMMANDS:
  add column "<label>" with <value>   添加柱状数据
  (no args)                           查询柱状图数据

OPTIONS:
  --json                              JSON 格式输出

DESCRIPTION:
  添加柱状图数据。

EXAMPLES:
  statistic-cli bar monthly_sales add column "Q1" with 15000
  statistic-cli bar monthly_sales add column "Q2" with 23000
  statistic-cli bar monthly_sales
  statistic-cli bar monthly_sales --json
`);
}

/**
 * Handle bar command
 * @param {Array} args - Command arguments
 * @param {Object} options - Command options
 */
async function handleBar(args, options) {
    const client = new StatsAPIClient({
        baseUrl: options.baseUrl,
        token: options.token,
        timeout: options.timeout,
        verbose: options.verbose
    });

    const fieldName = args[0];

    if (!fieldName) {
        showBarHelp();
        return;
    }

    // Check if SQL mode
    const sqlIndex = args.indexOf('--sql');
    if (sqlIndex !== -1 && args[sqlIndex + 1]) {
        const sql = args[sqlIndex + 1];
        console.log('SQL mode for bar is not yet implemented.');
        console.log(`SQL: ${sql}`);
        return;
    }

    // Check if adding data (add column "<label>" with <value>)
    const addIndex = args.indexOf('add');
    const columnIndex = args.indexOf('column');
    const withIndex = args.indexOf('with');

    if (addIndex !== -1 && columnIndex !== -1 && withIndex !== -1 && addIndex < columnIndex && columnIndex < withIndex) {
        // Adding data: stats-cli bar <name> add column "Q1" with 15000
        const label = args[columnIndex + 1];
        const value = parseFloat(args[withIndex + 1]);

        if (!label || isNaN(value)) {
            console.error('Usage: stats-cli bar <name> add column "<label>" with <value>');
            process.exit(1);
        }

        // Initialize data array for this field if not exists
        if (!fieldData.has(fieldName)) {
            fieldData.set(fieldName, []);
        }

        // Add data
        const items = fieldData.get(fieldName);
        items.push({ name: label, value: value });
        fieldData.set(fieldName, items);

        console.log(`Added data: ${label} = ${value}`);
        console.log(`Total items: ${items.length}`);

        // Insert the data immediately
        try {
            // Ensure field exists
            await client.ensureFieldExists({
                field: fieldName,
                name: fieldName,
                groupName: 'default',
                pattern: 'Timeline',
                chart: 'BarTimeline',
                attrRuntime: 0,
                attrInvisible: 0,
                attrSpan: 1,
                attrIndex: 0
            });

            // Insert data
            const chunks = items.map((item, index) => ({
                name: item.name,
                value: String(item.value),
                seq: index
            }));

            await client.insertData(fieldName, chunks);
            console.log(`Data inserted successfully for field '${fieldName}'`);

            // Clear the stored data after successful insert
            fieldData.delete(fieldName);
        } catch (error) {
            console.error(`Error inserting data: ${error.message}`);
            // Keep the data in memory for retry
        }
    } else {
        // Query mode: stats-cli bar <name>
        try {
            const result = await client.getStatisticByGroup('default');

            if (options.json) {
                console.log(JSON.stringify(result, null, 2));
                return;
            }

            // Find the field data
            if (result && result.data) {
                const fieldData = result.data.find(d => d.field === fieldName);
                if (fieldData && fieldData.records) {
                    console.log(`\n=== Bar Chart: ${fieldName} ===`);
                    const maxValue = Math.max(...fieldData.records.map(r => parseFloat(r.recordValue || 0)));
                    fieldData.records.forEach(record => {
                        const value = parseFloat(record.recordValue || 0);
                        const barLength = maxValue > 0 ? Math.round((value / maxValue) * 30) : 0;
                        const bar = '█'.repeat(barLength);
                        console.log(`  ${record.recordName.padEnd(15)} │${bar} ${value}`);
                    });
                    console.log();
                } else {
                    console.log(`No data found for field '${fieldName}'`);
                }
            } else {
                console.log(`No data found for field '${fieldName}'`);
            }
        } catch (error) {
            console.error(`Error querying data: ${error.message}`);
        }
    }
}

module.exports = { handleBar };
