import schema2component from "@/utils/schema2component"

const schema = {
    type: "page",
    data: {
        items: [
            {
                title: "dashboard.keyPoint.data.warehouse.location.occupy.rate",
                percent: "85%",
                increase: "5%"
            },
            {
                title: "dashboard.keyPoint.data.stock.transfer.daily",
                percent: "2%",
                increase: "0.05%"
            },
            {
                title: "dashboard.keyPoint.data.order.correct.rate",
                percent: "95%",
                increase: "5%"
            },
            {
                title: "dashboard.keyPoint.data.order.process.time",
                percent: "12",
                increase: "-2"
            },
            {
                title: "dashboard.keyPoint.data.operator.performance",
                percent: "98%",
                increase: "1%"
            }
        ]
    },
    body: [
        {
            type: "grid",
            columns: [
                {
                    type: "panel",
                    title: "dashboard.keyPoint.title",
                    body: {
                        type: "cards",
                        source: "$items",
                        itemClassName: "flex-1 mx-2",
                        card: {
                            body: [
                                {
                                    type: "tpl",
                                    tpl: "<h3>${title}</h3><p>${percent}</p><p>${increase}</p>"
                                }
                            ]
                        }
                    }
                }
            ]
        },
        {
            type: "grid",
            className: "col-sm-2  w-full",
            columns: [
                {
                    lg: 4,
                    md: 6,
                    sm: 12,
                    type: "panel",
                    title: "dashboard.order.inbound.title",
                    body: [
                        {
                            type: "chart",
                            title: "dashboard.order.inbound.data.count",
                            config: {
                                tooltip: {trigger: "axis"},
                                xAxis: {
                                    type: "category",
                                    data: ["dashboard.date.day", "dashboard.date.week", "dashboard.date.month"]
                                },
                                yAxis: {type: "value"},
                                series: [
                                    {
                                        data: [120, 1000, 5000],
                                        type: "bar",
                                        itemStyle: {color: "#4CAF50"},
                                        label: {show: true, position: "top"}
                                    }
                                ]
                            }
                        },
                        {
                            type: "chart",
                            title: "dashboard.order.inbound.data.status",
                            config: {
                                tooltip: {trigger: "item"},
                                legend: {bottom: "bottom"},
                                series: [
                                    {
                                        type: "pie",
                                        radius: "50%",
                                        center: ["center", "35%"],
                                        data: [
                                            {value: 50, name: "NEW"},
                                            {value: 20, name: "RECEIVING"},
                                            {value: 40, name: "DONE"},
                                            {value: 4, name: "CANCELED"},
                                            {value: 6, name: "CLOSED"}
                                        ]
                                    }
                                ]
                            }
                        }
                    ]
                },
                {
                    lg: 4,
                    md: 6,
                    sm: 12,
                    type: "panel",
                    title: "dashboard.order.outbound.title",
                    body: [
                        {
                            type: "chart",
                            title: "dashboard.order.outbound.data.count",
                            config: {
                                tooltip: {trigger: "axis"},
                                xAxis: {
                                    type: "category",
                                    data: ["dashboard.date.day", "dashboard.date.week", "dashboard.date.month"]
                                },
                                yAxis: {type: "value"},
                                series: [
                                    {
                                        data: [1500, 12000, 60000],
                                        type: "bar",
                                        itemStyle: {
                                            color: "#FF9800"
                                        },
                                        label: {show: true, position: "top"}
                                    }
                                ]
                            }
                        },
                        {
                            type: "chart",
                            title: "dashboard.order.outbound.data.status",
                            config: {
                                tooltip: {trigger: "item"},
                                legend: {
                                    // orient: "vertical",
                                    bottom: "bottom"
                                },
                                series: [
                                    {
                                        type: "pie",
                                        radius: "50%",
                                        center: ["center", "35%"],
                                        data: [
                                            {value: 600, name: "NEW"},
                                            {value: 10, name: "SHOT_WAITING"},
                                            {
                                                value: 300, name: "ASSIGNED"
                                            },
                                            {value: 200, name: "DISPATCHED"},
                                            {value: 200, name: "PICKING"},
                                            {value: 190, name: "DONE"},
                                            {value: 0, name: "CANCELED"}
                                        ]
                                    }
                                ]
                            }
                        }
                    ]
                },
                {
                    lg: 4,
                    md: 6,
                    sm: 12,
                    type: "panel",
                    title: "dashboard.order.stocktake.title",
                    body: [
                        {
                            type: "chart",
                            title: "dashboard.order.stocktake.data.status",
                            config: {
                                tooltip: {trigger: "axis"},
                                xAxis: {
                                    type: "category",
                                    data: ["dashboard.date.day", "dashboard.date.week", "dashboard.date.month"]
                                },
                                yAxis: {type: "value"},
                                series: [
                                    {
                                        data: [80, 600, 2500],
                                        type: "bar",
                                        itemStyle: {color: "#2196F3"},
                                        label: {show: true, position: "top"}
                                    }
                                ]
                            }
                        },
                        {
                            type: "chart",
                            title: "dashboard.order.stocktake.data.status",
                            config: {
                                tooltip: {trigger: "item"},
                                legend: {bottom: "bottom"},
                                series: [
                                    {
                                        type: "pie",
                                        radius: "50%",
                                        center: ["center", "35%"],
                                        data: [
                                            {value: 50, name: "NEW"},
                                            {value: 20, name: "PROCESSING"},
                                            {value: 10, name: "DONE"},
                                            {value: 0, name: "CANCELED"}
                                        ]
                                    }
                                ]
                            }
                        }
                    ]
                },
                {
                    lg: 4,
                    md: 6,
                    sm: 12,
                    type: "panel",
                    title: "dashboard.stock.title",
                    body: [
                        {
                            type: "chart",
                            title: "dashboard.stock.title",
                            config: {
                                tooltip: {trigger: "axis"},
                                xAxis: {
                                    type: "category",
                                    data: ["A", "B", "C"]
                                },
                                yAxis: [
                                    {type: "value", name: "total"},
                                    {type: "value", name: "rate"}
                                ],
                                series: [
                                    {
                                        type: "line",
                                        data: [10000, 15000, 8000]
                                    },
                                    {
                                        type: "bar",
                                        data: [2.0, 1.8, 2.5]
                                    }
                                ]
                            }
                        },
                        {
                            type: "crud",
                            title: "dashboard.stock.waring.title",
                            columns: [
                                {name: "product", label: "dashboard.stock.waring.data.product"},
                                {name: "currentStock", label: "dashboard.stock.waring.data.product.count"},
                                {name: "safeStock", label: "dashboard.stock.waring.data.product.safe.count"},
                                {name: "location", label: "dashboard.stock.waring.data.product.warehouse.area"}
                            ],
                            data: [
                                {
                                    product: "SKU_A",
                                    currentStock: 5,
                                    safeStock: "10-20",
                                    location: "Area1",
                                    status: "Low"
                                },
                                {
                                    product: "SKU_B",
                                    currentStock: 25,
                                    safeStock: "10-20",
                                    location: "Area2",
                                    status: "Normal"
                                }
                            ]
                        }
                    ]
                },
                {
                    lg: 4,
                    md: 6,
                    sm: 12,
                    type: "panel",
                    title: "dashboard.resource.title",
                    body: [
                        {
                            type: "crud",
                            title: "dashboard.resource.operator.assigned",
                            columns: [
                                {name: "employee", label: "dashboard.resource.operator.number"},
                                {name: "assignedTasks", label: "dashboard.resource.operator.assigned.tasks"},
                                {
                                    name: "completedTasks",
                                    label: "dashboard.resource.operator.complete.tasks"
                                },
                                {name: "completionRate", label: "dashboard.resource.operator.tasks.complete.rate"}
                            ],
                            data: [
                                {
                                    employee: "Lily",
                                    assignedTasks: 100,
                                    completedTasks: 80,
                                    completionRate: "80%"
                                },
                                {
                                    employee: "Lucy",
                                    assignedTasks: 150,
                                    completedTasks: 120,
                                    completionRate: "75%"
                                }
                            ]
                        },
                        {
                            type: "chart",
                            title: "dashboard.equipment.title",
                            config: {
                                series: [
                                    {
                                        type: "treemap",
                                        data: [
                                            {
                                                name: "Robot",
                                                value: 3,
                                                status: "Normal"
                                            },
                                            {
                                                name: "Conveyor",
                                                value: 2,
                                                status: "Normal"
                                            }
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
}

export default schema2component(schema)
