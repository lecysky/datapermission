package com.lecy.config;

import com.lecy.service.impl.PermissionInterceptorImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(PermissionInterceptorImpl.class)
public class PermissionAutoConfig {

    @Bean
    PermissionInterceptorImpl permissionInterceptorImpl() {
        return new PermissionInterceptorImpl();
    }
}
