package com.stock.metrics.marketdata.service;

import com.stock.metrics.marketdata.model.QuoteDocument;
import com.stock.metrics.marketdata.model.QuoteDto;
import com.stock.metrics.marketdata.model.QuoteRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Service
public class MarketDataIngestionService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataIngestionService.class);
    private final WebClient webClient;
    private final QuoteRepository quoteRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topicPattern;

    public MarketDataIngestionService(WebClient.Builder builder,
                                      QuoteRepository quoteRepository,
                                      KafkaTemplate<String, Object> kafkaTemplate,
                                      @Value("${marketdata.upstream.base-url:https://stooq.pl}") String baseUrl,
                                      @Value("${marketdata.kafka.topic-pattern:prices.%s}") String topicPattern) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.quoteRepository = quoteRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topicPattern = topicPattern;
    }

    @CircuitBreaker(name = "marketdata", fallbackMethod = "fallbackQuote")
    public Mono<QuoteDto> fetchQuote(String symbol) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/quote/{symbol}")
                        .build(symbol))
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> toQuote(symbol, map))
                .flatMap(this::persistAndPublish);
    }

    private QuoteDto toQuote(String symbol, Map<String, Object> body) {
        BigDecimal last = new BigDecimal(String.valueOf(body.getOrDefault("price", "0")));
        BigDecimal bid = body.containsKey("bid") ? new BigDecimal(String.valueOf(body.get("bid"))) : null;
        BigDecimal ask = body.containsKey("ask") ? new BigDecimal(String.valueOf(body.get("ask"))) : null;
        Long volume = body.containsKey("volume") ? Long.valueOf(String.valueOf(body.get("volume"))) : null;
        return new QuoteDto(symbol, Instant.now(), last, bid, ask, volume);
    }

    private Mono<QuoteDto> persistAndPublish(QuoteDto quote) {
        QuoteDocument document = new QuoteDocument(
                quote.symbol(), quote.timestamp(),
                quote.last(), quote.last(), quote.last(), quote.last(), quote.volume(), "1m");
        return quoteRepository.save(document)
                .doOnSuccess(saved -> kafkaTemplate.send(topicPattern.formatted(quote.symbol()), quote.symbol(), quote)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to publish quote", ex);
                            }
                        }))
                .thenReturn(quote);
    }

    private Mono<QuoteDto> fallbackQuote(String symbol, Throwable throwable) {
        log.warn("Fallback quote for {} due to {}", symbol, throwable.getMessage());
        return quoteRepository.findBySymbolAndTimestampBetween(symbol, Instant.now().minusSeconds(3600), Instant.now())
                .last()
                .map(doc -> new QuoteDto(doc.getSymbol(), doc.getTimestamp(), doc.getClose(), doc.getClose(), doc.getClose(), doc.getVolume()));
    }
}
