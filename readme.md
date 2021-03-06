# activiti组件介绍
## 一 供其它系统调用的的接口
### 模型相关接口
1. 获取模型：/modeler.html?modelId=model.id
2. 添加模型：/model/create
3. 保存模型：/model/{model.id}/save
4. 删除模型：/model/delete
5. 模型部署：/model/deploy{model.id}
6. 模型列表：/model/list
7. 通过XML文件建立模型: /deployByFile
### 流程部署相关接口
1. 流程的激活： /deployment/active/{procDefId}
2. 流程的挂起：/deployment/suspend/{procDefId}
3. 流程的导出为XML文件： /deployment/export?id={procDefId}
4. 流程转为模型：/deployment/convertToModel/{procDefId}
5. 流程部署列表：/deployment/list
6. 流程部署的节点列表：/deployment/node-list/{procDefId}
7. 节点配置保存：/deployment/node-save
### 流程实例相关接口
1. 流程实例列表：/execution/list
2. 启动一个实例：/execution/start/{procDefId}
3. 完成一个实例：/task/complete/{id}
4. 流程任务审批：/execution/pass/{procInstId}/{taskId}
5. 流程任务驳回：/execution/back/{procInstId}/{taskId}
6. 当前任务下一节点：/process/next-node/{procInstId}
7. 某个流程已完成的任务：/task/list/{procInstId}
8. 已完成的任务：/history/list

## 二 实现的功能
1. 审核
2. 驳回到节点
3. 会签，多个审批人进行审核，相满足一定数量的通过数后，去下一节点
4. 网关-并行，当多个并行的分支都通过后，才到达下一节点
5. 网关-互斥，满足哪个走哪个，然后到哪个的下一节点
### 会签
* 多实例类型： 并行(parallel)， 顺序(sequential)
* 集合（多实例）：主要指需要参与会签的人
* 完成条件（多实现)：${complete/allTask==1}，表示当完成会签数据与所有任务数的比值
## 三 数据流向
### 建立模型
* http://localhost:8081/model/create ACT_RE_MODEL,ACT_GE_BYTEARRAY
1. ACT_RE_MODEL.EDITOR_SOURCE_VALUE_ID_和ACT_RE_MODEL.EDITOR_SOURCE_EXTRA_VALUE_ID_在ACT_GE_BYTEARRAY对应两条记录
2. ACT_RE_MODEL表里EDITOR_SOURCE_VALUE_ID_对应ACT_GE_BYTEARRAY的ID_，表示该模型对应的模型文件（json格式数据） 
3. ACT_RE_MODEL表里EDITOR_SOURCE_EXTRA_VALUE_ID_对应ACT_GE_BYTEARRAY的ID_，表示该模型生成的图片文件
> /model/1/save ACT_RE_MODEL
更新ACT_RE_MODEL里对应的name,key,description等值
### 发布模型成为流程
* http://localhost:8081/model/deploy/1 ACT_RE_DEPLOYMENT,ACT_RE_PROCDEF
当模型发布之后，将会产生一个流程，而下次发布时，还会产生一个新的，所以模型与流程是一对多的关系；而对于流程的部署表也是一样，每发布
一次，在部署表里也会多一条记录，流程表里会记录部署表的ID_
1. ACT_RE_DEPLOYMENT添加数据
2. ACT_RE_PROCDEF添加数据
3. ACT_GE_BYTEARRAY里的DEPLOYMENT_ID_对应ACT_RE_DEPLOYMENT的ID_
### 启动流程成为实例
* http://localhost:8081/model/instance-create/{procDefId}
> 模型---发布---流程---启动---流程实例---任务---步骤审核---进入ACT_HI_TASKINST
* ACT_RU_EXECUTION 流程实例表
* ACT_RU_TASK 流程运行任务表 
### 审核节点 
* http://localhost:8081/model/pass/{procInstId}/{taskId}
每次启动一个流程，都对对应一个新的流程实例（ACT_RU_EXECUTION）和多条任务（ACT_RU_TASK），即ACT_RU_EXECUTION和ACT_RE_PROCDEF添加数据
是多对一的关系
### 下一个节点
* http://localhost:8081/model/getNextNode/{procInstId}
### 查看当前流程图像
* http://localhost:8081/model//getHighlightImg/{procInstId}
### 流程节点权限
ACT_RU_EXECUTION.ACT_ID_：流程实例中节点的节点ID，以后权限权限主要根据这个字段

## 四 数据库语句
### 建立保存模型
```
select * from ACT_RE_MODEL where ID_=7501
select * from ACT_GE_BYTEARRAY where ID_ in (select EDITOR_SOURCE_VALUE_ID_ from ACT_RE_MODEL where ID_=7501)
select * from ACT_GE_BYTEARRAY where ID_  in (select EDITOR_SOURCE_EXTRA_VALUE_ID_ from ACT_RE_MODEL where ID_=7501)
```
### 发布模型
```
select * from ACT_RE_DEPLOYMENT where ID_ in (select DEPLOYMENT_ID_ from ACT_RE_MODEL where ID_=7501)
select * from ACT_RE_PROCDEF
select * from ACT_GE_BYTEARRAY
```
### 启动流程实例,正在运行的实例 
```
select * from ACT_RU_EXECUTION
```
### 查看当前任务
```
select * from ACT_RU_TASK
select * from ACT_RU_VARIABLE
```