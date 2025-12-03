package com.stock.metrics.marketdata.model;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface QuoteRepository extends ReactiveMongoRepository<QuoteDocument, String> {
    Flux<QuoteDocument> findBySymbolAndTimestampBetween(String symbol, Instant start, Instant end);
}
