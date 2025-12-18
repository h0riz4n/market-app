package ru.yandex.market_app.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import ru.yandex.market_app.property.MarketAppProperty;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MarketAppProperty.class)
public class WebConfiguration implements WebFluxConfigurer {
    
    private final MarketAppProperty property;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(property.getCorsOrigins()) 
            .allowedMethods("*")
            .allowedHeaders("*");    
    }

    @Bean
    public PaymentApi paymentApi(WebClient.Builder builder) {
        // Указываем базовый URL API
        ApiClient apiClient = new ApiClient(
            builder.baseUrl("http://localhost:8080").build()
        );
        return new PaymentApi(apiClient);
    }
}
