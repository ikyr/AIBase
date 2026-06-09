package com.datang.aibase.common.config;

import com.datang.aibase.common.interceptor.SnowflakeAutoFillInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(SqlSessionFactory.class)
public class MyBatisConfig {

    @Bean
    public SnowflakeAutoFillInterceptor snowflakeAutoFillInterceptor() {
        return new SnowflakeAutoFillInterceptor();
    }

    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer(SnowflakeAutoFillInterceptor interceptor) {
        return configuration -> configuration.addInterceptor(interceptor);
    }
}
