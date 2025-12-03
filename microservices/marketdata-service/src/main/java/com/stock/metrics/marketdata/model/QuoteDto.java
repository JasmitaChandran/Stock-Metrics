package com.stock.metrics.marketdata.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record QuoteDto(
        @NotBlank String symbol,
        @NotNull Instant timestamp,
        @NotNull BigDecimal last,
        BigDecimal bid,
        BigDecimal ask,
        Long volume
) {
}
