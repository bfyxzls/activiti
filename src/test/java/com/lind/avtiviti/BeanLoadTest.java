package com.lind.avtiviti;

import com.lind.avtiviti.config.User;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BeanLoadTest {
    @Test
    public void loadEsBean() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.scan("com.lind.avtiviti");
        applicationContext.refresh();
        User esBlog = (User) applicationContext.getBean("esBlog");
        System.out.println(esBlog);

    }
}
