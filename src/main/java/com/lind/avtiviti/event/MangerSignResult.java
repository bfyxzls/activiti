package com.lind.avtiviti.event;

import com.lind.avtiviti.Constant;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * 会签完成
 */
@Service
@Transactional
@Slf4j
public class MangerSignResult implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("会签审批启动");
        if (delegateTask.getVariable(Constant.complete) != null && delegateTask.getVariable(Constant.allTask) != null) {
            Integer complete = Integer.parseInt(delegateTask.getVariable(Constant.complete).toString());
            Integer allTask = Integer.parseInt(delegateTask.getVariable(Constant.allTask).toString());
            delegateTask.setVariable(Constant.complete, complete + 1);
            if (((complete + 1) / allTask) == 1) {//说明已经全部审批了
                log.info("会签结束");
            }
        }
    }
}
