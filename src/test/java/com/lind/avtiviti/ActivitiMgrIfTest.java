package com.lind.avtiviti;


import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 完善的工作流的流程-带有条件分支的.
 */
public class ActivitiMgrIfTest {
    @Test
    public void test2() throws Exception {
        ProcessEngine processEngine = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml").buildProcessEngine();
        System.out.println(processEngine);
    }

    //调用引擎,初始化
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    /**
     * 部署流程定义
     * ACT_RE_DEPLOYMENT:信息
     * ACT_RE_PROCDEF:流程定义数据表
     */
    @Test
    public void deployment() {
        InputStream inputStreamBpmn = this.getClass().getResourceAsStream("diagrams/user.bpmn");
        InputStream inputStreampng = this.getClass().getResourceAsStream("diagrams/user.png");
        //部署流程定义
        Deployment deployment = processEngine.getRepositoryService()//
                .createDeployment()//创建部署对象
                .addInputStream("diagrams/user.bpmn", inputStreamBpmn)//部署加载资源文件
                .addInputStream("diagrams/user.png", inputStreampng)//
                .deploy();

        System.out.println("部署ID：" + deployment.getId());
        //启动流程实例
        ProcessInstance pi = processEngine.getRuntimeService()//
                .startProcessInstanceByKey("user");//使用流程定义的key的最新版本启动流程
        System.out.println("流程实例ID：" + pi.getId());
        System.out.println("流程定义的ID：" + pi.getProcessDefinitionId());

    }

    /**
     * 启动流程实例
     * ACT_RU_TASK:运行时任务节点表
     * ACT_RU_EXECUTION:运行时流程执行实例表
     */
    @Test
    public void startProcessInstance() {
        String processDefinitionKey = "user";
        Map<String, Object> map = new HashMap<>();
        map.put("day", 3);//大于等于3天直接找manager审批
        ProcessInstance processInstance = processEngine.getRuntimeService()//与正在执行的流程实例和执行对象相关的Service
                .startProcessInstanceByKey(processDefinitionKey, map);//根据Key值来查询流程,也可以根据ID
        System.out.println("流程实例ID:" + processInstance.getId());  //2501
        //对应数据库act_ru_execution
        System.out.println("流程定义ID:" + processInstance.getProcessDefinitionId()); //helloword:1:4
        //对应数据库act_re_deployment
    }

    /**
     * 查询当前个人的任务
     * 下面测试数据需要手工修改ASSIGNEE_值
     * 测试：张三-员工
     * 测试：李四-mgr1
     * 测试：王五-mgr2
     */
    @Test
    public void fingByPerson() {
        //ACT_RU_TASK.ASSIGNEE_字段赋值为张三
        String assignee = "zhangsan";
        List<Task> list = processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .createTaskQuery()//创建任务查询对象
                .taskAssignee(assignee)//指定个人任务查询，指定代理人
                .list();//以list形式记录对象
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());//2501
                System.out.println("任务名称：" + task.getName());//提交申请
                System.out.println("任务的创建时间：" + task.getCreateTime());//Wed Jun 06 18:12:15 CST 2018
                System.out.println("任务的代理人：" + task.getAssignee());//张三
                System.out.println("流程实例ID：" + task.getProcessInstanceId());//2501
                System.out.println("执行对象ID：" + task.getExecutionId());//2501
                System.out.println("流程定义ID：" + task.getProcessDefinitionId());//helloword:1:4
            }
        }
    }

    /**
     * 完成个人任务
     * 流程执行的流程：部署----启动流程实例-----查询当前正在执行的流程-----然后提交，当前（提交申请）的流程结束----会转到下一个流程执行者处理。
     */
    @Test
    public void complete() {
        String taskId = "5002";
        processEngine.getTaskService()
                .complete(taskId);
        System.out.println("完成任务ID：" + taskId);
    }
}
