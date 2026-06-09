package com.datang.aibase.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class LoggingGatewayFilterFactory extends AbstractGatewayFilterFactory<LoggingGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(LoggingGatewayFilterFactory.class);

    public LoggingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            Instant start = Instant.now();

            return chain.filter(exchange).doFinally(signal -> {
                Duration elapsed = Duration.between(start, Instant.now());
                var response = exchange.getResponse();
                String traceId = response.getHeaders().getFirst("X-Trace-Id");

                log.info("{} {} → {} {}ms [trace={}]",
                        request.getMethod(),
                        request.getURI().getPath(),
                        response.getStatusCode() != null ? response.getStatusCode().value() : "???",
                        elapsed.toMillis(),
                        traceId != null ? traceId : "N/A");
            });
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of();
    }

    public static class Config {
    }
}
