/**
 * Total Command Handler
 * Handles total related commands
 */

const { StatsAPIClient } = require('../api/client');

/**
 * Show total command help
 */
function showTotalHelp() {
    console.log(`
TOTAL COMMAND - 总数统计

USAGE:
  statistic-cli total <name> with <value>
  statistic-cli total <name> [--json]

SUBCOMMANDS:
  with <value>                     添加总数数据
  (no args)                        查询总数数据

OPTIONS:
  --json                           JSON 格式输出

DESCRIPTION:
  添加或查询总数统计数据。

EXAMPLES:
  statistic-cli total total_users with 1523
  statistic-cli total total_users
  statistic-cli total total_users --json
`);
}

/**
 * Handle total command
 * @param {Array} args - Command arguments
 * @param {Object} options - Command options
 */
async function handleTotal(args, options) {
    const client = new StatsAPIClient({
        baseUrl: options.baseUrl,
        token: options.token,
        timeout: options.timeout,
        verbose: options.verbose
    });

    const fieldName = args[0];

    if (!fieldName) {
        showTotalHelp();
        return;
    }

    // Check if SQL mode
    const sqlIndex = args.indexOf('--sql');
    if (sqlIndex !== -1 && args[sqlIndex + 1]) {
        const sql = args[sqlIndex + 1];
        console.log('SQL mode for total is not yet implemented.');
        console.log(`SQL: ${sql}`);
        return;
    }

    // Check if adding data (with <value>)
    const withIndex = args.indexOf('with');

    if (withIndex !== -1) {
        // Adding data: stats-cli total <name> with 1523
        const value = args[withIndex + 1];

        if (value === undefined) {
            console.error('Usage: stats-cli total <name> with <value>');
            process.exit(1);
        }

        // Insert the data
        try {
            // Ensure field exists
            await client.ensureFieldExists({
                field: fieldName,
                name: fieldName,
                groupName: 'default',
                pattern: 'Count',
                chart: 'BarTimeline',
                attrRuntime: 0,
                attrInvisible: 0,
                attrSpan: 1,
                attrIndex: 0
            });

            // Insert data as a single chunk
            const chunks = [{
                name: 'total',
                value: String(value)
            }];

            await client.insertData(fieldName, chunks);
            console.log(`Total data inserted successfully for field '${fieldName}'`);
            console.log(`Value: ${value}`);
        } catch (error) {
            console.error(`Error inserting data: ${error.message}`);
            process.exit(1);
        }
    } else {
        // Query mode: stats-cli total <name>
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
                    console.log(`\n=== Total: ${fieldName} ===`);
                    fieldData.records.forEach(record => {
                        console.log(`  ${record.recordName}: ${record.recordValue}`);
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

module.exports = { handleTotal };
