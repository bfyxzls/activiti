package com.lind.avtiviti.config;

import com.lind.avtiviti.es.EsBlog;
import com.lind.avtiviti.es.EsBlogRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class EsConfig {
    @Bean
    public User esBlog() {
        return new User("test", "test", "test","hello");
    }
}
