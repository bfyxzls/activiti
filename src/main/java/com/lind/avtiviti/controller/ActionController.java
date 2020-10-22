package com.lind.avtiviti.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lind.avtiviti.Constant;
import com.lind.avtiviti.util.ActivitiHelper;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
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
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
@Slf4j
public class ActionController {
    public static final String FATHER_SPLIT = "-";
    public static final String SON_SPLIT = "_";
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
    ActivitiHelper activitiHelper;

    /**
     * 建立页面，同时也保存.
     */
    @GetMapping("/model/create")
    public void createModel(HttpServletRequest request, HttpServletResponse response) {
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.putPOJO("stencilset", stencilSetNode);

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, Constant.modelName);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, Constant.description);
            Model modelData = repositoryService.newModel();
            modelData.setMetaInfo(modelObjectNode.toString());
            modelData.setName(Constant.modelName);
            modelData.setKey(Constant.modelKey);

            //保存模型
            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(),
                    editorNode.toString().getBytes(StandardCharsets.UTF_8));
            response
                    .sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + modelData.getId());
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @GetMapping("/model/delete")
    public void delModel(String modelId, HttpServletResponse response) throws IOException {
        repositoryService.deleteModel(modelId);
        response.sendRedirect("/view/model/list");
    }

    /**
     * 模型部署成为流程.
     */
    @RequestMapping(value = "/model/deploy/{id}", method = RequestMethod.GET)
    public void deploy(@PathVariable String id, HttpServletResponse response) throws IOException {

        // 获取模型
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            throw new IllegalArgumentException("模型数据为空，请先成功设计流程并保存");
        }

        try {
            JsonNode modelNode = new ObjectMapper().readTree(bytes);
            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            if (model.getProcesses().size() == 0) {
                throw new IllegalArgumentException("模型不符要求，请至少设计一条主线流程");
            }
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
            // 部署发布模型流程
            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment().name(modelData.getName())
                    .addString(processName, new String(bpmnBytes, StandardCharsets.UTF_8)).deploy();

            // 设置流程分类 保存扩展流程至数据库
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId()).list();

            for (ProcessDefinition pd : list) {
                log.info(pd.getName());
            }
        } catch (Exception e) {
            log.error(e.toString());
            throw new IllegalArgumentException(e.getMessage());
        }

        response.sendRedirect("/view/deployment/list");
    }

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
    ) throws Exception {

        if (StringUtils.isBlank(comment)) {
            comment = "";
        }
        taskService.addComment(taskId, procInstId, comment);
        if (assignee != null) {
            taskService.setAssignee(taskId, assignee);
        }
        Map map = new HashMap();
        if (StringUtils.isNoneBlank(params)) {
            String[] keys = StringUtils.split(params, FATHER_SPLIT);
            for (String val : keys) {
                String[] sonVal = StringUtils.split(val, SON_SPLIT);
                if (sonVal[0].equals(Constant.meeting)) {
                    //会签操作,需要算出会签人员数,人员分配在AssignedEventListener事件里完成
                    ProcessInstance pi = runtimeService.createProcessInstanceQuery() // 根据流程实例id获取流程实例
                            .processInstanceId(procInstId)
                            .singleResult();
                    BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
                    TaskDefinition taskDefinition = activitiHelper.getNextTaskInfo(procInstId);
                    UserTask userTask = (UserTask) bpmnModel.getMainProcess().getFlowElement(taskDefinition.getKey());
                    if (StringUtils.isBlank(userTask.getAssignee())) {
                        throw new ActivitiException("需要为节点指定人员");
                    }
                    List<String> assignees = Arrays.asList(StringUtils.split(userTask.getAssignee(), ","));
                    map.put(Constant.meeting, assignees);
                } else {
                    //普通操作
                    map.put(sonVal[0], sonVal[1]);
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
