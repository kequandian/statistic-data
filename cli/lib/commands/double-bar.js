/**
 * Double-Bar Command Handler
 * Handles double-bar (grouped bar) chart related commands
 */

const { StatsAPIClient } = require('../api/client');

// Store data in memory for each field
const fieldData = new Map();

/**
 * Handle double-bar command
 * @param {Array} args - Command arguments
 * @param {Object} options - Command options
 */
async function handleDoubleBar(args, options) {
    const client = new StatsAPIClient({
        baseUrl: options.baseUrl,
        token: options.token,
        timeout: options.timeout,
        verbose: options.verbose
    });

    const fieldName = args[0];

    if (!fieldName) {
        console.error('Usage: stats-cli double-bar <name> series "<series>" "<category>" with <value> [--json]');
        console.error('   or: stats-cli double-bar <name> [--json]');
        process.exit(1);
    }

    // Check if adding data (series "<series>" "<category>" with <value>)
    const seriesIndex = args.indexOf('series');
    const withIndex = args.indexOf('with');

    if (seriesIndex !== -1 && withIndex !== -1 && seriesIndex < withIndex) {
        // Adding data: stats-cli double-bar <name> series "2024" "Q1" with 15000
        const seriesName = args[seriesIndex + 1];
        const categoryName = args[seriesIndex + 2];
        const value = parseFloat(args[withIndex + 1]);

        if (!seriesName || !categoryName || isNaN(value)) {
            console.error('Usage: stats-cli double-bar <name> series "<series>" "<category>" with <value>');
            process.exit(1);
        }

        // Initialize data map for this field if not exists
        if (!fieldData.has(fieldName)) {
            fieldData.set(fieldName, new Map());
        }

        const seriesMap = fieldData.get(fieldName);

        // Initialize series array if not exists
        if (!seriesMap.has(seriesName)) {
            seriesMap.set(seriesName, []);
        }

        // Add data to series
        const items = seriesMap.get(seriesName);
        items.push({ name: categoryName, value: value });
        seriesMap.set(seriesName, items);

        console.log(`Added data: ${seriesName} - ${categoryName} = ${value}`);

        // Check if we have exactly 2 series
        if (seriesMap.size === 2) {
            // Insert the data
            try {
                // Ensure field exists
                await client.ensureFieldExists({
                    field: fieldName,
                    name: fieldName,
                    groupName: 'default',
                    pattern: 'Tuple',
                    chart: 'BarGroup_2',
                    attrRuntime: 0,
                    attrInvisible: 0,
                    attrSpan: 1,
                    attrIndex: 0
                });

                // Convert series map to chunks
                const chunks = [];
                let seq = 0;
                for (const [seriesName, items] of seriesMap) {
                    for (const item of items) {
                        chunks.push({
                            name: item.name,
                            value: String(item.value),
                            tuple: seriesName,
                            seq: seq++
                        });
                    }
                }

                await client.insertData(fieldName, chunks);
                console.log(`Data inserted successfully for field '${fieldName}'`);

                // Clear the stored data after successful insert
                fieldData.delete(fieldName);
            } catch (error) {
                console.error(`Error inserting data: ${error.message}`);
                // Keep the data in memory for retry
            }
        } else {
            console.log(`Waiting for more data. Current series: ${seriesMap.size}/2`);
        }
    } else {
        // Query mode: stats-cli double-bar <name>
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
                    console.log(`\n=== Double-Bar Chart: ${fieldName} ===`);

                    // Group by tuple (series)
                    const seriesData = new Map();
                    fieldData.records.forEach(record => {
                        const tuple = record.recordTuple || 'default';
                        if (!seriesData.has(tuple)) {
                            seriesData.set(tuple, []);
                        }
                        seriesData.get(tuple).push(record);
                    });

                    // Print each series
                    for (const [seriesName, records] of seriesData) {
                        console.log(`\n  Series: ${seriesName}`);
                        const maxValue = Math.max(...records.map(r => parseFloat(r.recordValue || 0)));
                        records.forEach(record => {
                            const value = parseFloat(record.recordValue || 0);
                            const barLength = maxValue > 0 ? Math.round((value / maxValue) * 20) : 0;
                            const bar = '█'.repeat(barLength);
                            console.log(`    ${record.recordName.padEnd(12)} │${bar} ${value}`);
                        });
                    }
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

module.exports = { handleDoubleBar };
