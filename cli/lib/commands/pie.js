/**
 * Pie Command Handler
 * Handles pie chart related commands
 */

const { StatsAPIClient } = require('../api/client');

/**
 * Show pie command help
 */
function showPieHelp() {
    console.log(`
PIE COMMAND - 饼图数据管理

USAGE:
  statistic-cli pie <name> add rate "<label>" with <value>
  statistic-cli pie <name> to percent [--json]
  statistic-cli pie <name> [--json]

SUBCOMMANDS:
  add rate "<label>" with <value>   添加数据项
  to percent                        转换为百分比（总值为100）
  (no args)                         查询饼图数据

OPTIONS:
  --json                            JSON 格式输出

DESCRIPTION:
  添加或转换饼图数据：
  - add rate: 添加数据项，不检查总数
  - to percent: 强制将所有数据转换为百分比，总值为100

EXAMPLES:
  statistic-cli pie user_dist add rate "Category A" with 40
  statistic-cli pie user_dist add rate "Category B" with 60
  statistic-cli pie user_dist to percent
  statistic-cli pie user_dist
  statistic-cli pie user_dist --json
`);
}

/**
 * Handle pie command
 * @param {Array} args - Command arguments
 * @param {Object} options - Command options
 */
async function handlePie(args, options) {
    const client = new StatsAPIClient({
        baseUrl: options.baseUrl,
        token: options.token,
        timeout: options.timeout,
        verbose: options.verbose
    });

    const fieldName = args[0];

    if (!fieldName) {
        showPieHelp();
        return;
    }

    // Check if SQL mode
    const sqlIndex = args.indexOf('--sql');
    if (sqlIndex !== -1 && args[sqlIndex + 1]) {
        const sql = args[sqlIndex + 1];
        console.log('SQL mode for pie is not yet implemented.');
        console.log(`SQL: ${sql}`);
        return;
    }

    // Check for "to percent" command
    const toIndex = args.indexOf('to');
    const percentIndex = args.indexOf('percent');

    if (toIndex !== -1 && percentIndex !== -1 && toIndex < percentIndex) {
        // Convert to percentage: stats-cli pie <name> to percent
        await convertToPercent(client, fieldName, options);
        return;
    }

    // Check if adding data (add rate "<label>" with <value>)
    const addIndex = args.indexOf('add');
    const rateIndex = args.indexOf('rate');
    const withIndex = args.indexOf('with');

    if (addIndex !== -1 && rateIndex !== -1 && withIndex !== -1 && addIndex < rateIndex && rateIndex < withIndex) {
        // Adding data: stats-cli pie <name> add rate "A" with 40
        const label = args[rateIndex + 1];
        const value = parseFloat(args[withIndex + 1]);

        if (!label || isNaN(value)) {
            console.error('Usage: stats-cli pie <name> add rate "<label>" with <value>');
            process.exit(1);
        }

        // Insert the value directly without checking total
        await insertPieData(client, fieldName, label, value);
    } else {
        // Query mode: stats-cli pie <name>
        await queryPieData(client, fieldName, options);
    }
}

/**
 * Convert pie data to percentages (total = 100)
 */
async function convertToPercent(client, fieldName, options) {
    try {
        const result = await client.getStatisticByGroup('default');

        if (!result || !result.data) {
            console.error(`No data found for field '${fieldName}'`);
            return;
        }

        const fieldData = result.data.find(d => d.field === fieldName);
        if (!fieldData || !fieldData.records || fieldData.records.length === 0) {
            console.error(`No data found for field '${fieldName}'`);
            return;
        }

        const records = fieldData.records;

        // Calculate current total
        const currentTotal = records.reduce((sum, r) => sum + parseFloat(r.recordValue || 0), 0);

        console.log(`\n=== Converting to Percentage ===`);
        console.log(`Field: ${fieldName}`);
        console.log(`Current total: ${currentTotal.toFixed(2)}`);
        console.log(`Records: ${records.length}\n`);

        // Calculate ratio to normalize to 100
        const ratio = 100 / currentTotal;

        // Convert all records to percentages
        const convertedItems = records.map(record => {
            const originalValue = parseFloat(record.recordValue);
            const percentValue = Math.round(originalValue * ratio * 100) / 100; // Round to 2 decimals
            console.log(`  ${record.recordName}: ${originalValue} → ${percentValue}%`);
            return {
                name: record.recordName,
                value: percentValue
            };
        });

        const newTotal = convertedItems.reduce((sum, item) => sum + item.value, 0);
        console.log(`\nNew total: ${newTotal.toFixed(2)}%`);

        // Ensure field exists
        await client.ensureFieldExists({
            field: fieldName,
            name: fieldName,
            groupName: 'default',
            pattern: 'Rate',
            chart: 'Pie',
            attrRuntime: 0,
            attrInvisible: 0,
            attrSpan: 1,
            attrIndex: 0
        });

        // Insert converted data
        const chunks = convertedItems.map(item => ({
            name: item.name,
            value: String(item.value)
        }));

        await client.insertData(fieldName, chunks);

        console.log(`\n✓ Data converted to percentage successfully for field '${fieldName}'\n`);

        // Show result
        if (!options.json) {
            console.log(`=== Pie Chart: ${fieldName} (Percentage) ===`);
            convertedItems.forEach(item => {
                console.log(`  ${item.name}: ${item.value.toFixed(2)}%`);
            });
            console.log(`  Total: ${newTotal.toFixed(2)}%\n`);
        } else {
            console.log(JSON.stringify({ field: fieldName, total: newTotal, data: convertedItems }, null, 2));
        }
    } catch (error) {
        console.error(`Error converting to percentage: ${error.message}`);
        process.exit(1);
    }
}

/**
 * Insert a single pie data item
 */
async function insertPieData(client, fieldName, label, value) {
    try {
        // Ensure field exists
        await client.ensureFieldExists({
            field: fieldName,
            name: fieldName,
            groupName: 'default',
            pattern: 'Rate',
            chart: 'Pie',
            attrRuntime: 0,
            attrInvisible: 0,
            attrSpan: 1,
            attrIndex: 0
        });

        // Insert data immediately
        const chunks = [{
            name: label,
            value: String(value)
        }];

        await client.insertData(fieldName, chunks);
        console.log(`✓ Data inserted successfully for field '${fieldName}'`);
        console.log(`  ${label} = ${value}\n`);
    } catch (error) {
        console.error(`Error inserting data: ${error.message}`);
        process.exit(1);
    }
}

/**
 * Query pie data
 */
async function queryPieData(client, fieldName, options) {
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
                console.log(`\n=== Pie Chart: ${fieldName} ===`);
                const total = fieldData.records.reduce((sum, r) => sum + parseFloat(r.recordValue || 0), 0);
                fieldData.records.forEach(record => {
                    const value = parseFloat(record.recordValue || 0);
                    const percent = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
                    console.log(`  ${record.recordName}: ${value} (${percent}%)`);
                });
                console.log(`  Total: ${total.toFixed(2)}`);
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

module.exports = { handlePie };
