/**
 * Extended API Client for Statistics
 * Extends the base API client with new methods for data insertion
 */

const http = require('http');
const https = require('https');
const path = require('path');
const dotenv = require('dotenv');

// Load .env file from multiple possible locations
const envPaths = [
    path.join(__dirname, '..', '..', '.env'),
    path.join(process.cwd(), '.env'),
    path.join(require('os').homedir(), '.env')
];
for (const envPath of envPaths) {
    if (require('fs').existsSync(envPath)) {
        dotenv.config({ path: envPath });
        break;
    }
}

const DEFAULT_TIMEOUT = 60000;

function httpRequest(options) {
    return new Promise((resolve, reject) => {
        const url = new URL(options.url);
        const isHttps = url.protocol === 'https:';
        const client = isHttps ? https : http;

        const reqOptions = {
            hostname: url.hostname,
            port: url.port || (isHttps ? 443 : 80),
            path: url.pathname + url.search,
            method: options.method || 'GET',
            headers: options.headers || {},
            timeout: options.timeout || DEFAULT_TIMEOUT
        };

        if (options.body) {
            reqOptions.headers['Content-Length'] = Buffer.byteLength(options.body);
        }

        if (options.verbose) {
            console.error(`[DEBUG] ${reqOptions.method} ${options.url}`);
            if (options.params) {
                console.error(`[DEBUG] Params: ${JSON.stringify(options.params)}`);
            }
        }

        const req = client.request(reqOptions, (res) => {
            let data = '';

            res.on('data', chunk => {
                data += chunk;
            });

            res.on('end', () => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    const contentType = res.headers['content-type'] || '';
                    if (contentType.includes('application/json')) {
                        try {
                            resolve(JSON.parse(data));
                        } catch (e) {
                            resolve(data);
                        }
                    } else {
                        resolve({ data: data, headers: res.headers });
                    }
                } else {
                    const error = new Error(`HTTP ${res.statusCode}`);
                    error.statusCode = res.statusCode;
                    error.response = data;
                    try {
                        error.responseJson = JSON.parse(data);
                    } catch (e) {
                        // Ignore
                    }
                    reject(error);
                }
            });
        });

        req.on('timeout', () => {
            req.destroy();
            reject(new Error(`Request timeout (${reqOptions.timeout}ms)`));
        });

        req.on('error', (err) => {
            if (err.code === 'ECONNREFUSED') {
                reject(new Error(`Cannot connect to server: ${options.url}`));
            } else {
                reject(err);
            }
        });

        if (options.body) {
            req.write(options.body);
        }

        req.end();
    });
}

class StatsAPIClient {
    constructor(options = {}) {
        // Use .env environment variables for configuration
        this.baseUrl = (options.baseUrl ||
                        process.env.STATISTIC_BASE_URL ||
                        process.env.STATISTIC_CLI_BASE_URL);

        if (!this.baseUrl) {
            throw new Error('STATISTIC_BASE_URL or STATISTIC_CLI_BASE_URL is required. Please set it in .env file.');
        }

        this.baseUrl = this.baseUrl.replace(/\/$/, '');
        this.token = options.token || process.env.STATISTIC_TOKEN;
        this.timeout = options.timeout || DEFAULT_TIMEOUT;
        this.verbose = options.verbose || false;

        this.headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        if (this.token) {
            this.headers['Authorization'] = `Bearer ${this.token}`;
        }
    }

    async _request(method, endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const params = new URLSearchParams(options.params || {});
        const queryString = params.toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;

        try {
            return await httpRequest({
                url: fullUrl,
                method: method,
                headers: this.headers,
                timeout: this.timeout,
                verbose: this.verbose,
                body: options.body,
                params: options.params
            });
        } catch (error) {
            if (error.message.includes('timeout')) {
                console.error(`Error: Request timeout (${this.timeout / 1000}s)`);
            } else if (error.message.includes('Cannot connect')) {
                console.error(`Error: ${error.message}`);
                console.error(`URL: ${url}`);
            } else if (error.statusCode) {
                console.error(`Error: HTTP ${error.statusCode}`);
                if (error.responseJson) {
                    console.error(`Details: ${JSON.stringify(error.responseJson)}`);
                }
            } else {
                console.error(`Request failed: ${error.message}`);
            }
            throw error;
        }
    }

    // ==================== Configuration API ====================
    async getGroups() {
        return this._request('GET', '/api/cfg/stat/groups');
    }

    async createGroup(groupData) {
        return this._request('POST', '/api/cfg/stat/groups', {
            body: JSON.stringify(groupData)
        });
    }

    async getFields(params = {}) {
        // Set default pagination params
        const queryParams = {
            pageNum: 1,
            pageSize: 1000,  // Get all fields
            ...params
        };
        return this._request('GET', '/api/cfg/stat/fields', { params: queryParams });
    }

    async createField(fieldData) {
        return this._request('POST', '/api/cfg/stat/fields', {
            body: JSON.stringify(fieldData)
        });
    }

    // ==================== Statistics Query API ====================
    async getStatisticByGroup(group, identifier = null) {
        const params = {};
        if (identifier) {
            params.identifier = identifier;
        }
        return this._request('GET', `/api/adm/stat/groups/${encodeURIComponent(group)}`, { params });
    }

    async getFieldStatistics(field, identifier = null) {
        const params = {};
        if (identifier) {
            params.identifier = identifier;
        }
        return this._request('GET', `/api/adm/stat/fields/${encodeURIComponent(field)}`, { params });
    }

    // ==================== Statistics Insert API (NEW) ====================
    async insertStatisticRecord(data) {
        return this._request('POST', '/api/adm/stat/notify', {
            body: JSON.stringify({
                name: data.name,
                recordTime: data.recordTime || new Date().toISOString(),
                chunks: data.chunks
            })
        });
    }

    /**
     * Delete a statistics record by ID
     * @param {number} recordId - Record ID to delete
     */
    async deleteRecord(recordId) {
        return this._request('DELETE', `/api/adm/stat/records/${recordId}`);
    }

    // ==================== Helper Methods ====================

    /**
     * Ensures a group exists, creates it if not
     * @param {string} groupName - Name of the group
     * @param {string} title - Title of the group
     * @returns {Promise<number>} Group ID
     */
    async ensureGroupExists(groupName = 'default', title = '默认分组') {
        try {
            const result = await this.getGroups();
            // Backend returns: {code: 200, data: [...]}
            const groups = result?.data || result || [];
            const existingGroup = groups.find(g => g.name === groupName);

            if (existingGroup) {
                if (this.verbose) {
                    console.error(`[DEBUG] Group '${groupName}' already exists (ID: ${existingGroup.id})`);
                }
                return existingGroup.id;
            }

            // Create new group
            const createResult = await this.createGroup({
                name: groupName,
                title: title,
                pid: null
            });

            if (this.verbose) {
                console.error(`[DEBUG] Created group '${groupName}' (ID: ${createResult?.data?.id || createResult?.id})`);
            }
            return createResult?.data?.id || createResult?.id;
        } catch (error) {
            console.error(`Error ensuring group exists: ${error.message}`);
            throw error;
        }
    }

    /**
     * Ensures a field exists, creates it if not
     * @param {Object} fieldData - Field configuration
     * @returns {Promise<number>} Field ID
     */
    async ensureFieldExists(fieldData) {
        try {
            // Use groupName parameter to filter fields by group name
            const result = await this.getFields({ groupId: fieldData.groupName });
            // Backend returns: {code: 200, data: {records: [...]}}
            const fields = result?.data?.records || result?.records || result?.data || result || [];

            const existingField = fields.find(f => f.field === fieldData.field);

            if (existingField) {
                if (this.verbose) {
                    console.error(`[DEBUG] Field '${fieldData.field}' already exists (ID: ${existingField.id})`);
                }
                return existingField.id;
            }

            // Ensure group exists first and get the group ID
            const groupId = await this.ensureGroupExists(fieldData.groupName, fieldData.groupTitle || '默认分组');

            // Prepare field data for creation - ensure both groupId and groupName are set
            const fieldCreateData = {
                ...fieldData,
                groupId: groupId,
                groupName: fieldData.groupName  // Ensure groupName is explicitly set
            };

            // Create new field
            const createResult = await this.createField(fieldCreateData);

            if (this.verbose) {
                console.error(`[DEBUG] Created field '${fieldData.field}' (ID: ${createResult.data || createResult})`);
            }
            return createResult.data || createResult;
        } catch (error) {
            console.error(`Error ensuring field exists: ${error.message}`);
            throw error;
        }
    }

    /**
     * Inserts data for a statistic field
     * @param {string} fieldName - Name of the field
     * @param {Array} chunks - Array of data chunks
     * @param {string} recordTime - Optional record time
     */
    async insertData(fieldName, chunks, recordTime = null) {
        try {
            const result = await this.insertStatisticRecord({
                name: fieldName,
                recordTime: recordTime,
                chunks: chunks
            });

            if (this.verbose) {
                console.error(`[DEBUG] Inserted ${chunks.length} chunk(s) for field '${fieldName}'`);
            }
            return result;
        } catch (error) {
            console.error(`Error inserting data: ${error.message}`);
            throw error;
        }
    }
}

module.exports = { StatsAPIClient };
