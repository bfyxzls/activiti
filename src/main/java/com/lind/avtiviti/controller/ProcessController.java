package com.lind.avtiviti.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.lind.avtiviti.config.ActivitiConfig;
import com.lind.avtiviti.util.ActivitiHelper;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class ProcessController {


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
    ActivitiHelper activitiHelper;

    /**
     * 正在被处理的任务.
     */
    @RequestMapping(value = "/execution/getRunningProcess", method = RequestMethod.GET)
    public Object getRunningProcess(@RequestParam(required = false) String name,
                                    @RequestParam(required = false) String categoryId,
                                    @RequestParam(required = false) String key) {

        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
                .orderByProcessInstanceId().desc();

        if (StringUtils.isNotBlank(name)) {
            query.processInstanceNameLike("%" + name + "%");
        }
        if (StringUtils.isNotBlank(categoryId)) {
            query.processDefinitionCategory(categoryId);
        }
        if (StringUtils.isNotBlank(key)) {
            query.processDefinitionKey(key);
        }
        List<ProcessInstance> processInstanceList = query.listPage(1, 10);
        return processInstanceList;
    }

    /**
     * 第一个流程节点.
     *
     * @param procDefId act_re_procdef.ID_
     */
    @RequestMapping(value = "/execution/getFirstNode/{procDefId}", method = RequestMethod.GET)
    public Object getFirstNode(@PathVariable String procDefId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(procDefId);
        List<Process> processes = bpmnModel.getProcesses();
        Collection<FlowElement> elements = processes.get(0).getFlowElements();
        // 流程开始节点
        StartEvent startEvent = null;
        for (FlowElement element : elements) {
            if (element instanceof StartEvent) {
                startEvent = (StartEvent) element;
                break;
            }
        }
        FlowElement e = null;
        // 判断开始后的流向节点
        SequenceFlow sequenceFlow = startEvent.getOutgoingFlows().get(0);
        for (FlowElement element : elements) {
            if (element.getId().equals(sequenceFlow.getTargetRef())) {
                if (element instanceof UserTask) {
                    return element;
                } else {
                    throw new IllegalArgumentException("流程设计错误，开始节点后只能是用户任务节点");
                }
            }
        }

        return null;
    }

    /**
     * 获取运行实例的下一个节点.
     *
     * @param procInstId ACT_RU_TASK.PROC_INST_ID_
     */
    @RequestMapping(value = "/execution/getNextNode/{procInstId}", method = RequestMethod.GET)
    public Object getNextNode(@PathVariable String procInstId) {
        // 当前执行节点id
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId)
                .singleResult();
        String currActId = pi.getActivityId();
        ProcessDefinitionEntity dfe = (ProcessDefinitionEntity)
                ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(pi.getProcessDefinitionId());
        // 获取所有节点
        List<ActivityImpl> activitiList = dfe.getActivities();
        // 判断出当前流程所处节点，根据路径获得下一个节点实例
        for (ActivityImpl activityImpl : activitiList) {
            if (activityImpl.getId().equals(currActId)) {
                // 获取下一个节点
                List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();

                PvmActivity pvmActivity = pvmTransitions.get(0).getDestination();
                String type = pvmActivity.getProperty("type").toString();
                if ("userTask".equals(type)) {
                    // 用户任务节点
                    return pvmActivity.getId()  ;
                } else if ("endEvent".equals(type)) {
                    // 结束
                    return "end";
                } else if ("exclusiveGateway".equals(type)) {
                    //网关
                    try {
                        TaskDefinition taskInfo = activitiHelper.getNextTaskInfo(procInstId);
                        return taskInfo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new IllegalArgumentException("流程设计错误，无法处理的节点");
                }
                break;
            }
        }
        return null;
    }


    /**
     * 获取流程图片.
     *
     * @param id       实例ID
     * @param response
     */
    @RequestMapping(value = "/execution/getHighlightImg/{id}", method = RequestMethod.GET)
    public void getHighlightImg(@PathVariable String id, HttpServletResponse response) {
        Map<String, Object> result = getInputStream(id);
        InputStream inputStream = (InputStream) result.get("inputStream");
        String picName = (String) result.get("name");

        OutputStream o;
        try {
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(picName, "UTF-8"));
            o = response.getOutputStream();
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                o.write(buf, 0, bytesRead);
                o.flush();
            }
            inputStream.close();
            o.close();
            response.flushBuffer();
        } catch (IOException e) {
            log.error(e.toString());
            throw new IllegalArgumentException("读取流程图片失败");
        }
    }

    /**
     * 输出图像.
     *
     * @param id       实例ID
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(
            value = "/execution/getActivitiImg/{id}",
            method = RequestMethod.GET,
            produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] getActivitiImg(@PathVariable String id, HttpServletResponse response)
            throws IOException {

        InputStream inputStream = (InputStream) getInputStream(id).get("inputStream");
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes, 0, inputStream.available());
        return bytes;
    }

    private Map<String, Object> getInputStream(String id) {
        InputStream inputStream;
        ProcessInstance pi;
        String picName;
        // 查询历史
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(id).singleResult();
        if (hpi != null && hpi.getEndTime() != null) {
            // 已经结束流程获取原图
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(hpi.getProcessDefinitionId()).singleResult();
            inputStream = repositoryService
                    .getResourceAsStream(pd.getDeploymentId(), pd.getDiagramResourceName());
            picName = pd.getDiagramResourceName();
        } else {
            pi = runtimeService.createProcessInstanceQuery().processInstanceId(id).singleResult();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
            List<String> highLightedActivities = new ArrayList<String>();
            // 高亮任务节点
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(id).list();
            for (Task task : tasks) {
                highLightedActivities.add(task.getTaskDefinitionKey());
            }

            List<String> highLightedFlows = new ArrayList<String>();
            ProcessDiagramGenerator diagramGenerator = processEngineConfiguration
                    .getProcessDiagramGenerator();
            inputStream = diagramGenerator
                    .generateDiagram(bpmnModel, "png", highLightedActivities, highLightedFlows,
                            properties.getActivityFontName(), properties.getLabelFontName(),
                            properties.getLabelFontName(), null, 1.0);
            picName = pi.getName() + ".png";
        }
        Map<String, Object> result = new HashMap<>();
        result.put("name", picName);
        result.put("inputStream", inputStream);
        return result;
    }


    /**
     * 删除任务.
     */
    @RequestMapping(value = "/execution/delete/{ids}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable String[] ids, @RequestParam(required = false) String reason) {

        if (StringUtils.isBlank(reason)) {
            reason = "";
        }
        for (String id : ids) {
            taskService.deleteTask(id, reason);
        }
        return "success";
    }

    /**
     * 删除任务历史.
     */
    @RequestMapping(value = "/execution/deleteHistoric/{ids}", method = RequestMethod.DELETE)
    public Object deleteHistoric(@PathVariable String[] ids) {

        for (String id : ids) {
            historyService.deleteHistoricTaskInstance(id);
        }
        return "success";
    }

    /**
     * 通过id删除运行中的实例.
     */
    @RequestMapping(value = "/execution/delInsByIds/{ids}", method = RequestMethod.GET)
    public Object delInsByIds(@PathVariable String[] ids,
                              @RequestParam(required = false) String reason) {

        if (StringUtils.isBlank(reason)) {
            reason = "";
        }
        for (String id : ids) {
            // 关联业务状态结束
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(id)
                    .singleResult();
            runtimeService.deleteProcessInstance(id, reason);
        }
        return "success";
    }


    /**
     * 当前任务列表.
     */
    @RequestMapping(value = "/task/list", method = RequestMethod.GET)
    public Object tasks() {
        List<Task> list =
                taskService.createTaskQuery()
                        .orderByExecutionId()
                        .desc()
                        .list();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Task item : list) {
            log.info("task.id={},proc_inst_id={},proc_def_id={}", item.getId(),
                    item.getProcessInstanceId(), item.getProcessDefinitionId());
            result.add(ImmutableMap.of(
                    "id", item.getId(),
                    "procInstId", item.getProcessInstanceId(),
                    "procDefId", item.getProcessDefinitionId()
            ));
        }

        return result;
    }

    /**
     * 历史记录列表.
     */
    @RequestMapping(value = "/history/finished-list", method = RequestMethod.GET)
    public List<HistoricTaskInstance> historyList(org.springframework.ui.Model model) {
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .finished().orderByTaskCreateTime().desc().list();
        return list;
    }


}
