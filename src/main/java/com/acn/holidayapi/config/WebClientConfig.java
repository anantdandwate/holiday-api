package com.acn.holidayapi.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${nager.date.api.base-url:https://date.nager.at/api/v3}")
    private String baseUrl;

    @Value("${ssl.truststore.path:certs/truststore.jks}")
    private String truststorePath;

    @Value("${ssl.truststore.password:changeit}")
    private String truststorePassword;

    @Bean
    public WebClient webClient() {
        log.info("🔧 Creating WebClient bean...");

        SslContext sslContext = createSslContext();

        HttpClient httpClient = HttpClient.create()
                .secure(sslSpec -> sslSpec.sslContext(sslContext));

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)// .baseUrl(baseUrl != null ? baseUrl : "https://date.nager.at/api/v3")
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        log.info("✅ WebClient bean created successfully with base URL: {}", baseUrl);
        return client;
    }

    private SslContext createSslContext() {
        try {
            File truststoreFile = new File(truststorePath);

            if (truststoreFile.exists()) {
                log.info("📁 Loading custom truststore from: {}", truststoreFile.getAbsolutePath());
                return buildCustomSslContext(truststoreFile);
            } else {
                log.warn("⚠️ Custom truststore not found at: {}", truststoreFile.getAbsolutePath());
                log.warn("⚠️ Falling back to INSECURE mode (trust all certificates)");
                log.warn("⚠️ THIS IS FOR DEVELOPMENT ONLY!");
                return buildInsecureSslContext();
            }

        } catch (Exception e) {
            log.error("❌ Failed to load SSL context: {}", e.getMessage(), e);
            log.warn("⚠️ Falling back to INSECURE mode");
            return buildInsecureSslContext();
        }
    }

    private SslContext buildCustomSslContext(File truststoreFile) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");

        try (FileInputStream fis = new FileInputStream(truststoreFile)) {
            trustStore.load(fis, truststorePassword.toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        int certCount = trustStore.size();
        log.info("✅ Custom truststore loaded with {} certificates", certCount);

        return SslContextBuilder.forClient()
                .trustManager(tmf)
                .build();
    }

    private SslContext buildInsecureSslContext() {
        try {
            return SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }
}