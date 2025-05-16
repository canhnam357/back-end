package com.bookstore.Config;

import jakarta.validation.constraints.NotNull;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import javax.net.ssl.SSLContext;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Override
    public @NotNull ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .usingSsl(createInsecureSslContext())
                .withBasicAuth("elastic", "OMvIE_PswR-R99DK8QYN")
                .build();
    }

    private SSLContext createInsecureSslContext() {
        try {
            return SSLContextBuilder.create()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }
}