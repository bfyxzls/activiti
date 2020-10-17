package com.lind.avtiviti.event;

import com.lind.avtiviti.config.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
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
public class AssignedEventListener implements org.activiti.engine.delegate.event.ActivitiEventListener {

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
            log.info("统一为UserTask的assignee赋值,processDefinitionId:{},processInstanceId:{}", event.getProcessDefinitionId(), event.getProcessInstanceId());
            RepositoryService repositoryService = SpringUtil.getObject(RepositoryService.class);
            BpmnModel bpmnModel = repositoryService.getBpmnModel(event.getProcessDefinitionId());
            String flowId = ((TaskEntity) taskEntity).getTaskDefinitionKey();
            UserTask flowElement = (UserTask) bpmnModel.getMainProcess().getFlowElement(flowId);

            if (flowElement != null) {
                ((TaskEntity) taskEntity).setAssignee(flowElement.getAssignee());
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
