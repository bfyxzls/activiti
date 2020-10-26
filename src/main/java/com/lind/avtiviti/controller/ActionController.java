package com.lind.avtiviti.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.lind.avtiviti.Constant;
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
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

/**
 * activiti操作接口,前后分离时更灵活.
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
    @Autowired
    ActReNodeRepository actReNodeRepository;

    private static int longCompare(Date obj1, Date obj2) {
        return obj1.compareTo(obj2);
    }

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
    public void createInstance(@PathVariable String procDefId,
                               @RequestParam String title,
                               HttpServletResponse response) throws IOException {
        // 启动流程
        ProcessInstance pi = runtimeService.startProcessInstanceById(procDefId);
        // 设置流程实例名称
        runtimeService.setProcessInstanceName(pi.getId(), title);
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
    }

    /**
     * 假删除部署
     *
     * @param id
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/deployment/suspend/{id}", method = RequestMethod.GET)
    public void deploymentDel(@PathVariable String id, HttpServletResponse response) throws IOException {
        processEngine.getRepositoryService().suspendProcessDefinitionById(id);
    }

    /**
     * 流程实例的任务的审批，需要通过下一节点配置的组选择审核人
     *
     * @param procInstId 流程实例ID ACT_RU_TASK.PROC_INST_ID_
     * @param taskId     任务ID ACT_RU_TASK.ID_
     * @param assignee   前端传过来的分配人,为空表示自己
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
        Map map = new HashMap();
        map.put(Constant.assignee, assignee);
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
                    if (StringUtils.isBlank(userTask.getOwner())) {
                        throw new ActivitiException("需要为节点指定角色");
                    }
                    List<String> assignees = Arrays.asList(StringUtils.split(assignee, ","));
                    map.put(Constant.meeting, assignees);
                } else {
                    //普通操作
                    map.put(sonVal[0], sonVal[1]);
                }
            }
        }

        taskService.complete(taskId, map);
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
        if (comment == null) {
            comment = "";
        }
        Task taskEntity = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 获得当前任务的流程实例ID
        String processInstanceId = taskEntity.getProcessInstanceId();

        // 获得当前任务的流程定义ID
        String processDefinitionId = taskEntity.getProcessDefinitionId();
        // 当前任务key
        String currtaskDefKey = taskEntity.getTaskDefinitionKey();

        // 获得当前活动节点和驳回的目标节点"draft"
        ActivityImpl currActiviti = null;// 当前活动节点
        ActivityImpl destActiviti = null;// 驳回目标节点

        currActiviti = getActivityImpl(currtaskDefKey, processDefinitionId);
        destActiviti = getActivityImpl(destTaskKey, processDefinitionId);

        // 保存当前活动节点的流程流出参数
        List<PvmTransition> hisPvmTransitionList = new ArrayList<PvmTransition>(0);

        for (PvmTransition pvmTransition : currActiviti.getOutgoingTransitions()) {
            hisPvmTransitionList.add(pvmTransition);
        }
        // 清空当前活动节点的所有流出项

        currActiviti.getOutgoingTransitions().clear();
        // 为当前节点动态创建新的流出项

        TransitionImpl newTransitionImpl = currActiviti.createOutgoingTransition();
        // 为当前活动节点新的流出项指定为目标流程节点
        newTransitionImpl.setDestination(destActiviti);
        // 保存驳回意见
        taskEntity.setDescription(comment);// 设置驳回意见
        taskService.saveTask(taskEntity);

        /**
         * 注意：添加批注的时候，由于Activiti底层代码是使用： String userId =
         * Authentication.getAuthenticatedUserId(); CommentEntity comment = new
         * CommentEntity(); comment.setUserId(userId);
         * 所有需要从Session中获取当前登录人，作为该任务的办理人（审核人），对应act_hi_comment表中的User_ID的字段，
         * 不添加审核人，该字段为null
         * 所以要求，添加配置执行使用Authentication.setAuthenticatedUserId();添加当前任务的审核人
         */
        Authentication.setAuthenticatedUserId("1");
        taskService.addComment(taskId, processInstanceId, comment);

        // 设定驳回标志
        Map<String, Object> variables = new HashMap<String, Object>(0);
        //variables.put("outcome", outcome);
        // 执行当前任务驳回到目标任务draft
        taskEntity.setDescription("backFlow");// 设置驳回意见
        taskService.saveTask(taskEntity);
        //设置审批人为当前审批人，保证前台展示审批人为一个人
        taskService.setAssignee(taskId, "1");
        taskService.complete(taskEntity.getId(), variables);
        // 清除目标节点的新流入项
        destActiviti.getIncomingTransitions().remove(newTransitionImpl);
        // 清除原活动节点的临时流程项
        currActiviti.getOutgoingTransitions().clear();
        // 还原原活动节点流出项参数
        currActiviti.getOutgoingTransitions().addAll(hisPvmTransitionList);

    }

    /**
     * @return
     * @Description (通过任务key, 获取对应的节点信息)
     * @author feizhou
     * @Date 2018年3月28日下午1:53:29
     * @version 1.0.0
     */
    public ActivityImpl getActivityImpl(String destTaskKey, String processDefinitionId) {
        // 获得当前流程的定义模型
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processDefinitionId);

        // 获得当前流程定义模型的所有任务节点

        List<ActivityImpl> activitilist = processDefinition.getActivities();
        // 获得当前活动节点和驳回的目标节点"draft"
        ActivityImpl descActiviti = null;// 当前活动节点

        for (ActivityImpl activityImpl : activitilist) {
            // 获取节点对应的key
            String taskKey = activityImpl.getId();
            // 确定当前活动activiti节点
            if (destTaskKey.equals(taskKey)) {
                descActiviti = activityImpl;
                break;
            }
        }
        return descActiviti;
    }

    /**
     * 获取当前节点的下一节点信息，前台需要它返回的role，来获取role下面的用户.
     *
     * @param procInstId
     * @return
     */
    @RequestMapping(value = "/process/next-node/{procInstId}", method = RequestMethod.GET)
    public Object getNextNode(@PathVariable String procInstId) throws Exception {
        TaskDefinition taskDefinition = activitiHelper.getNextTaskInfo(procInstId);
        String definitionId = runtimeService.createProcessInstanceQuery()
                .processInstanceId(procInstId).singleResult().getProcessDefinitionId();

        if (null != taskDefinition) {
            ActReNode actReNode = actReNodeRepository.findByNodeIdAndProcessDefId(taskDefinition.getKey(), definitionId);
            if (actReNode == null) {
                throw new ActivitiException("请为节点" + taskDefinition.getKey() + "配置角色");
            }
            return ImmutableMap.of(
                    "end", false,
                    "id", taskDefinition.getKey(),
                    "name", taskDefinition.getNameExpression().getExpressionText(),
                    "role", actReNode.getRoleId());
        }
        return ImmutableMap.of(
                "end", true);
    }

    /**
     * 根据任务id查询已经执行的任务节点信息
     */
    public List<Map<String, String>> getRunNodes(String taskId) {
        // 获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
        List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(taskId)
                .activityType("userTask")   //用户任务
                .finished()       //已经执行的任务节点
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        List<Map<String, String>> list = new ArrayList<>();
        // 已执行的节点ID集合
        if (CollectionUtils.isNotEmpty(historicActivityInstanceList)) {
            Map<String, String> map = new LinkedHashMap<String, String>();
            for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
                if (!map.containsKey(historicActivityInstance.getActivityId())) {
                    map.put(historicActivityInstance.getActivityId(), historicActivityInstance.getActivityName());
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * 导出部署流程资源
     *
     * @param id
     * @param response
     */
    @RequestMapping(value = "/process/export", method = RequestMethod.GET)
    public void exportResource(@RequestParam String id,
                               HttpServletResponse response) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id).singleResult();
        String resourceName = pd.getResourceName();
        InputStream inputStream = repositoryService.getResourceAsStream(pd.getDeploymentId(),
                resourceName);

        try {
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(resourceName, "UTF-8"));
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
            response.flushBuffer();
        } catch (IOException e) {
            log.error(e.toString());
            throw new ActivitiException("导出部署流程资源失败");
        }
    }

    /**
     * 模模型列表.
     */
    @RequestMapping(value = "/model/list", method = RequestMethod.GET)
    public Object modelist(org.springframework.ui.Model model,
                           @RequestParam(required = false, defaultValue = "1") int pageindex,
                           @RequestParam(required = false, defaultValue = "10") int pagesize) {
        pageindex = (pageindex - 1) * pagesize;
        List<org.activiti.engine.repository.Model> list = processEngine.getRepositoryService().createModelQuery()
                .orderByCreateTime().desc()
                .listPage(pageindex, pagesize);

        return list;
    }

    /**
     * 通过文件部署流程.
     *
     * @param file
     */
    @PostMapping(value = "deployByFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void deployByFile(@RequestPart("file") MultipartFile file, HttpServletResponse response) throws IOException {
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            return;
        }
        try {
            InputStream fileInputStream = file.getInputStream();
            Deployment deployment;
            String extension = FilenameUtils.getExtension(fileName);
            String baseName = FilenameUtils.getBaseName(fileName);
            if ("zip".equals(extension) || "bar".equals(extension)) {
                ZipInputStream zip = new ZipInputStream(fileInputStream);
                deployment = repositoryService.createDeployment().name(baseName)
                        .addZipInputStream(zip).deploy();
            } else if ("png".equals(extension)) {
                deployment = repositoryService.createDeployment().name(baseName)
                        .addInputStream(fileName, fileInputStream).deploy();
            } else if (fileName.indexOf("bpmn20.xml") != -1) {
                deployment = repositoryService.createDeployment().name(baseName)
                        .addInputStream(fileName, fileInputStream).deploy();
            } else if ("bpmn".equals(extension)) {
                deployment = repositoryService.createDeployment().name(baseName)
                        .addInputStream(baseName + ".bpmn20.xml", fileInputStream).deploy();
            } else {
                throw new IllegalArgumentException("文件格式不支持");
            }
            ProcessDefinition processDefinition = processEngine.getRepositoryService()
                    .createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();
            convertToModel(processDefinition.getId(), response);

        } catch (Exception e) {
            log.error(e.toString());
        }
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
    public Object deployment(org.springframework.ui.Model model,
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
        return result;

    }

    /**
     * 当前运行中的流程实例列表，应该是启动了的流程（/execution/start/会出现的流程）.
     */
    @RequestMapping(value = "/execution/list", method = RequestMethod.GET)
    public Object execution(org.springframework.ui.Model model,
                            @RequestParam(required = false, defaultValue = "1") int pageindex,
                            @RequestParam(required = false, defaultValue = "10") int pagesize) {
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
            for (Task task : tasks) {

                String owner = task.getOwner() == null ? "" : task.getOwner();
                String assignee = task.getAssignee() == null ? "" : task.getAssignee();
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
                        .build());
            }
        }
        result = result.stream()
                .sorted((i, j) -> longCompare((Date) j.get("time"), (Date) i.get("time")))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * 历史流程列表.
     */
    @RequestMapping(value = "/history/list", method = RequestMethod.GET)
    public Object historyList(org.springframework.ui.Model model,
                              @RequestParam(required = false, defaultValue = "1") int pageindex,
                              @RequestParam(required = false, defaultValue = "10") int pagesize) {
        pageindex = (pageindex - 1) * pagesize;
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
                //     .finished()
                .orderByProcessInstanceStartTime()
                .desc()
                .listPage(pageindex, pagesize);

        return list;
    }

    /**
     * 已完成的历史记录列表.
     */
    @RequestMapping(value = "/task/list/{id}", method = RequestMethod.GET)
    public Object taskList(@PathVariable String id, org.springframework.ui.Model model) {
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(id)
                .finished()
                .orderByTaskCreateTime()
                .desc()
                .list();
        return list;
    }

    /**
     * 通过流程定义id获取流程节点.
     *
     * @param procDefId 流程定义ID
     */
    @RequestMapping(value = "/deployment/node-list/{procDefId}", method = RequestMethod.GET)
    public Object getProcessNode(@PathVariable String procDefId, org.springframework.ui.Model model) {
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
                    }
                    processNodeVos.add(node);
                }
            }
        }
        log.info("processNodeVos:{}", processNodeVos);
        return processNodeVos;
    }

    /**
     * 节点配置.
     *
     * @param procDefId
     * @param nodeId
     * @param assignee
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/deployment/node-save", method = RequestMethod.POST)
    @Transactional
    public void getProcessNode(@RequestParam String procDefId,
                               String[] nodeId,
                               String[] assignee,
                               HttpServletResponse response) throws IOException {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(procDefId);
        Process process = bpmnModel.getMainProcess(); //获取主流程的，不考虑子流程
        List<ActReNode> actReNodes = new ArrayList<>();
        for (int i = 0; i < nodeId.length; i++) {
            UserTask flowElement = (UserTask) process.getFlowElement(nodeId[i]);
            flowElement.setOwner(assignee[i]);
            process.setValues(flowElement);//数据只保存在内存里，需要添加节点分配数据表才能实现
            actReNodeRepository.removeByNodeIdAndProcessDefId(nodeId[i], procDefId);
            ActReNode actReNode = new ActReNode();
            actReNode.setId(UUID.randomUUID().toString());
            actReNode.setNodeId(nodeId[i]);
            actReNode.setRoleId(assignee[i]);
            actReNode.setProcessDefId(procDefId);
            actReNodeRepository.save(actReNode);
        }
    }
}
