/**
 * Gauge Command Handler
 * Simple dashboard with single field + multiple records
 */

const { StatsAPIClient } = require('../api/client');

/**
 * Show gauge command help
 */
function showGaugeHelp() {
    console.log(`
GAUGE COMMAND - 仪表盘数据管理

USAGE:
  statistic-cli gauge <field> add <name> with <value>
  statistic-cli gauge <field> add <name> meta <sql>
  statistic-cli gauge <field> del <name>
  statistic-cli gauge <field> [--url]

SUBCOMMANDS:
  add <name> with <value>        添加指标数据（静态值）
  add <name> meta <sql>          添加指标数据（使用 SQL 查询）
  del <name>                     删除指标数据
  (no args)                      查询仪表盘数据

OPTIONS:
  --url                          显示 API URL

DESCRIPTION:
  简单的仪表盘：单个 field + 多个 records
  不需要 groups，直接用 field 作为仪表盘名称

  当使用 meta 参数时，系统会：
  1. 在 StatisticsMeta 表中创建/更新记录，存储 SQL 查询
  2. 设置 field 的 attrRuntime = 1，启用实时查询
  3. 查询时优先使用 meta SQL，静态值作为备用

EXAMPLES:
  statistic-cli gauge alarm add errors with 3
  statistic-cli gauge alarm add errors meta "SELECT count(*) as errors FROM alarm_table WHERE status='active'"
  statistic-cli gauge alarm add warnings with 5
  statistic-cli gauge alarm del warnings
  statistic-cli gauge alarm
  statistic-cli gauge alarm --url
`);
}

/**
 * Handle gauge command
 * @param {Array} args - Command arguments
 * @param {Object} options - Command options
 */
async function handleGauge(args, options) {
    const client = new StatsAPIClient({
        baseUrl: options.baseUrl,
        token: options.token,
        timeout: options.timeout,
        verbose: options.verbose
    });

    const fieldName = args[0];

    if (!fieldName) {
        // Show gauge help when no field name provided
        showGaugeHelp();
        return;
    }

    // Check if adding data (add <name> with <value> OR add <name> meta <sql>)
    const addIndex = args.indexOf('add');
    const withIndex = args.indexOf('with');
    const metaIndex = args.indexOf('meta');
    const delIndex = args.indexOf('del');

    // Handle meta mode: statistic-cli gauge alarm add errors meta "SELECT ..."
    if (addIndex !== -1 && metaIndex !== -1 && addIndex < metaIndex) {
        const recordName = args[addIndex + 1];
        // Collect all remaining args as the SQL query
        const sqlArgs = args.slice(metaIndex + 1);
        const sqlQuery = sqlArgs.join(' ');

        if (!recordName || !sqlQuery) {
            console.error('Usage: statistic-cli gauge <field> add <name> meta <sql>');
            process.exit(1);
        }

        try {
            // Ensure field exists
            const fieldId = await client.ensureFieldExists({
                field: fieldName,
                name: fieldName,
                groupName: fieldName,
                pattern: 'Gauge',
                chart: 'Total',
                attrRuntime: 0,
                attrInvisible: 0,
                attrSpan: 1,
                attrIndex: 0
            });

            // Set pattern to Gauge for gauge dashboard
            await client.updateFieldPattern(fieldId, 'Gauge');
            if (options.verbose) {
                console.error(`[DEBUG] Set pattern to 'Gauge' for field '${fieldName}'`);
            }

            // Create/update meta with SQL query
            // The SQL query should return a result with column name matching recordName
            // Example: SELECT count(*) as errors FROM table
            await client.ensureMetaExists(fieldName, sqlQuery, {
                title: fieldName,
                type: '数量',
                pattern: 'Gauge',
                chart: 'Total'
            });

            // Set attrRuntime to 1 to enable meta query
            await client.setFieldRuntime(fieldId, 1);
            if (options.verbose) {
                console.error(`[DEBUG] Set attrRuntime to 1 for field '${fieldName}'`);
            }

            // Also insert a placeholder record (value will be overridden by meta query)
            const chunks = [{
                name: recordName,
                value: '0'  // Placeholder value, meta query will provide actual value
            }];

            await client.insertData(fieldName, chunks);
            console.log(`✓ Added: ${recordName} (with meta SQL)`);
            if (options.verbose) {
                console.error(`[DEBUG] SQL: ${sqlQuery}`);
            }
        } catch (error) {
            console.error(`✗ Error setting up meta query: ${error.message}`);
            process.exit(1);
        }
    } else if (addIndex !== -1 && withIndex !== -1 && addIndex < withIndex) {
        // Adding data: statistic-cli gauge alarm add errors with 3
        const recordName = args[addIndex + 1];
        const value = args[withIndex + 1];

        if (!recordName || value === undefined) {
            console.error('Usage: statistic-cli gauge <field> add <name> with <value>');
            process.exit(1);
        }

        try {
            // Ensure field exists (group name = field name for gauge dashboard)
            const fieldId = await client.ensureFieldExists({
                field: fieldName,
                name: fieldName,
                groupName: fieldName,  // Use fieldName as groupName
                pattern: 'Count',  // Initial pattern, will be updated to Gauge
                chart: 'Total',
                attrRuntime: 0,
                attrInvisible: 0,
                attrSpan: 1,
                attrIndex: 0
            });

            // Set pattern to Gauge for gauge dashboard
            await client.updateFieldPattern(fieldId, 'Gauge');
            if (options.verbose) {
                console.error(`[DEBUG] Set pattern to 'Gauge' for field '${fieldName}'`);
            }

            // Insert data as a record
            const chunks = [{
                name: recordName,  // record name: errors, warnings, etc.
                value: String(value)
            }];

            await client.insertData(fieldName, chunks);
            console.log(`✓ Added: ${recordName} = ${value}`);
        } catch (error) {
            console.error(`✗ Error inserting data: ${error.message}`);
            process.exit(1);
        }
    } else if (delIndex !== -1) {
        // Deleting data: statistic-cli gauge alarm del warnings
        const recordName = args[delIndex + 1];

        if (!recordName) {
            console.error('Usage: statistic-cli gauge <field> del <name>');
            process.exit(1);
        }

        try {
            const result = await client.getStatisticByGroup(fieldName);

            if (result && result.data) {
                const fieldData = result.data.find(d => d.field === fieldName);

                if (!fieldData) {
                    console.error(`Field '${fieldName}' not found`);
                    process.exit(1);
                }

                // Find the record to delete
                const recordToDelete = fieldData.records?.find(r => r.recordName === recordName);

                if (!recordToDelete) {
                    console.error(`Record '${recordName}' not found in field '${fieldName}'`);
                    process.exit(1);
                }

                // Delete the record
                await client.deleteRecord(recordToDelete.id);
                console.log(`✓ Deleted: ${recordName}`);
            }
        } catch (error) {
            console.error(`✗ Error deleting data: ${error.message}`);
            process.exit(1);
        }
    } else {
        // Query mode: statistic-cli gauge alarm [--url]
        try {
            // When --url is provided, fetch the /statistic endpoint (pattern-based data)
            if (options.showUrl) {
                const apiUrl = `${client.baseUrl}/api/adm/stat/fields/${encodeURIComponent(fieldName)}/statistic`;
                console.error(`URL: ${apiUrl}`);

                // Make request to the /statistic endpoint
                const result = await client._request('GET', `/api/adm/stat/fields/${encodeURIComponent(fieldName)}/statistic`);

                // Output the result from the /statistic endpoint
                console.log(JSON.stringify(result.data || result, null, 2));
            } else {
                // Default behavior: use /fields endpoint and format as simple object
                const result = await client.getFieldStatistics(fieldName);

                // Backend returns: {code: 200, data: {field: "...", items: [...]}}
                // The items array contains the records
                if (result && result.data) {
                    const fieldData = result.data;

                    // Build result object from records
                    const output = {};
                    if (fieldData.items && fieldData.items.length > 0) {
                        fieldData.items.forEach(record => {
                            output[record.recordName] = parseInt(record.recordValue, 10) || 0;
                        });
                    }

                    // Output JSON
                    console.log(JSON.stringify(output, null, 2));
                }
            }
        } catch (error) {
            console.error(`✗ Error querying data: ${error.message}`);
            process.exit(1);
        }
    }
}

module.exports = { handleGauge };
