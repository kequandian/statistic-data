/**
 * Pie Command Handler
 * Handles pie chart related commands
 */

const { StatsAPIClient } = require('../api/client');
const PieValidator = require('../validators/pie');

// Store data in memory for each field
const fieldData = new Map();

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
        console.error('Usage: stats-cli pie <name> [rate "<label>" with <value>] [--json]');
        console.error('   or: stats-cli pie <name> --sql "<query>" [--json]');
        console.error('   or: stats-cli pie <name> [--json]');
        process.exit(1);
    }

    // Check if SQL mode
    const sqlIndex = args.indexOf('--sql');
    if (sqlIndex !== -1 && args[sqlIndex + 1]) {
        const sql = args[sqlIndex + 1];
        console.log('SQL mode for pie is not yet implemented.');
        console.log(`SQL: ${sql}`);
        return;
    }

    // Check if adding data (rate "<label>" with <value>)
    const rateIndex = args.indexOf('rate');
    const withIndex = args.indexOf('with');

    if (rateIndex !== -1 && withIndex !== -1 && rateIndex < withIndex) {
        // Adding data: stats-cli pie <name> rate "A" with 40
        const label = args[rateIndex + 1];
        const value = parseFloat(args[withIndex + 1]);

        if (!label || isNaN(value)) {
            console.error('Usage: stats-cli pie <name> rate "<label>" with <value>');
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

        // Validate
        try {
            const result = PieValidator.validateOrThrow(items);
            console.log(`Added data: ${label} = ${value}`);
            console.log(`Current total: ${result.total.toFixed(2)}`);

            // If total is valid, insert the data
            if (result.isValid) {
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

                    // Insert data
                    const chunks = items.map(item => ({
                        name: item.name,
                        value: String(item.value)
                    }));

                    await client.insertData(fieldName, chunks);
                    console.log(`Data inserted successfully for field '${fieldName}'`);

                    // Clear the stored data after successful insert
                    fieldData.delete(fieldName);
                } catch (error) {
                    console.error(`Error inserting data: ${error.message}`);
                    // Keep the data in memory for retry
                }
            }
        } catch (error) {
            console.error(`Validation error: ${error.message}`);
            console.log('Data kept in memory. Add more data or fix the values.');
        }
    } else {
        // Query mode: stats-cli pie <name>
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
}

module.exports = { handlePie };
