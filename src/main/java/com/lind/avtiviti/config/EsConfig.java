package com.lind.avtiviti.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class EsConfig {
    @Bean
    public User esBlog() {
        return new User("test", "test", "test", "hello");
    }
}
