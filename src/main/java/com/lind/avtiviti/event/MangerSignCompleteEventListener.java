package com.lind.avtiviti.event;

import com.lind.avtiviti.Constant;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

/**
 * 会签完成
 * 解决ioc注入问题，在activiti配置时使用代理表达式${mangerSignCompleteEventListener}
 */
@Component
@Transactional
@Slf4j
public class MangerSignCompleteEventListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("会签审批启动");
        if (delegateTask.getVariable(Constant.completeCount) != null && delegateTask.getVariable(Constant.allTaskCount) != null) {
            Integer complete = Integer.parseInt(delegateTask.getVariable(Constant.completeCount).toString());
            Integer allTask = Integer.parseInt(delegateTask.getVariable(Constant.allTaskCount).toString());
            delegateTask.setVariable(Constant.completeCount, complete + 1);
            if (((complete + 1) / allTask) == 1) {//说明已经全部审批了
                log.info("会签结束");
            }
        }
    }
}
