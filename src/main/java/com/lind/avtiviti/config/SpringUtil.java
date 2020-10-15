package com.lind.avtiviti.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtil implements ApplicationContextAware {
    /**
     * 当前IOC
     */
    private static ApplicationContext applicationContext;

    /**
     * 从当前IOC获取bean
     */
    public static <T> T getObject(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static void showClass() {
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
        }
    }

    /**
     * 设置applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}