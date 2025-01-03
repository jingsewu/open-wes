import schema2component from "@/utils/schema2component";

const schema = {
    type: 'page',
    title: '仓库执行系统 Dashboard',
    body: [
        {
            type: 'grid',
            columns: [
                {
                    type: 'panel',
                    title: '关键指标汇总',
                    body: [
                        {
                            type: 'grid',
                            columns: [
                                {
                                    type: 'card',
                                    className: 'kpi-card',
                                    body: [
                                        {
                                            type: 'tpl',
                                            tpl: '<h3>仓库空间利用率</h3><p>85%</p><p>环比上升 5%</p>'
                                        }
                                    ]
                                },
                                {
                                    type: 'card',
                                    className: 'kpi-card',
                                    body: [
                                        {
                                            type: 'tpl',
                                            tpl: '<h3>库存周转率(日)</h3><p>2%</p><p>环比上升 0.05%</p>'
                                        }
                                    ]
                                },
                                {
                                    type: 'card',
                                    className: 'kpi-card',
                                    body: [
                                        {
                                            type: 'tpl',
                                            tpl: '<h3>订单准确率</h3><p>95%</p><p>环比上升 5%</p>'
                                        }
                                    ]
                                },
                                {
                                    type: 'card',
                                    className: 'kpi-card',
                                    body: [
                                        {
                                            type: 'tpl',
                                            tpl: '<h3>平均订单处理时长</h3><p>12分钟</p><p>环比下降 2分钟</p>'
                                        }
                                    ]
                                },
                                {
                                    type: 'card',
                                    className: 'kpi-card',
                                    body: [
                                        {
                                            type: 'tpl',
                                            tpl: '<h3>人员劳动生产率</h3><p>98</p><p>同比下降 1%</p>'
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ]
        },
        {
            type: 'grid',
            columns: [
                {
                    type: 'panel',
                    title: '订单处理区域 - 入库',
                    body: [
                        {
                            type: 'chart',
                            title: '入库订单总量统计',
                            config: {
                                tooltip: {trigger: 'axis'},
                                xAxis: {type: 'category', data: ['日', '周', '月']},
                                yAxis: {type: 'value'},
                                series: [
                                    {
                                        data: [120, 1000, 5000],
                                        type: 'bar',
                                        itemStyle: {color: '#4CAF50'},
                                        label: {show: true, position: 'top'}
                                    }
                                ]
                            }
                        },
                        {
                            type: 'chart',
                            title: '入库订单状态分布',
                            config: {
                                tooltip: {trigger: 'item'},
                                legend: {orient: 'vertical', left: 'left'},
                                series: [
                                    {
                                        name: '订单状态',
                                        type: 'pie',
                                        radius: '50%',
                                        data: [
                                            {value: 50, name: '新单据'},
                                            {value: 20, name: '收货中'},
                                            {value: 40, name: '收货完成'},
                                            {value: 4, name: '取消'},
                                            {value: 6, name: '关闭'}
                                        ]
                                    }
                                ]
                            }
                        }
                    ]
                },
                {
                    type: 'panel',
                    title: '订单处理区域 - 出库',
                    body: [
                        {
                            type: 'chart',
                            title: '出库订单总量统计',
                            config: {
                                tooltip: {trigger: 'axis'},
                                xAxis: {type: 'category', data: ['日', '周', '月']},
                                yAxis: {type: 'value'},
                                series: [
                                    {
                                        data: [1500, 12000, 60000],
                                        type: 'bar',
                                        itemStyle: {color: '#FF9800'},
                                        label: {show: true, position: 'top'}
                                    }
                                ]
                            }
                        },
                        {
                            type: 'chart',
                            title: '出库订单状态分布',
                            config: {
                                tooltip: {trigger: 'item'},
                                legend: {orient: 'vertical', left: 'left'},
                                series: [
                                    {
                                        name: '订单状态',
                                        type: 'pie',
                                        radius: '50%',
                                        data: [
                                            {value: 600, name: '新单据'},
                                            {value: 10, name: '缺货等待'},
                                            {value: 300, name: '分配完成(库区)'},
                                            {value: 200, name: '派单完成'},
                                            {value: 200, name: '拣货中'},
                                            {value: 190, name: '已完成'},
                                            {value: 0, name: '已取消'}
                                        ]
                                    }
                                ]
                            }
                        }
                    ]
                },
                {
                    type: 'panel',
                    title: '订单处理区域 - 盘点',
                    body: [
                        {
                            type: 'chart',
                            title: '盘点订单总量统计',
                            config: {
                                tooltip: {trigger: 'axis'},
                                xAxis: {type: 'category', data: ['日', '周', '月']},
                                yAxis: {type: 'value'},
                                series: [
                                    {
                                        data: [80, 100, 90],
                                        type: 'bar',
                                        itemStyle: {color: '#2196F3'},
                                        label: {show: true, position: 'top'}
                                    }
                                ]
                            }
                        },
                        {
                            type: 'chart',
                            title: '盘点订单状态分布',
                            config: {
                                tooltip: {trigger: 'item'},
                                legend: {orient: 'vertical', left: 'left'},
                                series: [
                                    {
                                        name: '订单状态',
                                        type: 'pie',
                                        radius: '50%',
                                        data: [
                                            {value: 50, name: '新单据'},
                                            {value: 20, name: '盘点中'},
                                            {value: 10, name: '完成'},
                                            {value: 0, name: '取消'}
                                        ]
                                    }
                                ]
                            }
                        }
                    ]
                },
                {
                    type: 'panel',
                    title: '库存管理区域',
                    body: [
                        {
                            type: 'chart',
                            title: '库存总量与周转率',
                            config: {
                                tooltip: {trigger: 'axis'},
                                xAxis: {type: 'category', data: ['品类A', '品类B', '品类C']},
                                yAxis: [
                                    {type: 'value', name: '库存总量'},
                                    {type: 'value', name: '周转率'}
                                ],
                                series: [
                                    {
                                        name: '库存总量',
                                        type: 'line',
                                        data: [10000, 15000, 8000]
                                    },
                                    {
                                        name: '周转率',
                                        type: 'bar',
                                        data: [2.0, 1.8, 2.5]
                                    }
                                ]
                            }
                        },
                        {
                            type: 'crud',
                            title: '库存预警提示',
                            columns: [
                                {name: 'product', label: '商品名称'},
                                {name: 'currentStock', label: '当前库存'},
                                {name: 'safeStock', label: '安全库存范围'},
                                {name: 'location', label: '所在仓库位置'}
                            ],
                            source: [
                                {
                                    product: '商品A',
                                    currentStock: 5,
                                    safeStock: '10-20',
                                    location: '区域1',
                                    status: '低库存'
                                },
                                {
                                    product: '商品B',
                                    currentStock: 25,
                                    safeStock: '10-20',
                                    location: '区域2',
                                    status: '正常'
                                }
                            ]
                        }
                    ]
                },
                {
                    type: 'panel',
                    title: '资源分配区域',
                    body: [
                        {
                            type: 'crud',
                            title: '人员任务分配情况',
                            columns: [
                                {name: 'employee', label: '员工编号'},
                                {name: 'assignedTasks', label: '分配任务数'},
                                {name: 'completedTasks', label: '已完成任务数'},
                                {name: 'completionRate', label: '任务完成率'}
                            ],
                            source: [
                                {employee: '001', assignedTasks: 100, completedTasks: 80, completionRate: '80%'},
                                {employee: '002', assignedTasks: 150, completedTasks: 120, completionRate: '75%'}
                            ]
                        },
                        {
                            type: 'chart',
                            title: '设备运行状态',
                            config: {
                                series: [
                                    {
                                        type: 'treemap',
                                        data: [
                                            {name: '设备A', value: 1, status: '正常'},
                                            {name: '设备B', value: 1, status: '待机'},
                                            {name: '设备C', value: 1, status: '故障'}
                                        ]
                                    }
                                ]
                            }
                        }
                    ]
                }
            ]
        }
    ]
};


export default schema2component(schema)
