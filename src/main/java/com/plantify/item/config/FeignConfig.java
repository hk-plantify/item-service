package com.plantify.item.config;

import com.plantify.item.global.logging.FeignMdcInterceptor;
import com.plantify.item.global.util.FeignAuthInterceptor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor feignMdcInterceptor() {
        return new FeignMdcInterceptor();
    }

    @Bean
    public RequestInterceptor feignAuthInterceptor() {
        return new FeignAuthInterceptor();
    }
}

