package com.lind.avtiviti.event;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * springboot程序启动后要执行的事件.
 */
@Component
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {
    private static Logger logger = LoggerFactory.getLogger(ApplicationReadyEventListener.class);
    @Autowired
    AssignedEventListener assignedEventListener;
    @Autowired
    LoggerEventListener loggerEventListener;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    NodeAssignedEventListener nodeAssignedEventListener;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 添加TASK_CREATED触发时订阅的事件
        runtimeService.addEventListener(assignedEventListener, ActivitiEventType.TASK_CREATED);
        // 添加TASK_COMPLETED触发时订阅的事件
        runtimeService.addEventListener(loggerEventListener, ActivitiEventType.TASK_COMPLETED);
        runtimeService.addEventListener(nodeAssignedEventListener, ActivitiEventType.TASK_COMPLETED);

    }
}