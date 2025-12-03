package com.stock.metrics.marketdata.controller;

import com.stock.metrics.marketdata.model.QuoteDocument;
import com.stock.metrics.marketdata.model.QuoteDto;
import com.stock.metrics.marketdata.model.QuoteRepository;
import com.stock.metrics.marketdata.service.MarketDataIngestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
@RequestMapping(path = "/marketdata", produces = MediaType.APPLICATION_JSON_VALUE)
public class MarketDataController {

    private final MarketDataIngestionService ingestionService;
    private final QuoteRepository quoteRepository;

    public MarketDataController(MarketDataIngestionService ingestionService, QuoteRepository quoteRepository) {
        this.ingestionService = ingestionService;
        this.quoteRepository = quoteRepository;
    }

    @PostMapping("/symbols")
    public Mono<Void> addSymbol(@RequestBody @Valid SymbolRequest request) {
        return ingestionService.fetchQuote(request.symbol()).then();
    }

    @GetMapping("/{symbol}/quote")
    public Mono<QuoteDto> latestQuote(@PathVariable @NotBlank String symbol) {
        return ingestionService.fetchQuote(symbol);
    }

    @GetMapping("/{symbol}/history")
    public Flux<QuoteDocument> history(@PathVariable String symbol,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        return quoteRepository.findBySymbolAndTimestampBetween(symbol, start, end);
    }

    public record SymbolRequest(@NotBlank String symbol) {}
}
