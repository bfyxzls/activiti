package com.lind.avtiviti.event;

import com.lind.avtiviti.Constant;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * 会签开始
 * 注意TaskListener里在流程UI中使用类名注册时不能直接用@Autowired这些注解，需要使用代理表达式才可以用这些注解.
 * activiti配置事件代理表达式：${mangerSignCreateEventListener}
 */
@Service
@Transactional
@Slf4j
public class MangerSignCreateEventListener implements TaskListener {
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
