#!/usr/bin/env node
/**
 * Mock API Server for E2E Testing
 * Simulates the Java statistics API for CLI testing
 */

const http = require('http');

// In-memory data store
const data = {
    groups: [
        { id: 1, name: 'default', title: '默认分组', pid: null }
    ],
    fields: [],
    records: []
};

// Request counter
let requestId = 0;

// CORS headers
const corsHeaders = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization'
};

// Parse JSON body
function parseBody(req) {
    return new Promise((resolve, reject) => {
        let body = '';
        req.on('data', chunk => body += chunk);
        req.on('end', () => {
            try {
                resolve(body ? JSON.parse(body) : {});
            } catch (e) {
                reject(e);
            }
        });
    });
}

// Routes
const routes = {
    // GET /api/cfg/stat/groups - Get all groups
    'GET:/api/cfg/stat/groups': (req, res) => {
        res.writeHead(200, { 'Content-Type': 'application/json', ...corsHeaders });
        res.end(JSON.stringify(data.groups));
    },

    // POST /api/cfg/stat/groups - Create group
    'POST:/api/cfg/stat/groups': async (req, res) => {
        const body = await parseBody(req);
        const newGroup = {
            id: data.groups.length + 1,
            name: body.name,
            title: body.title || body.name,
            pid: body.pid || null
        };

        // Check if group already exists
        if (data.groups.find(g => g.name === newGroup.name)) {
            res.writeHead(200, { 'Content-Type': 'application/json', ...corsHeaders });
            res.end(JSON.stringify(data.groups.find(g => g.name === newGroup.name)));
            return;
        }

        data.groups.push(newGroup);
        res.writeHead(201, { 'Content-Type': 'application/json', ...corsHeaders });
        res.end(JSON.stringify(newGroup));
    },

    // GET /api/cfg/stat/fields - Get all fields
    'GET:/api/cfg/stat/fields': (req, res) => {
        res.writeHead(200, { 'Content-Type': 'application/json', ...corsHeaders });
        res.end(JSON.stringify(data.fields));
    },

    // POST /api/cfg/stat/fields - Create field
    'POST:/api/cfg/stat/fields': async (req, res) => {
        const body = await parseBody(req);
        const newField = {
            id: data.fields.length + 1,
            field: body.field,
            name: body.name || body.field,
            groupName: body.groupName || 'default',
            pattern: body.pattern || 'Rate',
            chart: body.chart || 'Pie',
            attrRuntime: body.attrRuntime || 0,
            attrInvisible: body.attrInvisible || 0,
            attrSpan: body.attrSpan || 1,
            attrIndex: body.attrIndex || 0
        };

        // Check if field already exists
        if (data.fields.find(f => f.field === newField.field)) {
            res.writeHead(200, { 'Content-Type': 'application/json', ...corsHeaders });
            res.end(JSON.stringify(data.fields.find(f => f.field === newField.field)));
            return;
        }

        data.fields.push(newField);
        res.writeHead(201, { 'Content-Type': 'application/json', ...corsHeaders });
        res.end(JSON.stringify(newField));
    },

    // GET /api/adm/stat/groups/:group - Get statistics by group
    'GET:/api/adm/stat/groups/': (req, res, url) => {
        const groupName = url.pathname.split('/').pop();
        const groupFields = data.fields.filter(f => f.groupName === groupName);

        const result = {
            group: groupName,
            data: groupFields.map(f => ({
                field: f.field,
                name: f.name,
                pattern: f.pattern,
                chart: f.chart,
                records: data.records.filter(r => r.field === f.field)
            }))
        };

        res.writeHead(200, { 'Content-Type': 'application/json', ...corsHeaders });
        res.end(JSON.stringify(result));
    },

    // POST /api/adm/stat/notify - Insert statistics records
    'POST:/api/adm/stat/notify': async (req, res) => {
        const body = await parseBody(req);
        const fieldName = body.name;
        const chunks = body.chunks || [];

        // Insert or update records
        for (const chunk of chunks) {
            const existingIndex = data.records.findIndex(
                r => r.field === fieldName &&
                r.recordName === chunk.name &&
                r.recordTuple === chunk.tuple &&
                r.recordCluster === chunk.cluster &&
                r.timeline === chunk.timeline
            );

            if (existingIndex >= 0) {
                data.records[existingIndex].recordValue = chunk.value;
                data.records[existingIndex].createTime = new Date().toISOString();
            } else {
                data.records.push({
                    id: data.records.length + 1,
                    field: fieldName,
                    recordName: chunk.name,
                    recordValue: chunk.value,
                    recordTuple: chunk.tuple || null,
                    recordCluster: chunk.cluster || null,
                    timeline: chunk.timeline || null,
                    createTime: new Date().toISOString()
                });
            }
        }

        res.writeHead(200, { 'Content-Type': 'application/json', ...corsHeaders });
        res.end(JSON.stringify({ success: true }));
    },

    // OPTIONS - CORS preflight
    'OPTIONS': (req, res) => {
        res.writeHead(204, corsHeaders);
        res.end();
    }
};

// Create server
const server = http.createServer(async (req, res) => {
    const id = ++requestId;
    const url = new URL(req.url, `http://${req.headers.host}`);

    console.log(`[${id}] ${req.method} ${req.url}`);

    // Handle CORS preflight
    if (req.method === 'OPTIONS') {
        routes['OPTIONS'](req, res);
        return;
    }

    // Find matching route
    const routeKey = `${req.method}:${url.pathname}`;
    let matched = false;

    // Exact match
    if (routes[routeKey]) {
        routes[routeKey](req, res, url);
        matched = true;
    } else {
        // Prefix match for paths with parameters
        for (const key in routes) {
            if (key.endsWith('/') && routeKey.startsWith(key)) {
                routes[key](req, res, url);
                matched = true;
                break;
            }
        }
    }

    if (!matched) {
        console.log(`[${id}] 404 Not Found`);
        res.writeHead(404, { 'Content-Type': 'application/json', ...corsHeaders });
        res.end(JSON.stringify({ error: 'Not Found' }));
    }
});

// Start server
const PORT = process.env.PORT || 8086;
server.listen(PORT, () => {
    console.log(`Mock API Server running on http://localhost:${PORT}`);
    console.log('Press Ctrl+C to stop');
});

// Cleanup on exit
process.on('SIGINT', () => {
    console.log('\nShutting down mock server...');
    server.close(() => {
        console.log('Mock server stopped');
        process.exit(0);
    });
});
