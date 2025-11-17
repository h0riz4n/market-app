package ru.yandex.market_app.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;
import ru.yandex.market_app.property.MarketAppProperty;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MarketAppProperty.class)
public class WebConfiguration implements WebMvcConfigurer {
    
    private final MarketAppProperty property;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(property.getCorsOrigins()) 
            .allowedMethods("*")
            .allowedHeaders("*");    
    }
}
