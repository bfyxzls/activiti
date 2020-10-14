package com.lind.avtiviti.event;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;

public class ActivitiMessageListener implements ActivitiEventListener {

    public boolean isFailOnException() {
        return false;
    }

    public void onEvent(ActivitiEvent event) {
        switch (event.getType()) {
            case ACTIVITY_COMPENSATE:
                // 一个节点将要被补偿。事件包含了将要执行补偿的节点id。
                break;
            case ACTIVITY_COMPLETED:
                // 一个节点成功结束
                break;
            case ACTIVITY_ERROR_RECEIVED:
                // 一个节点收到了一个错误事件。在节点实际处理错误之前触发。 事件的activityId对应着处理错误的节点。 这个事件后续会是ACTIVITY_SIGNALLED或ACTIVITY_COMPLETE， 如果错误发送成功的话。
                break;
            case ACTIVITY_MESSAGE_RECEIVED:
                // 一个节点收到了一个消息。在节点收到消息之前触发。收到后，会触发ACTIVITY_SIGNAL或ACTIVITY_STARTED，这会根据节点的类型（边界事件，事件子流程开始事件）
                break;
            case ACTIVITY_SIGNALED:
                // 一个节点收到了一个信号
                break;
            case ACTIVITY_STARTED:
                // 一个节点开始执行
                break;
            case CUSTOM:
                break;
            case ENGINE_CLOSED:
                // 监听器监听的流程引擎已经关闭，不再接受API调用。
                break;
            case ENGINE_CREATED:
                // 监听器监听的流程引擎已经创建完毕，并准备好接受API调用。
                break;
            case ENTITY_ACTIVATED:
                // 激活了已存在的实体，实体包含在事件中。会被ProcessDefinitions, ProcessInstances 和 Tasks抛出。
                break;
            case ENTITY_CREATED:
                // 创建了一个新实体。实体包含在事件中。
                break;
            case ENTITY_DELETED:
                // 删除了已存在的实体。实体包含在事件中
                break;
            case ENTITY_INITIALIZED:
                // 创建了一个新实体，初始化也完成了。如果这个实体的创建会包含子实体的创建，这个事件会在子实体都创建/初始化完成后被触发，这是与ENTITY_CREATED的区别。
                break;
            case ENTITY_SUSPENDED:
                // 暂停了已存在的实体。实体包含在事件中。会被ProcessDefinitions, ProcessInstances 和 Tasks抛出。
                break;
            case ENTITY_UPDATED:
                // 更新了已存在的实体。实体包含在事件中。
                break;
            case JOB_EXECUTION_FAILURE:
                // 作业执行失败。作业和异常信息包含在事件中。
                break;
            case JOB_EXECUTION_SUCCESS:
                // 作业执行成功。job包含在事件中。
                break;
            case JOB_RETRIES_DECREMENTED:
                // 因为作业执行失败，导致重试次数减少。作业包含在事件中。
                break;
            case MEMBERSHIPS_DELETED:
                // 所有成员被从一个组中删除。在成员删除之前触发这个事件，所以他们都是可以访问的。 因为性能方面的考虑，不会为每个成员触发单独的MEMBERSHIP_DELETED事件。
                break;
            case MEMBERSHIP_CREATED:
                // 用户被添加到一个组里。事件包含了用户和组的id。
                break;
            case MEMBERSHIP_DELETED:
                // 用户被从一个组中删除。事件包含了用户和组的id。
                break;
            case TASK_ASSIGNED:
                // 任务被分配给了一个人员。事件包含任务。
                break;
            case TASK_COMPLETED:
                // 任务被完成了。它会在ENTITY_DELETE事件之前触发。当任务是流程一部分时，事件会在流程继续运行之前， 后续事件将是ACTIVITY_COMPLETE，对应着完成任务的节点。
                break;
            case TIMER_FIRED:
                // 触发了定时器。job包含在事件中。
                break;
            case UNCAUGHT_BPMN_ERROR:
                break;
            case VARIABLE_CREATED:
                break;
            case VARIABLE_DELETED:
                break;
            case VARIABLE_UPDATED:
                break;
            default:
                break;
        }
    }

}