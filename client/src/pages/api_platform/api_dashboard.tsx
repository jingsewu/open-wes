import schema2component from '@/utils/schema2component';

const schema = {
    type: 'page',
    title: 'API Analytics Dashboard',
    data: {
        apiUrl: '/wms/api/analytics/dashboard'
    },
    initApi: '$apiUrl',
    body: [
        {
            type: 'grid',
            columns: [
                {
                    type: 'card',
                    className: 'stats-card',
                    body: {
                        type: 'tpl',
                        tpl: '<div class="stat-card"><div class="stat-title">Total Requests</div><div class="stat-value">${stats.totalRequests | number}</div>',
                    },
                    md: 4,
                },
                {
                    type: 'card',
                    className: 'stats-card',
                    body: {
                        type: 'tpl',
                        tpl: '<div class="stat-card"><div class="stat-title">Error Rate</div><div class="stat-value">${stats.errorRate | number: 2}%</div></div>',
                    },
                    md: 4,
                },
                {
                    type: 'card',
                    className: 'stats-card',
                    body: {
                        type: 'tpl',
                        tpl: '<div class="stat-card"><div class="stat-title">Avg Response Time</div><div class="stat-value">${stats.avgResponseTime | number: 0}ms</div></div>',
                    },
                    md: 4,
                }
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
                        data: {
                            timeSlots: "${timeSeriesData.map(item => item.timeSlot)}",
                            responseTimes: "${timeSeriesData.map(item => item.avgResponseTime)}",
                            requestCounts: "${timeSeriesData.map(item => item.requestCount)}"
                        },
                        config: {
                            xAxis: {
                                type: 'category',
                                data: '${timeSlots}'
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
                                    data: '${responseTimes}',
                                    yAxisIndex: 0,
                                },
                                {
                                    name: 'Requests',
                                    type: 'line',
                                    data: '${requestCounts}',
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
                        data: {
                            endpoints: "${topEndpoints.map(item => item.apiCode)}",
                            counts: "${topEndpoints.map(item => item.requestCount)}"
                        },
                        config: {
                            xAxis: {
                                type: 'category',
                                data: '${endpoints}'
                            },
                            yAxis: {
                                type: 'value',
                            },
                            series: [
                                {
                                    type: 'bar',
                                    data: '${counts}'
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
                        data: {
                            errorData: "${errorDistribution.map(item => ({value: item.count, name: item.errorType}))}"
                        },
                        config: {
                            series: [
                                {
                                    type: 'pie',
                                    radius: '60%',
                                    data: '${errorData}'
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
                        source: '${recentApiCalls}',
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
                                name: 'responseTime',
                                label: 'Response Time',
                            },
                            {
                                name: 'timestamp',
                                label: 'Timestamp',
                                type: 'datetime'
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
