package com.datang.aibase.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthGatewayFilterFactory.class);
    private static final String API_KEY_HEADER = "X-Api-Key";

    public AuthGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        Set<String> validKeys = config.getValidKeys();

        return (exchange, chain) -> {
            var request = exchange.getRequest();
            String apiKey = request.getHeaders().getFirst(API_KEY_HEADER);

            if (apiKey == null || apiKey.isBlank()) {
                log.warn("Missing X-Api-Key header for request: {} {}", request.getMethod(), request.getURI().getPath());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            if (!validKeys.isEmpty() && !validKeys.contains(apiKey)) {
                log.warn("Invalid X-Api-Key for request: {} {}", request.getMethod(), request.getURI().getPath());
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("validKeys");
    }

    public static class Config {
        private Set<String> validKeys = Collections.emptySet();

        public Set<String> getValidKeys() {
            return validKeys;
        }

        public void setValidKeys(Set<String> validKeys) {
            this.validKeys = validKeys != null ? validKeys : Collections.emptySet();
        }
    }
}
