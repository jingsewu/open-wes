

module.exports = {
  "/api/chart/chartData": require("./chart.json"),
  "/api/chart/chart2": require("./chart2.json"),
  "/api/chart/chart3": require("./chart3.json"),
  "/api/login": {
    status: 0,
    msg: "登录成功",
  },
  "/api/form/save": {
    status: 0,
    msg: "",
  },
  "/api/schemaApi/demo1": {
    status: 0,
    msg: "",
    data: {
      type: "page",
      title: "这个是接口返回的amis JSON 生成的界面",
      body: {
        label: "弹框",
        type: "button",
        actionType: "dialog",
        dialog: {
          title: "弹框",
          body: "这是个简单的弹框。",
        },
      },
    },
  },
  "/api/menus": require("./menus"),
  "/api/customer": require("./customer"),
};
