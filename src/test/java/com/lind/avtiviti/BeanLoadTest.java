package com.lind.avtiviti;

import com.lind.avtiviti.config.User;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BeanLoadTest {
    static Logger logger= LoggerFactory.getLogger("BeanLoadTest");
    @Test
    public void loadEsBean() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.scan("com.lind.avtiviti");
        applicationContext.refresh();
        User esBlog = (User) applicationContext.getBean("esBlog");
        logger.info("info:{}",esBlog);

    }
}
