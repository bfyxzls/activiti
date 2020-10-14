package com.lind.avtiviti.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.lind.avtiviti.config.ActivitiConfig;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("view")
@Slf4j
public class ViewController {

    static final String modelName = "modelName";
    static final String modelKey = "modelKey";
    static final String description = "description";
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
    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;
    @Autowired
    ActivitiConfig.ActivitiExtendProperties properties;
    @Autowired
    HttpMessageConverters httpMessageConverters;

    /**
     * 模模型列表.
     */
    @RequestMapping(value = "/model/list", method = RequestMethod.GET)
    public String modelist(Model model) {
        List<org.activiti.engine.repository.Model> list = processEngine.getRepositoryService().createModelQuery()
                .orderByCreateTime().desc()
                .list();

        model.addAttribute("result", list);
        return "view/model-list";

    }


    /**
     * 流程列表.
     */
    @RequestMapping(value = "/deployment/list", method = RequestMethod.GET)
    public String deployment(org.springframework.ui.Model model) {
        List<Deployment> list = processEngine.getRepositoryService().createDeploymentQuery()
                .orderByDeploymenTime()
                .desc()
                .list();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Deployment item : list) {
            ProcessDefinition processDefinition = processEngine.getRepositoryService()
                    .createProcessDefinitionQuery()
                    .deploymentId(item.getId())
                    .singleResult();
            result.add(ImmutableMap.of(
                    "id", item.getId(),
                    "time", item.getDeploymentTime(),
                    "name", item.getName(),
                    "proDefId", processDefinition.getId()
            ));
        }
        model.addAttribute("result", result);
        return "view/deployment-list";

    }

    /**
     * 当前运行中的流程实例列表，应该是启动了的流程（/execution/start/会出现的流程）.
     */
    @RequestMapping(value = "/execution/list", method = RequestMethod.GET)
    public String execution(Model model) {
        List<ProcessInstance> list =
                runtimeService.createProcessInstanceQuery()
                        .orderByProcessDefinitionId()
                        .desc()
                        .list();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ProcessInstance item : list) {
            log.info("execution.id={},proc_inst_id={},proc_def_id={},isSuspended={}", item.getId(),
                    item.getProcessInstanceId(), item.getProcessDefinitionId(), item.isSuspended());
            Task task =
                    taskService.createTaskQuery()
                            .active()
                            .processInstanceId(item.getId())
                            .singleResult();
            result.add(ImmutableMap.of(
                    "id", item.getId(),
                    "proDefId", item.getProcessDefinitionId(),
                    "isSuspended", item.isSuspended(),
                    "taskId", task.getId(),
                    "taskName", task.getName()
            ));
        }
        model.addAttribute("result", result);
        return "view/execution-list";
    }

    /**
     * 历史记录列表.
     *
     */
    @RequestMapping(value = "/history/finished-list", method = RequestMethod.GET)
    public String historyList(Model model) {
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .finished().orderByTaskCreateTime().desc().list();
        model.addAttribute("result", list);
        return "view/history-list";
    }

}
