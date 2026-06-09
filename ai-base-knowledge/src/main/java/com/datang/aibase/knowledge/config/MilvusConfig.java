package com.datang.aibase.knowledge.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "knowledge.repository.type", havingValue = "milvus")
public class MilvusConfig {

    private static final Logger log = LoggerFactory.getLogger(MilvusConfig.class);

    private final MilvusProperties props;

    public MilvusConfig(MilvusProperties props) {
        this.props = props;
    }

    @Bean
    public MilvusServiceClient milvusServiceClient() {
        ConnectParam.Builder builder = ConnectParam.newBuilder()
                .withHost(props.getHost())
                .withPort(props.getPort())
                .withDatabaseName(props.getDatabase());
        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            builder.withAuthorization(props.getUsername(), props.getPassword() != null ? props.getPassword() : "");
        }
        log.info("Milvus client connecting to {}:{}/{}", props.getHost(), props.getPort(), props.getDatabase());
        return new MilvusServiceClient(builder.build());
    }
}
