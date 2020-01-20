package com.lind.avtiviti.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(ActivitiConfig.ActivitiExtendProperties.class)
public class ActivitiConfig {

  @Autowired
  private ActivitiExtendProperties properties;

  /**
   * init.
   */
  @Bean
  public ProcessEngineConfigurationConfigurer processEngineConfigurationConfigurer() {

    ProcessEngineConfigurationConfigurer configurer = new ProcessEngineConfigurationConfigurer() {
      @Override
      public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {

        processEngineConfiguration.setActivityFontName(properties.getActivityFontName());
        processEngineConfiguration.setAnnotationFontName(properties.getActivityFontName());
        processEngineConfiguration.setLabelFontName(properties.getLabelFontName());
      }
    };

    return configurer;
  }

  @Data
  @ConfigurationProperties(prefix = "spring.activiti.font")
  public static class ActivitiExtendProperties {

    /**
     * 流程图字体配置.
     */
    private String activityFontName = "宋体";

    /**
     * 流程图字体配置.
     */
    private String labelFontName = "宋体";
  }
}

