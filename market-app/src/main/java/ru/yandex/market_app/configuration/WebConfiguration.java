package ru.yandex.market_app.configuration;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import ru.yandex.client.ApiClient;
import ru.yandex.client.api.BalanceApi;
import ru.yandex.client.api.PaymentApi;
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
    public DSLContext dslContext(DataSource dataSource) {
        return DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Bean
    public ApiClient paymentApiClient(WebClient paymentWebClient) {
        ApiClient apiClient = new ApiClient(paymentWebClient);
        apiClient.setBasePath(property.getPaymentServiceHost());
        return apiClient;
    }

    @Bean
    public PaymentApi paymentApi(ApiClient apiClient) {
        return new PaymentApi(apiClient);
    }

    @Bean
    public BalanceApi balanceApi(ApiClient apiClient) {
        return new BalanceApi(apiClient);
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
            .baseUrl(property.getPaymentServiceHost())
            .build();
    }
}
