package com.lind.avtiviti.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.lind.avtiviti.config.ActivitiConfig;
import com.lind.avtiviti.entity.ActReNode;
import com.lind.avtiviti.repository.ActReNodeRepository;
import com.lind.avtiviti.util.ActivitiHelper;
import com.lind.avtiviti.vo.ProcessNodeVo;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    ActReNodeRepository actReNodeRepository;
    @Autowired
    ActivitiHelper activitiHelper;

    private static int longCompare(Date obj1, Date obj2) {
        return obj1.compareTo(obj2);
    }

    /**
     * 模模型列表.
     */
    @RequestMapping(value = "/model/list", method = RequestMethod.GET)
    public String modelist(Model model,
                           @RequestParam(required = false, defaultValue = "1") int pageindex,
                           @RequestParam(required = false, defaultValue = "10") int pagesize) {
        pageindex = (pageindex - 1) * pagesize;
        List<org.activiti.engine.repository.Model> list = processEngine.getRepositoryService().createModelQuery()
                .orderByCreateTime().desc()
                .listPage(pageindex, pagesize);

        model.addAttribute("result", list);
        return "view/model-list";

    }

    @GetMapping("deployByFile")
    public String deployByFile() {
        return "/view/model-upload";
    }

    /**
     * 转化流程为模型.
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/convertToModel/{id}", method = RequestMethod.GET)
    public void convertToModel(@PathVariable String id, HttpServletResponse response) throws IOException {

        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        InputStream bpmnStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());

        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
            XMLStreamReader xtr = xif.createXMLStreamReader(in);
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
            BpmnJsonConverter converter = new BpmnJsonConverter();

            ObjectNode modelNode = converter.convertToJson(bpmnModel);
            org.activiti.engine.repository.Model modelData = repositoryService.newModel();
            modelData.setKey(pd.getKey());
            modelData.setName(pd.getResourceName());

            ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, pd.getName());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, pd.getDescription());
            modelData.setMetaInfo(modelObjectNode.toString());

            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));
        } catch (Exception e) {
            log.error(e.toString());
            throw new IllegalArgumentException("转化流程为模型失败");
        }
    }

    /**
     * 部署列表.
     */
    @RequestMapping(value = "/deployment/list", method = RequestMethod.GET)
    public String deployment(org.springframework.ui.Model model,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false, defaultValue = "1") int pageindex,
                             @RequestParam(required = false, defaultValue = "10") int pagesize) {
        pageindex = (pageindex - 1) * pagesize;
        List<Deployment> list = processEngine.getRepositoryService().createDeploymentQuery()
                .orderByDeploymenTime()
                .desc()
                .listPage(pageindex, pagesize);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Deployment item : list) {
            ProcessDefinition processDefinition = processEngine.getRepositoryService()
                    .createProcessDefinitionQuery()
                    .deploymentId(item.getId())
                    .singleResult();
            if (StringUtils.isBlank(status)) {
                result.add(ImmutableMap.of(
                        "id", item.getId(),
                        "time", item.getDeploymentTime(),
                        "name", item.getName(),
                        "proDefId", processDefinition.getId(),
                        "isSuspended", processDefinition.isSuspended()
                ));
            } else if (status.equals("1")) {
                if (!processDefinition.isSuspended())
                    result.add(ImmutableMap.of(
                            "id", item.getId(),
                            "time", item.getDeploymentTime(),
                            "name", item.getName(),
                            "proDefId", processDefinition.getId(),
                            "isSuspended", processDefinition.isSuspended()
                    ));
            } else if (status.equals("2")) {
                if (processDefinition.isSuspended())
                    result.add(ImmutableMap.of(
                            "id", item.getId(),
                            "time", item.getDeploymentTime(),
                            "name", item.getName(),
                            "proDefId", processDefinition.getId(),
                            "isSuspended", processDefinition.isSuspended()
                    ));
            }

        }
        model.addAttribute("result", result);
        return "view/deployment-list";

    }

    /**
     * 当前运行中的流程实例列表，应该是启动了的流程（/execution/start/会出现的流程）.
     */
    @RequestMapping(value = "/execution/list", method = RequestMethod.GET)
    public String execution(Model model,
                            @RequestParam(required = false, defaultValue = "1") int pageindex,
                            @RequestParam(required = false, defaultValue = "10") int pagesize) throws Exception {
        pageindex = (pageindex - 1) * pagesize;


        List<ProcessInstance> list =
                runtimeService.createProcessInstanceQuery()
                        .orderByProcessInstanceId()
                        .desc()
                        .listPage(pageindex, pagesize);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProcessInstance item : list) {
            List<Task> tasks =
                    taskService.createTaskQuery()
                            .active()
                            .processInstanceId(item.getId()).list();//并行网关可能是多条任务
            ExecutionEntity execution = (ExecutionEntity) runtimeService.createProcessInstanceQuery()
                    .processInstanceId(item.getProcessInstanceId()).singleResult();


            for (Task task : tasks) {

                String owner = task.getOwner() == null ? "" : task.getOwner();
                String assignee = task.getAssignee() == null ? "" : task.getAssignee();

                ActReNode actReNode = actReNodeRepository.findByNodeIdAndProcessDefId(execution.getActivityId(),item.getProcessDefinitionId());
                Integer rejectFlag;
                if (actReNode == null) {
                    rejectFlag = 0;
                } else {
                    rejectFlag = actReNode.getRejectFlag();
                }
                result.add(new ImmutableMap.Builder<String, Object>()
                        .put("id", item.getId())
                        .put("proDefId", item.getProcessDefinitionId())
                        .put("isSuspended", item.isSuspended())
                        .put("executionId", task.getExecutionId())
                        .put("taskId", task.getId())
                        .put("taskName", task.getName())
                        .put("time", task.getCreateTime())
                        .put("owner", owner)
                        .put("assignee", assignee)
                        .put("rejectFlag", rejectFlag)
                        .build());
            }
        }
        result = result.stream()
                .sorted((i, j) -> longCompare((Date) j.get("time"), (Date) i.get("time")))
                .collect(Collectors.toList());

        model.addAttribute("result", result);
        return "view/execution-list";
    }

    /**
     * 历史流程列表.
     */
    @RequestMapping(value = "/history/list", method = RequestMethod.GET)
    public String historyList(Model model,
                              @RequestParam(required = false, defaultValue = "1") int pageindex,
                              @RequestParam(required = false, defaultValue = "10") int pagesize) {
        pageindex = (pageindex - 1) * pagesize;
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
                //     .finished()
                .orderByProcessInstanceStartTime()
                .desc()
                .listPage(pageindex, pagesize);

        model.addAttribute("result", list);
        return "view/history-list";
    }

    /**
     * 已完成的历史记录列表.
     */
    @RequestMapping(value = "/task/list/{id}", method = RequestMethod.GET)
    public String taskList(@PathVariable String id, Model model) {
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(id)
                .finished()
                .orderByTaskCreateTime()
                .desc()
                .list();
        model.addAttribute("id", id);
        model.addAttribute("result", list);
        return "view/task-list";
    }

    /**
     * 通过流程定义id获取流程节点.
     *
     * @param procDefId 流程定义ID
     */
    @RequestMapping(value = "/deployment/node-list/{procDefId}", method = RequestMethod.GET)
    public String getProcessNode(@PathVariable String procDefId, Model model) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(procDefId);
        List<Process> processes = bpmnModel.getProcesses();
        List<ProcessNodeVo> processNodeVos = new ArrayList<>();
        for (Process process : processes) {
            Collection<FlowElement> elements = process.getFlowElements();
            for (FlowElement element : elements) {
                if (element instanceof UserTask) {
                    ProcessNodeVo node = new ProcessNodeVo();
                    node.setNodeId(element.getId());
                    node.setTitle(element.getName());
                    ActReNode actReNode = actReNodeRepository.findByNodeIdAndProcessDefId(element.getId(), procDefId);
                    if (actReNode != null) {
                        node.setAssignee(actReNode.getRoleId()); //指定的角色
                        node.setRejectFlag(actReNode.getRejectFlag());
                    }
                    processNodeVos.add(node);
                }
            }
        }
        log.info("processNodeVos:{}", processNodeVos);
        model.addAttribute("procDefId", procDefId);
        model.addAttribute("result", processNodeVos);

        return "view/node-list";
    }


}
