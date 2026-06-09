package com.datang.aibase.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ObservabilityGatewayFilterFactory extends AbstractGatewayFilterFactory<ObservabilityGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityGatewayFilterFactory.class);

    public ObservabilityGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            var response = exchange.getResponse();

            String traceId = request.getHeaders().getFirst("X-Trace-Id");
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            }

            response.getHeaders().add("X-Trace-Id", traceId);
            response.getHeaders().add("X-Request-Id", traceId);

            response.beforeCommit(() -> {
                response.getHeaders().add("X-Response-Time", String.valueOf(System.currentTimeMillis()));
                return reactor.core.publisher.Mono.empty();
            });

            return chain.filter(exchange);
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of();
    }

    public static class Config {
    }
}
