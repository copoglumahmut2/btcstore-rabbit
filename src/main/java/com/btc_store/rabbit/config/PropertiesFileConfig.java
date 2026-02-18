package com.btc_store.rabbit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:rabbit-${spring.profiles.active}.properties")
@PropertySource("classpath:log-${spring.profiles.active}.properties")
public class PropertiesFileConfig {

}
