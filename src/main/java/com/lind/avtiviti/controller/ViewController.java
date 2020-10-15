package com.lind.avtiviti.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.lind.avtiviti.config.ActivitiConfig;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

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

    private static int longCompare(Date obj1, Date obj2) {
        return obj1.compareTo(obj2);
    }

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

    @GetMapping("deployByFile")
    public String deployByFile() {
        return "/view/model-upload";
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

        response.sendRedirect("/view/model/list");
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
            if (!processDefinition.isSuspended()) {
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
            List<Task> tasks =
                    taskService.createTaskQuery()
                            .active()
                            .processInstanceId(item.getId()).list();//并行网关可能是多条任务
            for (Task task : tasks) {
                String assignee = task.getAssignee() == null ? "" : task.getAssignee();
                result.add(new ImmutableMap.Builder<String, Object>()
                        .put("id", item.getId())
                        .put("proDefId", item.getProcessDefinitionId())
                        .put("isSuspended", item.isSuspended())
                        .put("taskId", task.getId())
                        .put("taskName", task.getName())
                        .put("time", task.getCreateTime())
                        .put("assignee", assignee)
                        .build());
            }
        }
        result = result.stream().sorted((i, j) -> longCompare((Date) j.get("time"), (Date) i.get("time"))).collect(Collectors.toList());

        model.addAttribute("result", result);
        return "view/execution-list";
    }

    /**
     * 已完成的历史记录列表.
     */
    @RequestMapping(value = "/history/finished-list", method = RequestMethod.GET)
    public String historyList(Model model) {
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .finished().orderByTaskCreateTime().desc().list();
        model.addAttribute("result", list);
        return "view/history-list";
    }

    /**
     * 通过流程定义id获取流程节点.
     *
     * @param procDefId 流程定义ID
     */
    @RequestMapping(value = "/deployment/node-list/{procDefId}", method = RequestMethod.GET)
    public String getProcessNode(@PathVariable String procDefId,Model model) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(procDefId);
        List<Process> processes = bpmnModel.getProcesses();
        model.addAttribute("result",processes.get(0));
       return "view/node-list";
    }
}
