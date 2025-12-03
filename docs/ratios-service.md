# Ratios Service (Financial Analytics)

Formulas implemented (inputs assumed as decimals):
- **Net Profit Margin** = Net Income / Revenue
- **Return on Equity (ROE)** = Net Income / Average Shareholder Equity
- **Return on Assets (ROA)** = Net Income / Average Total Assets
- **Gross Margin** = (Revenue - Cost of Goods Sold) / Revenue
- **Current Ratio** = Current Assets / Current Liabilities
- **Quick Ratio** = (Current Assets - Inventory) / Current Liabilities
- **P/E** = Price per Share / Earnings per Share
- **P/B** = Price per Share / (Equity / Shares Outstanding)
- **P/S** = Market Cap / Revenue
- **PEG Ratio** = (Price/Earnings) / Earnings Growth Rate
- **Debt/Equity** = Total Debt / Shareholder Equity
- **Interest Coverage** = EBIT / Interest Expense
- **Free Cash Flow Yield** = Free Cash Flow / Market Cap
- **Operating Cash Flow Ratio** = Operating Cash Flow / Current Liabilities
- **Altman Z-Score (public)** = 1.2*(WC/TA) + 1.4*(RE/TA) + 3.3*(EBIT/TA) + 0.6*(MVE/TL) + 1.0*(Sales/TA)
- **Piotroski F-Score** = Sum of nine binary signals
- **Beta (regression)** = Covariance(stock, market) / Variance(market)
- **Intrinsic Value (DCF simplified)** = Σ (FCF_t / (1+WACC)^t) + Terminal Value

## Sample DTOs and Service (Java)
```java
public record RatioRequest(String symbol, BigDecimal price, BigDecimal eps, BigDecimal revenue,
                           BigDecimal equity, BigDecimal debt, BigDecimal cashFlow,
                           BigDecimal totalAssets, BigDecimal totalLiabilities,
                           BigDecimal ebit, BigDecimal interestExpense, BigDecimal sharesOut,
                           BigDecimal inventory, BigDecimal cogs, BigDecimal growthRate, BigDecimal marketReturn,
                           BigDecimal stockReturn) {}

public record RatioResponse(BigDecimal pe, BigDecimal pb, BigDecimal ps, BigDecimal roe,
                            BigDecimal roa, BigDecimal currentRatio, BigDecimal quickRatio,
                            BigDecimal peg, BigDecimal debtToEquity, BigDecimal interestCoverage,
                            BigDecimal fcfYield, BigDecimal altmanZ, BigDecimal beta) {}

@Service
public class RatioCalculator {
    public RatioResponse calculate(RatioRequest r) {
        BigDecimal pe = safeDivide(r.price(), r.eps());
        BigDecimal pb = safeDivide(r.price(), r.equity().divide(r.sharesOut(), RoundingMode.HALF_UP));
        BigDecimal ps = safeDivide(r.price().multiply(r.sharesOut()), r.revenue());
        BigDecimal roe = safeDivide(r.eps().multiply(r.sharesOut()), r.equity());
        BigDecimal roa = safeDivide(r.eps().multiply(r.sharesOut()), r.totalAssets());
        BigDecimal current = safeDivide(r.totalAssets(), r.totalLiabilities());
        BigDecimal quick = safeDivide(r.totalAssets().subtract(r.inventory()), r.totalLiabilities());
        BigDecimal peg = safeDivide(pe, r.growthRate());
        BigDecimal debtEquity = safeDivide(r.debt(), r.equity());
        BigDecimal interestCov = safeDivide(r.ebit(), r.interestExpense());
        BigDecimal fcfYield = safeDivide(r.cashFlow(), r.price().multiply(r.sharesOut()));
        BigDecimal altmanZ = BigDecimal.valueOf(1.2).multiply(r.totalAssets().subtract(r.totalLiabilities()).divide(r.totalAssets(), RoundingMode.HALF_UP))
                .add(BigDecimal.valueOf(1.4).multiply(r.equity().divide(r.totalAssets(), RoundingMode.HALF_UP)))
                .add(BigDecimal.valueOf(3.3).multiply(r.ebit().divide(r.totalAssets(), RoundingMode.HALF_UP)))
                .add(BigDecimal.valueOf(0.6).multiply(r.price().multiply(r.sharesOut()).divide(r.totalLiabilities(), RoundingMode.HALF_UP)))
                .add(r.revenue().divide(r.totalAssets(), RoundingMode.HALF_UP));
        BigDecimal beta = safeDivide(covariance(r.stockReturn(), r.marketReturn()), variance(r.marketReturn()));
        return new RatioResponse(pe, pb, ps, roe, roa, current, quick, peg, debtEquity, interestCov, fcfYield, altmanZ, beta);
    }

    private BigDecimal safeDivide(BigDecimal num, BigDecimal den) {
        if (num == null || den == null || BigDecimal.ZERO.compareTo(den) == 0) return BigDecimal.ZERO;
        return num.divide(den, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal covariance(BigDecimal stock, BigDecimal market) {
        return stock.subtract(stock.mean(new MathContext(4))).multiply(market.subtract(market.mean(new MathContext(4))));
    }

    private BigDecimal variance(BigDecimal value) {
        return value.subtract(value.mean(new MathContext(4))).pow(2);
    }
}
```
