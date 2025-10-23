package com.postgresql.StudentMarket.Config;

import com.postgresql.StudentMarket.infra.LastSeenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LastSeenInterceptor lastSeenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(lastSeenInterceptor)
                .addPathPatterns("/**"); // theo dõi mọi request
    }
}
