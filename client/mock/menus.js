module.exports= {
    status: 0,
    data: [
      {
        label: "导航",
        children: [
          {
            path: "/dashboard",
            label: "Dashboard",
            icon: "fa fa-gauge",
          },
          {
            path: "/dialog/simple",
            label: "Dialog",
            icon: "fa fa-bomb",
          },
          {
            label: "会员管理",
            icon: "fa fa-user",
            children: [
              {
                label: "列表",
                path: "/customer/index",
              },
            ],
          },
          {
            label: "选项卡",
            icon: "fa fa-clone",
            children: [
              {
                label: "常规选项卡",
                path: "/tabs",
              },
            ],
          },
          {
            path: "/icon/simple",
            label: "Icon",
            icon: "fa fa-th",
          },
          {
            label: "SchemaApi",
            icon: "fa fa-th",
            children: [
              {
                path: "/schemaApi/demo1",
                label: "渲染接口amis",
                icon: "fa fa-th",
              },
            ],
          },
          {
            label: "表单页面",
            icon: "fas fa-pen-to-square",
            children: [
              {
                label: "常规表单",
                path: "/form/basic",
                children: [
                  {
                    label: "三级目录测试",
                    path: "/form/basic/advanced",
                  },
                ],
              },

              {
                label: "复杂表单",
                path: "/form/advanced",
              },

              {
                label: "向导",
                path: "/form/wizard",
              },

              {
                label: "代码编辑器",
                path: "/form/editor",
              },
            ],
          },
        ],
      },
    ],
  }