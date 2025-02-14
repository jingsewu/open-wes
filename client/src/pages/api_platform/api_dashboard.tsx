import schema2component from '@/utils/schema2component';

const schema = {
    type: 'page',
    title: 'API Analytics Dashboard',
    body: [
        {
            type: 'grid',
            columns: [
                {
                    type: 'card',
                    className: 'stats-card',
                    body: {
                        type: 'tpl',
                        tpl: '<div class="stat-card"><div class="stat-title">Total Requests</div><div class="stat-value">23.5K</div><div class="stat-desc">↗︎ 12% from last month</div></div>',
                    },
                    md: 3,
                },
                {
                    type: 'card',
                    className: 'stats-card',
                    body: {
                        type: 'tpl',
                        tpl: '<div class="stat-card"><div class="stat-title">Error Rate</div><div class="stat-value">1.2%</div><div class="stat-desc">↘︎ 0.3% from last month</div></div>',
                    },
                    md: 3,
                },
                {
                    type: 'card',
                    className: 'stats-card',
                    body: {
                        type: 'tpl',
                        tpl: '<div class="stat-card"><div class="stat-title">Avg Response Time</div><div class="stat-value">156ms</div><div class="stat-desc">↗︎ 50ms from last month</div></div>',
                    },
                    md: 3,
                },
                {
                    type: 'card',
                    className: 'stats-card',
                    body: {
                        type: 'tpl',
                        tpl: '<div class="stat-card"><div class="stat-title">Active Users</div><div class="stat-value">1,234</div><div class="stat-desc">↗︎ 8% from last month</div></div>',
                    },
                    md: 3,
                },
            ],
        },
        {
            type: 'grid',
            columns: [
                {
                    type: 'card',
                    title: 'Response Time & Requests',
                    body: {
                        type: 'chart',
                        height: 300,
                        config: {
                            xAxis: {
                                type: 'category',
                                data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
                            },
                            yAxis: [
                                {
                                    type: 'value',
                                    name: 'Response Time (ms)',
                                },
                                {
                                    type: 'value',
                                    name: 'Requests',
                                    position: 'right',
                                },
                            ],
                            series: [
                                {
                                    name: 'Response Time',
                                    type: 'line',
                                    data: [120, 150, 180, 200, 160, 140],
                                    yAxisIndex: 0,
                                },
                                {
                                    name: 'Requests',
                                    type: 'line',
                                    data: [1500, 1200, 2500, 3000, 2800, 2000],
                                    yAxisIndex: 1,
                                },
                            ],
                            tooltip: {
                                trigger: 'axis',
                            },
                        },
                    },
                    md: 6,
                },
                {
                    type: 'card',
                    title: 'Top Endpoints',
                    body: {
                        type: 'chart',
                        height: 300,
                        config: {
                            xAxis: {
                                type: 'category',
                                data: ['/users', '/products', '/orders', '/auth', '/search'],
                            },
                            yAxis: {
                                type: 'value',
                            },
                            series: [
                                {
                                    type: 'bar',
                                    data: [45000, 38000, 32000, 28000, 25000],
                                },
                            ],
                            tooltip: {
                                trigger: 'axis',
                            },
                        },
                    },
                    md: 6,
                },
            ],
        },
        {
            type: 'grid',
            columns: [
                {
                    type: 'card',
                    title: 'Error Distribution',
                    body: {
                        type: 'chart',
                        height: 300,
                        config: {
                            series: [
                                {
                                    type: 'pie',
                                    radius: '60%',
                                    data: [
                                        {value: 60, name: '4xx Errors'},
                                        {value: 30, name: '5xx Errors'},
                                        {value: 10, name: 'Network Errors'},
                                    ],
                                },
                            ],
                        },
                    },
                    md: 6,
                },
                {
                    type: 'card',
                    title: 'Recent API Calls',
                    body: {
                        type: 'table',
                        source: '$api_calls',
                        columns: [
                            {
                                name: 'endpoint',
                                label: 'Endpoint',
                            },
                            {
                                name: 'method',
                                label: 'Method',
                            },
                            {
                                name: 'status',
                                label: 'Status',
                            },
                            {
                                name: 'response_time',
                                label: 'Response Time',
                            },
                            {
                                name: 'timestamp',
                                label: 'Timestamp',
                            },
                        ],
                    },
                    md: 6,
                },
            ],
        },
    ],
    style: {
        '.stats-card': {
            marginBottom: '1rem',
        },
        '.stat-card': {
            padding: '1rem',
            textAlign: 'center',
        },
        '.stat-title': {
            fontSize: '0.875rem',
            color: '#666',
        },
        '.stat-value': {
            fontSize: '1.5rem',
            fontWeight: 'bold',
            margin: '0.5rem 0',
        },
        '.stat-desc': {
            fontSize: '0.75rem',
            color: '#888',
        },
    },
};

export default schema2component(schema);
