package com.runtime.engine.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = "com.runtime.engine")
@EntityScan(basePackages = "com.runtime.engine")
@EnableJpaRepositories(basePackages = "com.runtime.engine.repo")
@EnableTransactionManagement
public class EngineAutoConfiguration {
}
