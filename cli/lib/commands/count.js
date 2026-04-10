/**
 * Count Command Handler
 * Handles count related commands
 */

const { StatsAPIClient } = require('../api/client');

/**
 * Handle count command
 * @param {Array} args - Command arguments
 * @param {Object} options - Command options
 */
async function handleCount(args, options) {
    const client = new StatsAPIClient({
        baseUrl: options.baseUrl,
        token: options.token,
        timeout: options.timeout,
        verbose: options.verbose
    });

    const fieldName = args[0];

    if (!fieldName) {
        console.error('Usage: stats-cli count <name> with <value> [--json]');
        console.error('   or: stats-cli count <name> --sql "<query>" [--json]');
        console.error('   or: stats-cli count <name> [--json]');
        process.exit(1);
    }

    // Check if SQL mode
    const sqlIndex = args.indexOf('--sql');
    if (sqlIndex !== -1 && args[sqlIndex + 1]) {
        const sql = args[sqlIndex + 1];
        console.log('SQL mode for count is not yet implemented.');
        console.log(`SQL: ${sql}`);
        return;
    }

    // Check if adding data (with <value>)
    const withIndex = args.indexOf('with');

    if (withIndex !== -1) {
        // Adding data: stats-cli count <name> with 1523
        const value = args[withIndex + 1];

        if (value === undefined) {
            console.error('Usage: stats-cli count <name> with <value>');
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
            console.log(`Count data inserted successfully for field '${fieldName}'`);
            console.log(`Value: ${value}`);
        } catch (error) {
            console.error(`Error inserting data: ${error.message}`);
            process.exit(1);
        }
    } else {
        // Query mode: stats-cli count <name>
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
                    console.log(`\n=== Count: ${fieldName} ===`);
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

module.exports = { handleCount };
