# Jupiter

### 开发指南
1. 如果需要增加新的应用，则在pages目录下增加一个新的目录  
eg: 增加一个user应用，则创建一个user的目录
2. 在对应的应用下创建相应的模块目录
3. 在模块目录下增加页面
4. 页面根据amis元数据来创建，参考src/pages/wms/config_center/warehouse/warehouse_management/index.tsx
5. 在path2Compoment.tsx中增加页面组件

> amis: https://baidu.github.io/amis/zh-CN/docs/index

### 环境需求
#### node : V18+


### 服务启动
1. npm install 
2. npm start


### 注意事项
1. 在执行npm start启动的时候,可能会出现
```azure
Module not found: Error: Can't resolve 'react-overlays/useRootClose' in xxx
```
则需要执行:
```azure
npm install react-overlays
```
2. 因为本地packing的host默认为test.baidu.com，需要将本机的host文件增加一列
```agsl
xxx.xxx.xxx.xxx test.baidu.com
```
xxx.xxx.xxx.xxx为本机ip

