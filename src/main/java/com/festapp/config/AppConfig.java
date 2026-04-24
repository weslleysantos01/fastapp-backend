package com.festapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * RestTemplate como bean singleton — evita criar novo objeto a cada request.
     * Injetado via @Autowired onde necessário.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}