package com.lind.avtiviti.event;

import com.lind.avtiviti.Constant;
import com.lind.avtiviti.config.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 会签开始
 * 注意TaskListener里不能直接用@Autowired这些注解.
 * activiti配置事件：delegateExpression="${mangerSignCreateEvent}"
 */
@Service
@Transactional
@Slf4j
public class MangerSignCreateEvent implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("会签审批开始,委托taskInsId:{},taskId:{}", delegateTask.getProcessInstanceId(), delegateTask.getId());
        // 会签节点的人数，这是由审核开始时为其赋的值
        List<String> countersignLeaders= (List<String>)delegateTask.getVariable(Constant.countersignLeaders);

        // 初始化变量
        delegateTask.setVariable(Constant.allTaskCount, countersignLeaders.size());
       // delegateTask.setVariable(Constant.countersignLeaders, countersignLeaders);
        delegateTask.setVariable(Constant.completeCount, 0);
        delegateTask.setVariable(Constant.signResult, "");
    }

}
