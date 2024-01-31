package com.example.KinopoiskScraperV2.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KinopoiskScraperV2ApplicationConfig {

    @Value("${kinopoisk.apiKey}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}
