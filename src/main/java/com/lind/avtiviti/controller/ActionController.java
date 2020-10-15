package com.lind.avtiviti.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动作处理.
 */
@RestController
public class ActionController {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ProcessEngine processEngine;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    HistoryService historyService;
    @Autowired
    TaskService taskService;

    /**
     * 激活流程定义，通过/deployment/list来查看流程列表里的ACT_RE_PROCDEF.
     *
     * @param procDefId ACT_RE_PROCDEF.ID_
     */
    @RequestMapping(value = "/execution/active/{procDefId}", method = RequestMethod.GET)
    public String active(@PathVariable String procDefId) {
        repositoryService.activateProcessDefinitionById(procDefId, true, new Date());
        return "激活成功";
    }

    /**
     * 启动流程实例 数据在ACT_RU_TASK和ACT_RU_JOB和ACT_RU_EXECUTION表生成记录.
     * /execution/list接口可以获取到数据
     *
     * @param procDefId act_re_procdef.ID_
     */
    @RequestMapping(value = "/execution/start/{procDefId}", method = RequestMethod.GET)
    public void createInstance(@PathVariable String procDefId, @RequestParam String title, HttpServletResponse response) throws IOException {
        // 启动流程
        ProcessInstance pi = runtimeService.startProcessInstanceById(procDefId);
        // 设置流程实例名称
        runtimeService.setProcessInstanceName(pi.getId(), title);
        response.sendRedirect("/view/execution/list");
    }

    /**
     * 任务强制完成
     *
     * @param id
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/task/complete/{id}", method = RequestMethod.GET)
    public void taskComplete(@PathVariable String id, HttpServletResponse response) throws IOException {
        taskService.complete(id);
        response.sendRedirect("/view/execution/list");
    }

    /**
     * 假删除部署
     *
     * @param id
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/deployment/del/{id}", method = RequestMethod.GET)
    public void deploymentDel(@PathVariable String id, HttpServletResponse response) throws IOException {
        processEngine.getRepositoryService().suspendProcessDefinitionById(id);
        response.sendRedirect("/view/deployment/list");
    }

    /**
     * 流程实例的任务的审批.
     *
     * @param procInstId 流程实例ID ACT_RU_TASK.PROC_INST_ID_
     * @param taskId     任务ID ACT_RU_TASK.ID_
     * @param assignee   分配人
     * @param comment    备注
     */
    @RequestMapping(value = "/execution/pass/{procInstId}/{taskId}", method = RequestMethod.GET)
    public void pass(@PathVariable String procInstId,
                     @PathVariable String taskId,
                     @RequestParam(required = false) String assignee,
                     @RequestParam(required = false) String comment,
                     @RequestParam(required = false) String params,
                     HttpServletResponse response
    ) throws IOException {

        if (StringUtils.isBlank(comment)) {
            comment = "";
        }
        taskService.addComment(taskId, procInstId, comment);
        if (assignee != null) {
            taskService.setAssignee(taskId, assignee);
        }
        Map map = new HashMap();
        if (StringUtils.isNoneBlank(params)) {
            String[] keys = StringUtils.split(params, "-");
            for (String val : keys) {
                String[] vals = StringUtils.split(val, "_");
                String[] arr = StringUtils.split(vals[1], ",");
                if (arr != null && arr.length > 1) {//表示多个元素，需要转成数组
                    map.put(vals[0], Arrays.asList(arr));
                } else {
                    map.put(vals[0], vals[1]);
                }
            }
        }

        taskService.complete(taskId, map);
        response.sendRedirect("/view/execution/list");
    }

    /**
     * 任务节点审批驳回.
     *
     * @param procInstId
     * @param taskId
     * @param comment
     * @param destTaskKey 驳回到的节点
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/execution/back/{procInstId}/{taskId}", method = RequestMethod.GET)
    public void back(@PathVariable String procInstId,
                     @PathVariable String taskId,
                     @RequestParam(required = false) String comment,
                     @RequestParam(required = false) String destTaskKey,
                     HttpServletResponse response) throws IOException {

        if (StringUtils.isBlank(comment)) {
            comment = "";
        }
        taskService.addComment(taskId, procInstId, comment);
        Map<String, Object> variables;
        // 取得当前任务
        HistoricTaskInstance currTask = historyService
                .createHistoricTaskInstanceQuery().taskId(taskId)
                .singleResult();
        // 取得流程实例
        ProcessInstance instance = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(currTask.getProcessInstanceId())
                .singleResult();
        if (instance == null) {
            throw new IllegalArgumentException("流程已经结束");
        }
        variables = instance.getProcessVariables();
        // 取得流程定义
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(currTask
                        .getProcessDefinitionId());
        if (definition == null) {
            throw new IllegalArgumentException("流程定义未找到");
        }

        // 取得上一步活动
        ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                .findActivity(destTaskKey);

        List<PvmTransition> nextTransitionList = currActivity
                .getIncomingTransitions();
        // 清除当前活动的出口
        List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
        List<PvmTransition> pvmTransitionList = currActivity
                .getOutgoingTransitions();
        for (PvmTransition pvmTransition : pvmTransitionList) {
            oriPvmTransitionList.add(pvmTransition);
        }
        pvmTransitionList.clear();

        // 建立新出口
        List<TransitionImpl> newTransitions = new ArrayList<TransitionImpl>();
        for (PvmTransition nextTransition : nextTransitionList) {
            PvmActivity nextActivity = nextTransition.getSource();
            ActivityImpl nextActivityImpl = ((ProcessDefinitionImpl) definition)
                    .findActivity(nextActivity.getId());
            TransitionImpl newTransition = currActivity
                    .createOutgoingTransition();
            newTransition.setDestination(nextActivityImpl);
            newTransitions.add(newTransition);
        }
        // 完成任务
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(instance.getId())
                .taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
        for (Task task : tasks) {
            taskService.complete(task.getId(), variables);
            historyService.deleteHistoricTaskInstance(task.getId());
        }
        // 恢复方向
        for (TransitionImpl transitionImpl : newTransitions) {
            currActivity.getOutgoingTransitions().remove(transitionImpl);
        }
        for (PvmTransition pvmTransition : oriPvmTransitionList) {
            pvmTransitionList.add(pvmTransition);
        }
        response.sendRedirect("/view/execution/list");
    }
}
