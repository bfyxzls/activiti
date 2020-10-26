package com.lind.avtiviti.util;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.javax.el.ExpressionFactory;
import org.activiti.engine.impl.javax.el.ValueExpression;
import org.activiti.engine.impl.juel.ExpressionFactoryImpl;
import org.activiti.engine.impl.juel.SimpleContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * activiti工具类.
 */
@Component
public class ActivitiHelper {
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    RepositoryService repositoryService;

    /**
     * 下一个任务节点信息,
     * <p>
     * 如果下一个节点为用户任务则直接返回,
     * <p>
     * 如果下一个节点为排他网关, 获取排他网关Id信息, 根据排他网关Id信息和execution获取流程实例排他网关Id为key的变量值,
     * 根据变量值分别执行排他网关后线路中的el表达式, 并找到el表达式通过的线路后的用户任务
     *
     * @param activityImpl      流程节点信息
     * @param activityId        当前流程节点Id信息
     * @param elString          排他网关顺序流线段判断条件
     * @param processInstanceId 流程实例Id信息
     * @return
     */
    public List<TaskDefinition> nextTaskDefinitionList(ActivityImpl activityImpl, String activityId, String elString, String processInstanceId) {

        PvmActivity ac = null;
        List<TaskDefinition> taskDefinitions = new ArrayList<>();
        Object s = null;
        TaskDefinition taskDefinition = null;
        // 如果遍历节点为用户任务并且节点不是当前节点信息
        if ("userTask".equals(activityImpl.getProperty("type")) && !activityId.equals(activityImpl.getId())) {
            // 获取该节点下一个节点信息

            if (activityImpl.getActivityBehavior() instanceof ParallelMultiInstanceBehavior) {
                ParallelMultiInstanceBehavior activityBehavior = (ParallelMultiInstanceBehavior) activityImpl.getActivityBehavior();
                UserTaskActivityBehavior userTaskActivityBehavior = (UserTaskActivityBehavior) activityBehavior.getInnerActivityBehavior();
                taskDefinition = userTaskActivityBehavior.getTaskDefinition();
            } else if (activityImpl.getActivityBehavior() instanceof UserTaskActivityBehavior) {
                taskDefinition = ((UserTaskActivityBehavior) activityImpl.getActivityBehavior())
                        .getTaskDefinition();
            }
            taskDefinitions.add(taskDefinition);
            return taskDefinitions;
        } else {
            // 获取节点所有流向线路信息
            List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
            List<PvmTransition> outTransitionsTemp = null;
            for (PvmTransition tr : outTransitions) {
                ac = tr.getDestination(); // 获取线路的终点节点
                // 如果流向线路为排他网关
                if ("exclusiveGateway".equals(ac.getProperty("type"))) {
                    outTransitionsTemp = ac.getOutgoingTransitions();

                    // 如果网关路线判断条件为空信息
                    if (StringUtils.isEmpty(elString)) {
                        // 获取流程启动时设置的网关判断条件信息
                        elString = getGatewayCondition(ac.getId(), processInstanceId);
                    }

                    // 如果排他网关只有一条线路信息
                    if (outTransitionsTemp.size() == 1) {
                        return nextTaskDefinitionList((ActivityImpl) outTransitionsTemp.get(0).getDestination(), activityId,
                                elString, processInstanceId);
                    } else if (outTransitionsTemp.size() > 1) { // 如果排他网关有多条线路信息
                        for (PvmTransition tr1 : outTransitionsTemp) {
                            s = tr1.getProperty("conditionText"); // 获取排他网关线路判断条件信息
                            // 判断el表达式是否成立
                            if (isCondition(ac.getId(), StringUtils.trim(s.toString()), elString)) {
                                return nextTaskDefinitionList((ActivityImpl) tr1.getDestination(), activityId, elString,
                                        processInstanceId);
                            }
                        }
                    }
                } else if ("userTask".equals(ac.getProperty("type"))) {

                    ActivityImpl activity = (ActivityImpl) ac;
                    if (activity.getActivityBehavior() instanceof UserTaskActivityBehavior) {
                        taskDefinition = ((UserTaskActivityBehavior) activity.getActivityBehavior())
                                .getTaskDefinition();
                    } else if (activity.getActivityBehavior() instanceof ParallelMultiInstanceBehavior) {
                        ParallelMultiInstanceBehavior activityBehavior = (ParallelMultiInstanceBehavior) activity.getActivityBehavior();
                        UserTaskActivityBehavior userTaskActivityBehavior = (UserTaskActivityBehavior) activityBehavior.getInnerActivityBehavior();
                        taskDefinition = userTaskActivityBehavior.getTaskDefinition();
                    }
                    taskDefinitions.add(taskDefinition);
                    return taskDefinitions;
                } else if ("parallelGateway".equals(ac.getProperty("type"))) {
                    outTransitionsTemp = ac.getOutgoingTransitions();
                    if (outTransitionsTemp.size() == 1) {
                        return nextTaskDefinitionList((ActivityImpl) outTransitionsTemp.get(0).getDestination(), activityId,
                                elString, processInstanceId);
                    } else if (outTransitionsTemp.size() > 1) { // 如果排他网关有多条线路信息
                        for (PvmTransition tr1 : outTransitionsTemp) {
                            List<TaskDefinition> taskDefinitionList = nextTaskDefinitionList((ActivityImpl) tr1.getDestination(), activityId, elString,
                                    processInstanceId);
                            taskDefinitions.addAll(taskDefinitionList);
                        }
                        return taskDefinitions;
                    }
                }
            }
            return null;
        }
    }

    /**
     * 下一个任务.
     */
    public TaskDefinition getNextTaskInfo(String processInstanceId) throws Exception {

        ProcessDefinitionEntity processDefinitionEntity = null;

        String id = null;

        TaskDefinition task = null;

        //获取流程发布Id信息
        String definitionId = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();

        processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(definitionId);

        ExecutionEntity execution = (ExecutionEntity) runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        //当前流程节点Id信息
        String activitiId = execution.getActivityId();

        //获取流程所有节点信息
        List<ActivityImpl> activitiList = processDefinitionEntity.getActivities();

        //遍历所有节点信息
        for (ActivityImpl activityImpl : activitiList) {
            id = activityImpl.getId();
            if (activitiId.equals(id)) {
                //获取下一个节点信息
                task = nextTaskDefinition(activityImpl, activityImpl.getId(), null, processInstanceId);
                break;
            }
        }
        return task;
    }

    /**
     * 下一个任务节点信息.
     *
     * @param activityImpl      流程节点信息
     * @param activityId        当前流程节点Id信息
     * @param elString          排他网关顺序流线段判断条件
     * @param processInstanceId 流程实例Id信息
     */
    private TaskDefinition nextTaskDefinition(ActivityImpl activityImpl, String activityId,
                                              String elString, String processInstanceId) {

        PvmActivity ac = null;

        Object s = null;

        // 如果遍历节点为用户任务并且节点不是当前节点信息
        if ("userTask".equals(activityImpl.getProperty("type")) && !activityId
                .equals(activityImpl.getId())) {
            // 获取该节点下一个节点信息
            TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activityImpl
                    .getActivityBehavior()).getTaskDefinition();
            return taskDefinition;
        } else {
            // 获取节点所有流向线路信息
            List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
            List<PvmTransition> outTransitionsTemp = null;
            for (PvmTransition tr : outTransitions) {
                ac = tr.getDestination(); // 获取线路的终点节点
                // 如果流向线路为排他网关
                if ("exclusiveGateway".equals(ac.getProperty("type"))) {
                    outTransitionsTemp = ac.getOutgoingTransitions();

                    // 如果网关路线判断条件为空信息
                    if (StringUtils.isEmpty(elString)) {
                        // 获取流程启动时设置的网关判断条件信息
                        elString = getGatewayCondition(ac.getId(), processInstanceId);
                    }

                    // 如果排他网关只有一条线路信息
                    if (outTransitionsTemp.size() == 1) {
                        return nextTaskDefinition((ActivityImpl) outTransitionsTemp.get(0).getDestination(),
                                activityId, elString, processInstanceId);
                    } else if (outTransitionsTemp.size() > 1) { // 如果排他网关有多条线路信息
                        for (PvmTransition tr1 : outTransitionsTemp) {
                            s = tr1.getProperty("conditionText"); // 获取排他网关线路判断条件信息
                            // 判断el表达式是否成立
                            if (isCondition(ac.getId(), StringUtils.trim(s.toString()), elString)) {
                                return nextTaskDefinition((ActivityImpl) tr1.getDestination(), activityId, elString,
                                        processInstanceId);
                            }
                        }
                    }
                } else if ("userTask".equals(ac.getProperty("type"))) {
                    ActivityImpl activity = (ActivityImpl) ac;
                    ActivityBehavior activityBehavior = activity.getActivityBehavior();
                    // 多任务场景
                    if (activityBehavior instanceof MultiInstanceActivityBehavior) {
                        return ((UserTaskActivityBehavior) ((MultiInstanceActivityBehavior) activityBehavior).getInnerActivityBehavior()).getTaskDefinition();
                    }
                    return ((UserTaskActivityBehavior) activityBehavior)
                            .getTaskDefinition();
                }
            }
            return null;
        }
    }

    /**
     * 查询流程启动时设置排他网关判断条件信息.
     *
     * @param gatewayId         排他网关Id信息, 流程启动时设置网关路线判断条件key为网关Id信息
     * @param processInstanceId 流程实例Id信息
     */
    private String getGatewayCondition(String gatewayId, String processInstanceId) {
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstanceId)
                .singleResult();
        Object object = runtimeService.getVariable(execution.getId(), gatewayId);
        return object == null ? "" : object.toString();
    }

    /**
     * 根据key和value判断el表达式是否通过信息.
     *
     * @param key   el表达式key信息
     * @param el    el表达式信息
     * @param value el表达式传入值信息
     */
    private boolean isCondition(String key, String el, String value) {
        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        context.setVariable(key, factory.createValueExpression(value, String.class));
        ValueExpression e = factory.createValueExpression(context, el, boolean.class);
        return (Boolean) e.getValue(context);
    }

    /**
     * 是否为最后一个节点.
     *
     * @param procInstId
     * @return
     */
    public Boolean isEndNode(@PathVariable String procInstId) {
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
                if ("endEvent".equals(type)) {
                    // 结束
                    return true;
                }
            }
        }
        return false;
    }
}
