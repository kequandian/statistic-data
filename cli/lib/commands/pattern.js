/**
 * Pattern Command Handler
 * Changes the pattern of a statistics field
 */

const { StatsAPIClient } = require('../api/client');

// Valid patterns from backend StatisticData.java
const VALID_PATTERNS = [
    'Count',
    'CountTimeline',
    'CountCluster',
    'CountTimelineCluster',
    'Rate',
    'RateTimeline',
    'RateCluster',
    'RateTimelineCluster',
    'Tuple',
    'TupleTimeline',
    'TupleCluster',
    'TupleTimelineCluster',
    'Gauge'
];

/**
 * Show pattern command help
 */
function showPatternHelp() {
    console.log(`
PATTERN COMMAND - 变更统计域的数据模式

USAGE:
  statistic-cli pattern <field> set <pattern>
  statistic-cli pattern <field> show
  statistic-cli pattern list

SUBCOMMANDS:
  set <pattern>               设置统计域的pattern
  show                        显示当前pattern
  list                        列出所有可用的pattern

VALID PATTERNS:
  Count                       单一数值计数
  CountTimeline               带时间线的计数
  CountCluster                带分类的计数
  CountTimelineCluster        带时间线和分类的计数
  Rate                        比率数据
  RateTimeline                带时间线的比率
  RateCluster                带分类的比率
  RateTimelineCluster        带时间线和分类的比率
  Tuple                       元组数据
  TupleTimeline               带时间线的元组
  TupleCluster               带分类的元组
  TupleTimelineCluster       带时间线和分类的元组
  Gauge                       仪表盘数据

EXAMPLES:
  statistic-cli pattern device_alarm set Gauge
  statistic-cli pattern device_alarm show
  statistic-cli pattern list
`);
}

/**
 * Validate pattern value
 */
function validatePattern(pattern) {
    if (!pattern) {
        return { valid: false, error: 'Pattern cannot be empty' };
    }

    if (!VALID_PATTERNS.includes(pattern)) {
        return {
            valid: false,
            error: `Invalid pattern: '${pattern}'\n\nValid patterns are:\n  ${VALID_PATTERNS.join('\n  ')}`
        };
    }

    return { valid: true };
}

/**
 * Handle pattern command
 * @param {Array} args - Command arguments
 * @param {Object} options - Command options
 */
async function handlePattern(args, options) {
    const client = new StatsAPIClient({
        baseUrl: options.baseUrl,
        token: options.token,
        timeout: options.timeout,
        verbose: options.verbose
    });

    const fieldName = args[0];

    if (!fieldName) {
        // Show pattern help when no field name provided
        showPatternHelp();
        return;
    }

    const subcommand = args[1];

    if (!subcommand) {
        showPatternHelp();
        return;
    }

    switch (subcommand) {
        case 'set': {
            const newPattern = args[2];

            if (!newPattern) {
                console.error('Usage: statistic-cli pattern <field> set <pattern>');
                console.error('Use "statistic-cli pattern list" to see all valid patterns');
                process.exit(1);
            }

            // Validate pattern
            const validation = validatePattern(newPattern);
            if (!validation.valid) {
                console.error(`Error: ${validation.error}`);
                process.exit(1);
            }

            try {
                // Get field by name to find its ID
                const field = await client.getFieldByName(fieldName);

                if (!field) {
                    console.error(`Error: Field '${fieldName}' not found`);
                    process.exit(1);
                }

                // Update pattern
                await client.updateFieldPattern(field.id, newPattern);
                console.log(`✓ Pattern updated for field '${fieldName}'`);
                console.log(`  New pattern: ${newPattern}`);
            } catch (error) {
                console.error(`✗ Error updating pattern: ${error.message}`);
                process.exit(1);
            }
            break;
        }

        case 'show': {
            try {
                const field = await client.getFieldByName(fieldName);

                if (!field) {
                    console.error(`Error: Field '${fieldName}' not found`);
                    process.exit(1);
                }

                console.log(`\n=== Field: ${fieldName} ===`);
                console.log(`Pattern: ${field.pattern || 'N/A'}`);
                console.log(`Chart: ${field.chart || 'N/A'}`);
                console.log(`Group: ${field.groupName || 'N/A'}`);
            } catch (error) {
                console.error(`✗ Error getting field info: ${error.message}`);
                process.exit(1);
            }
            break;
        }

        default:
            console.error(`Unknown subcommand: ${subcommand}`);
            showPatternHelp();
            process.exit(1);
    }
}

/**
 * Handle pattern list command
 */
async function handlePatternList() {
    console.log('\n=== Valid Patterns ===\n');
    console.log('Count patterns:');
    console.log('  Count              - Single value count');
    console.log('  CountTimeline      - Count with timeline');
    console.log('  CountCluster       - Count with cluster grouping');
    console.log('  CountTimelineCluster - Count with timeline and cluster');
    console.log('');
    console.log('Rate patterns:');
    console.log('  Rate               - Rate data');
    console.log('  RateTimeline       - Rate with timeline');
    console.log('  RateCluster        - Rate with cluster grouping');
    console.log('  RateTimelineCluster - Rate with timeline and cluster');
    console.log('');
    console.log('Tuple patterns:');
    console.log('  Tuple              - Tuple data');
    console.log('  TupleTimeline      - Tuple with timeline');
    console.log('  TupleCluster       - Tuple with cluster grouping');
    console.log('  TupleTimelineCluster - Tuple with timeline and cluster');
    console.log('');
    console.log('Gauge patterns:');
    console.log('  Gauge              - Dashboard gauge (key-value pairs)');
    console.log('');
}

module.exports = { handlePattern, handlePatternList, VALID_PATTERNS };
