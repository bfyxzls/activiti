package com.lind.avtiviti.event;

import com.lind.avtiviti.Constant;
import com.lind.avtiviti.config.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * 会签开始
 * 注意TaskListener里不能直接用@Autowired这些注解.
 */
@Service
@Transactional
@Slf4j
public class MangerSignStart implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("会签审批开始,委托taskInsId:{},taskId:{}", delegateTask.getProcessInstanceId(), delegateTask.getId());
        TaskService taskService = SpringUtil.getObject(TaskService.class);
        // 为当前task赋assignee
        List<String> countersignLeaders = (List<String>) delegateTask.getVariable("countersignLeaders");
        // 有的节点可以为多人点击
        delegateTask.setAssignee(String.join(",", countersignLeaders));
        // 初始化变量
        delegateTask.setVariable(Constant.complete, 0);
        delegateTask.setVariable(Constant.allTask, countersignLeaders.size());
        delegateTask.setVariable(Constant.signResult, "");
    }

}
