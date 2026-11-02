import schema2component from '@/utils/schema2component';

const schema = {
    type: 'page',
    title: 'interfacePlatform.dashboard.pageTitle',
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
                        tpl: '<div class="stat-card"><div class="stat-title">interfacePlatform.dashboard.totalRequests</div><div class="stat-value">${stats.totalRequests | number}</div></div>',
                    },
                    md: 4,
                },
                {
                    type: 'card',
                    className: 'stats-card',
                    body: {
                        type: 'tpl',
                        tpl: '<div class="stat-card"><div class="stat-title">interfacePlatform.dashboard.errorRate</div><div class="stat-value">${stats.errorRate | number: 2}%</div></div>',
                    },
                    md: 4,
                },
                {
                    type: 'card',
                    className: 'stats-card',
                    body: {
                        type: 'tpl',
                        tpl: '<div class="stat-card"><div class="stat-title">interfacePlatform.dashboard.avgResponseTime</div><div class="stat-value">${stats.avgResponseTime | number: 0}ms</div></div>',
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
                    header: {
                        title: 'interfacePlatform.dashboard.responseTimeRequests'
                    },
                    body: {
                        type: 'chart',
                        height: 300,
                        api: {
                            method: 'get',
                            url: '$apiUrl',
                            adaptor: function (payload: { timeSeriesData: any[]; }) {
                                return {
                                    data: {
                                        timeSlots: payload.timeSeriesData.map(item => item.timeSlot),
                                        responseTimes: payload.timeSeriesData.map(item => item.avgResponseTime),
                                        requestCounts: payload.timeSeriesData.map(item => item.requestCount)
                                    }
                                };
                            }
                        },
                        config: {
                            xAxis: {
                                type: 'category',
                                data: '${timeSlots}'
                            },
                            // ECharts names are global identifiers: AMIS replaceText
                            // skips "name" keys, so chart labels stay in one language.
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
                    header: {
                        title: 'interfacePlatform.dashboard.topEndpoints',
                    },
                    body: {
                        type: 'chart',
                        height: 300,
                        api: {
                            method: 'get',
                            url: '$apiUrl',
                            adaptor: function (payload: any) {
                                const endpoints = payload.topEndpoints || [];
                                return {
                                    data: {
                                        categories: endpoints.map((item: { apiCode: any; }) => item.apiCode),
                                        requestCounts: endpoints.map((item: {
                                            requestCount: any;
                                        }) => item.requestCount)
                                    }
                                };
                            }
                        },
                        config: {
                            tooltip: {
                                trigger: 'axis',
                                axisPointer: {
                                    type: 'shadow'
                                }
                            },
                            legend: {
                                data: ['Requests', 'Errors']
                            },
                            xAxis: {
                                type: 'category',
                                data: '${categories}',
                                axisLabel: {
                                    rotate: 45
                                }
                            },
                            yAxis: {
                                type: 'value'
                            },
                            series: [
                                {
                                    name: 'Requests',
                                    type: 'bar',
                                    data: '${requestCounts}'
                                }
                            ]
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
                    header: {
                        title: 'interfacePlatform.dashboard.errorDistribution'
                    },
                    body: {
                        type: 'chart',
                        height: 300,
                        api: {
                            method: 'get',
                            url: '$apiUrl',
                            adaptor: function (payload: { errorDistribution: any[]; }) {
                                const errorData = payload.errorDistribution || [];
                                return {
                                    data: {
                                        errorData: errorData.map(item => ({
                                            value: item.count,
                                            name: item.errorType
                                        }))
                                    }
                                };
                            }
                        },
                        config: {
                            tooltip: {
                                trigger: 'item',
                                formatter: '{a} <br/>{b}: {c} ({d}%)'
                            },
                            legend: {
                                orient: 'vertical',
                                left: 'left'
                            },
                            series: [
                                {
                                    name: 'Error Types',
                                    type: 'pie',
                                    radius: '60%',
                                    data: '${errorData}',
                                    emphasis: {
                                        itemStyle: {
                                            shadowBlur: 10,
                                            shadowOffsetX: 0,
                                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                                        }
                                    }
                                }
                            ]
                        },
                    },
                    md: 6,
                },
                {
                    type: 'card',
                    header: {
                        title: 'interfacePlatform.dashboard.recentApiCalls'
                    },
                    body: {
                        type: 'table',
                        source: '${recentApiCalls}',
                        columns: [
                            {
                                name: 'endpoint',
                                label: 'interfacePlatform.dashboard.endpoint',
                            },
                            {
                                name: 'method',
                                label: 'interfacePlatform.dashboard.method',
                            },
                            {
                                name: 'status',
                                label: 'table.status',
                            },
                            {
                                name: 'responseTime',
                                label: 'interfacePlatform.dashboard.responseTimeMs',
                            },
                            {
                                name: 'timestamp',
                                label: 'interfacePlatform.dashboard.timestamp',
                                type: 'datetime',
                                format: 'YYYY-MM-DD HH:mm:ss'
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
        '.label': {
            padding: '0.2rem 0.5rem',
            borderRadius: '3px',
            fontSize: '0.75rem',
        },
        '.label-success': {
            backgroundColor: '#28a745',
            color: 'white',
        },
        '.label-warning': {
            backgroundColor: '#ffc107',
            color: 'black',
        },
        '.label-danger': {
            backgroundColor: '#dc3545',
            color: 'white',
        },
    },
};

export default schema2component(schema);
