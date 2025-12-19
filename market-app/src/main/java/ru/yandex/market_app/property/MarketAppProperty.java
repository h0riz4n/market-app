package ru.yandex.market_app.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "market-app")
public class MarketAppProperty {

    private String[] corsOrigins;
    
    private String paymentServiceHost;
}
