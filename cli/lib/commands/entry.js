/**
 * Entry Command Handler
 * Manages multiple entries under a group
 */

const { StatsAPIClient } = require('../api/client');

/**
 * Handle entry command
 * @param {Array} args - Command arguments
 * @param {Object} options - Command options
 */
async function handleEntry(args, options) {
    const client = new StatsAPIClient({
        baseUrl: options.baseUrl,
        token: options.token,
        timeout: options.timeout,
        verbose: options.verbose
    });

    const groupName = args[0];

    if (!groupName) {
        console.error('Usage: stats-cli entry <group> add <entry-name> with <value>');
        console.error('   or: stats-cli entry <group> [--url]');
        process.exit(1);
    }

    // Check if adding data (add <entry-name> with <value>)
    const addIndex = args.indexOf('add');
    const withIndex = args.indexOf('with');

    if (addIndex !== -1 && withIndex !== -1 && addIndex < withIndex) {
        // Adding data: stats-cli entry alarm add errors with 3
        const entryName = args[addIndex + 1];
        const value = args[withIndex + 1];

        if (!entryName || value === undefined) {
            console.error('Usage: stats-cli entry <group> add <entry-name> with <value>');
            process.exit(1);
        }

        // Field name is group_entry-name
        const fieldName = `${groupName}_${entryName}`;

        try {
            // Ensure field exists
            await client.ensureFieldExists({
                field: fieldName,
                name: fieldName,
                groupName: groupName,
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
            console.log(`Entry data inserted successfully for field '${fieldName}'`);
            console.log(`Value: ${value}`);
        } catch (error) {
            console.error(`Error inserting data: ${error.message}`);
            process.exit(1);
        }
    } else {
        // Query mode: stats-cli entry alarm [--url]
        try {
            const result = await client.getStatisticByGroup(groupName);

            // Build result object from all fields in the group
            const output = {};

            if (result && result.data) {
                result.data.forEach(fieldData => {
                    if (fieldData.records) {
                        // Find the 'total' record
                        const totalRecord = fieldData.records.find(r => r.recordName === 'total');
                        if (totalRecord) {
                            output[fieldData.field] = parseInt(totalRecord.recordValue, 10) || 0;
                        }
                    }
                });
            }

            // Show URL if --url option is provided
            if (options.showUrl) {
                console.log(`URL: ${client.baseUrl}/api/adm/stat/groups/${encodeURIComponent(groupName)}`);
            }

            // Always output JSON (default behavior)
            console.log(JSON.stringify(output));
        } catch (error) {
            console.error(`Error querying data: ${error.message}`);
            process.exit(1);
        }
    }
}

module.exports = { handleEntry };
