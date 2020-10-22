package com.lind.avtiviti.event;

import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

/**
 * 所有任务在建立时出发的事件,在流程实例启动时添加的.
 * 事件注册：runtimeService.addEventListener(assignedEvent,ActivitiEventType.TASK_CREATED);
 * TaskListener事件完成后,执行ActivitiEventListener
 */
@Slf4j
@Component
@Transactional
public class NodeAssignedEventListener implements org.activiti.engine.delegate.event.ActivitiEventListener {
    @Autowired
    RepositoryService repositoryService;

    @Override
    public void onEvent(ActivitiEvent event) {
        Object taskEntity = null;
        if (event instanceof ActivitiEntityEventImpl) {
            ActivitiEntityEventImpl eventImpl = (ActivitiEntityEventImpl) event;
            taskEntity = eventImpl.getEntity();
        } else if (event instanceof ActivitiEntityEvent) {
            ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
            taskEntity = entityEvent.getEntity();
        } else {
            log.info("activiti event type not support!");
        }

        if (taskEntity != null && taskEntity instanceof TaskEntity) {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(event.getProcessDefinitionId());
            String flowId = ((TaskEntity) taskEntity).getTaskDefinitionKey();
            UserTask flowElement = (UserTask) bpmnModel.getMainProcess().getFlowElement(flowId);

            if (flowElement != null) {
                log.info("NodeAssignedEventListener...");
                Process process = bpmnModel.getMainProcess(); //获取主流程的，不考虑子流程
                UserTask flowElementUpdate = (UserTask) process.getFlowElement(flowElement.getId());
                flowElementUpdate.setAssignee(flowElement.getAssignee());
                process.removeFlowElement(flowElement.getId());
                process.addFlowElement(flowElement);
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
