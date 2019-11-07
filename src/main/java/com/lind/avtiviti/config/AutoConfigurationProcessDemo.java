package com.lind.avtiviti.config;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.FormService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.assertj.core.util.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Configuration
@Slf4j
public class AutoConfigurationProcessDemo {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired    private TaskService taskService;
    @Autowired    private FormService formService;
    /**
     * 命令行执行
     *
     * @return
     */
    @Bean
    public CommandLineRunner commandLineRunner() {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                logger.info("启动我们的程序");
                //启动运行流程
                ProcessInstance processInstance = startProcessInstance("second_approve");
                //处理流程任务
                processTask(processInstance);
                logger.info("结束我们的程序");
            }
        };
    }    /**
     * 处理流程任务
     *
     * @param processInstance
     * @throws ParseException
     */
    private void processTask(ProcessInstance processInstance) throws ParseException {
        Scanner scanner = new Scanner(System.in);
        while (processInstance != null && !processInstance.isEnded()) {
            List<Task> list = taskService.createTaskQuery().list();
            logger.info("待处理任务数量 [{}]", list.size());
            for (Task task : list) {
                logger.info("待处理任务 [{}]", task.getName());
                Map<String, Object> variables = buildVariablesByScanner(scanner, task);
                taskService.complete(task.getId(), variables);
                processInstance = runtimeService
                        .createProcessInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .singleResult();
            }
        }
        scanner.close();
    }    /**
     * 从控制台获取变量
     *
     * @param scanner
     * @param task
     * @return
     * @throws ParseException
     */
    private Map<String, Object> buildVariablesByScanner(Scanner scanner, Task task) throws ParseException {
        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        List<FormProperty> formProperties = taskFormData.getFormProperties();
        return buildVariablesByScanner(scanner, formProperties);
    }

    public static Map<String, Object> buildVariablesByScanner(Scanner scanner, List<FormProperty> formProperties) throws ParseException {
        Map<String, Object> variables = Maps.newHashMap();
        for (FormProperty property : formProperties) {
            String line = null;
            if (StringFormType.class.isInstance(property.getType())) {
                logger.info("请输入 {} ？", property.getName());
                line = scanner.nextLine();
                variables.put(property.getId(), line);
            } else if (DateFormType.class.isInstance(property.getType())) {
                logger.info("请输入 {} ？ 格式 （yyyy-MM-dd）", property.getName());
                line = scanner.nextLine();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dateFormat.parse(line);
                variables.put(property.getId(), date);
            } else {
                logger.info("类型暂不支持 {}", property.getType());
            }
            logger.info("您输入的内容是 [{}]", line);

        }
        return variables;
    }    /**
     * 获取流程实例
     *
     * @param processDefinitionId
     * @return
     */
    private ProcessInstance startProcessInstance(String processDefinitionId) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionId);        logger.info("启动流程 [{}]", processInstance.getProcessDefinitionKey());        return processInstance;
    }

}