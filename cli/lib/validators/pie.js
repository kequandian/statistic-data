/**
 * Pie Chart Validator
 * Validates pie chart data to ensure sum is between 80-120
 */

class PieValidator {
    /**
     * Validate pie chart data
     * @param {Array} items - Array of items with name and value
     * @returns {Object} Validation result with isValid and error message
     */
    static validate(items) {
        if (!Array.isArray(items) || items.length === 0) {
            return {
                isValid: false,
                error: 'Pie chart must have at least one data item',
                total: 0
            };
        }

        // Calculate total
        const total = items.reduce((sum, item) => {
            const value = parseFloat(item.value);
            if (isNaN(value)) {
                throw new Error(`Invalid value for item '${item.name}': ${item.value}`);
            }
            return sum + value;
        }, 0);

        // Validate total range
        if (total < 80 || total > 120) {
            return {
                isValid: false,
                error: `Pie chart total must be between 80-120, got ${total.toFixed(2)}`,
                total: total
            };
        }

        return {
            isValid: true,
            total: total
        };
    }

    /**
     * Validate and throw error if invalid
     * @param {Array} items - Array of items with name and value
     * @throws {Error} If validation fails
     */
    static validateOrThrow(items) {
        const result = this.validate(items);
        if (!result.isValid) {
            throw new Error(result.error);
        }
        return result;
    }
}

module.exports = PieValidator;
