import schema2component from "@/utils/schema2component";


const schema = {
    "type": "page",
    "title": "dashboard.title",
    "body": [
        {
            "type": "grid",
            "columns": [
                {
                    "type": "panel",
                    "title": "dashboard.outboundProgress",
                    "body": {
                        "type": "chart",
                        "name": "outboundProgress",
                        "api": {
                            "method": "get",
                            "url": "/wms/api/dashboard/outbound-progress",
                            "adaptor": function(payload: { code: string; data: any[]; msg: any; }) {
                                if (payload.code === "0") {
                                    return {
                                        status: 0,
                                        data: {
                                            data: payload.data.map(item => ({
                                                value: parseInt(item.value),
                                                name: item.name
                                            }))
                                        }
                                    };
                                } else {
                                    return {
                                        status: payload.code,
                                        msg: payload.msg
                                    };
                                }
                            }
                        },
                        "interval": 30000,
                        "config": {
                            "tooltip": {
                                "trigger": "item",
                                "formatter": "{a} <br/>{b}: {c} ({d}%)"
                            },
                            "series": [{
                                "name": "${__('dashboard.outboundStatus')}",
                                "type": "pie",
                                "radius": ["50%", "70%"],
                                "avoidLabelOverlap": false,
                                "itemStyle": {
                                    "borderRadius": 10,
                                    "borderColor": "#fff",
                                    "borderWidth": 2
                                },
                                "label": {
                                    "show": true,
                                    "formatter": "{b}: {c}"
                                },
                                "emphasis": {
                                    "label": {
                                        "show": true,
                                        "fontSize": "18",
                                        "fontWeight": "bold"
                                    }
                                },
                                "data": "${data}"
                            }]
                        }
                    }
                },
                {
                    "type": "panel",
                    "title": "dashboard.inboundProgress",
                    "body": {
                        "type": "chart",
                        "name": "inboundProgress",
                        "api": {
                            "method": "get",
                            "url": "/wms/api/dashboard/inbound-progress",
                            "adaptor": function(payload: { code: string; data: any[]; msg: any; }) {
                                if (payload.code === "0") {
                                    return {
                                        status: 0,
                                        data: {
                                            data: payload.data.map(item => ({
                                                value: parseInt(item.value),
                                                name: item.name
                                            }))
                                        }
                                    };
                                } else {
                                    return {
                                        status: payload.code,
                                        msg: payload.msg
                                    };
                                }
                            }
                        },
                        "interval": 30000,
                        "config": {
                            "tooltip": {
                                "trigger": "item",
                                "formatter": "{a} <br/>{b}: {c} ({d}%)"
                            },
                            "series": [{
                                "name": "${__('dashboard.inboundStatus')}",
                                "type": "pie",
                                "radius": ["50%", "70%"],
                                "avoidLabelOverlap": false,
                                "itemStyle": {
                                    "borderRadius": 10,
                                    "borderColor": "#fff",
                                    "borderWidth": 2
                                },
                                "label": {
                                    "show": true,
                                    "formatter": "{b}: {c}"
                                },
                                "emphasis": {
                                    "label": {
                                        "show": true,
                                        "fontSize": "18",
                                        "fontWeight": "bold"
                                    }
                                },
                                "data": "${data}"
                            }]
                        }
                    }
                }
            ]
        },
        {
            "type": "panel",
            "title": "dashboard.flowTitle",
            "body": {
                "type": "chart",
                "api": "/wms/api/dashboard/hourly-flow",
                "interval": 60000,
                "config": {
                    "tooltip": {
                        "trigger": "axis",
                        "axisPointer": {
                            "type": "shadow"
                        }
                    },
                    "legend": {
                        "data": ["dashboard.inbound", "dashboard.outbound"]
                    },
                    "grid": {
                        "left": "3%",
                        "right": "4%",
                        "bottom": "3%",
                        "containLabel": true
                    },
                    "xAxis": {
                        "type": "category",
                        "data": ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"]
                    },
                    "yAxis": {"type": "value"},
                    "series": [
                        {
                            "name": "dashboard.inbound",
                            "type": "line",
                            "smooth": true,
                            "lineStyle": {
                                "width": 3
                            },
                            "data": ["$inbound"]
                        },
                        {
                            "name": "dashboard.outbound",
                            "type": "line",
                            "smooth": true,
                            "lineStyle": {
                                "width": 3
                            },
                            "data": ["$outbound"]
                        }
                    ]
                }
            }
        },
        {
            "type": "grid",
            "columns": [
                {
                    "type": "panel",
                    "title": "dashboard.operatorRanking",
                    "body": {
                        "type": "crud",
                        "api": "/wms/api/dashboard/operator-ranking",
                        "interval": 60000,
                        "columns": [
                            {"name": "operator", "label": "dashboard.operator", "sortable": true},
                            {"name": "totalQty", "label": "dashboard.totalQty", "sortable": true},
                            {"name": "acceptQty", "label": "dashboard.acceptQty", "sortable": true},
                            {"name": "pickingQty", "label": "dashboard.pickingQty", "sortable": true}
                        ]
                    }
                },
                {
                    "type": "panel",
                    "title": "dashboard.workstationDetails",
                    "body": {
                        "type": "crud",
                        "api": "/wms/api/dashboard/workstation-tasks",
                        "interval": 30000,
                        "columns": [
                            {"name": "stationCode", "label": "dashboard.stationCode", "sortable": true},
                            {"name": "taskType", "label": "dashboard.taskType", "sortable": true},
                            {"name": "taskCount", "label": "dashboard.taskCount", "sortable": true},
                            {"name": "requiredQty", "label": "dashboard.requiredQty", "sortable": true},
                            {"name": "operatedQty", "label": "dashboard.pickingQty", "sortable": true}
                        ]
                    }
                }
            ]
        }
    ]
};

export default schema2component(schema);
