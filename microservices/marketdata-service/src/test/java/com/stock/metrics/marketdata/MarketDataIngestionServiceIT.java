package com.stock.metrics.marketdata;

import com.stock.metrics.marketdata.model.QuoteDto;
import com.stock.metrics.marketdata.model.QuoteRepository;
import com.stock.metrics.marketdata.service.MarketDataIngestionService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.WireMockServer;
import org.wiremock.client.WireMock;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class MarketDataIngestionServiceIT {

    @Container
    static final KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.5.0");

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    private static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("marketdata.upstream.base-url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @Autowired
    MarketDataIngestionService ingestionService;

    @Autowired
    QuoteRepository quoteRepository;

    @Test
    void shouldPersistAndPublishQuote() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/api/quote/AAPL"))
                .willReturn(WireMock.okJson("{" +
                        "\"price\":191.22,\"bid\":191.2,\"ask\":191.25,\"volume\":1000" +
                        "}")));

        QuoteDto dto = ingestionService.fetchQuote("AAPL").block(Duration.ofSeconds(5));

        assertThat(dto).isNotNull();
        assertThat(dto.symbol()).isEqualTo("AAPL");

        long saved = quoteRepository.count().block(Duration.ofSeconds(5));
        assertThat(saved).isGreaterThan(0);

        KafkaConsumer<String, String> consumer = createConsumer();
        consumer.subscribe(Collections.singleton("prices.AAPL"));
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "prices.AAPL", Duration.ofSeconds(5));
        assertThat(record).isNotNull();
    }

    private KafkaConsumer<String, String> createConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps("testGroup", "true", kafka.getBootstrapServers());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        Properties consumerProps = new Properties();
        consumerProps.putAll(props);
        return new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer());
    }
}
