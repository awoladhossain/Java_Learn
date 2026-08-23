package com.example.testing.tdd;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tiered Discount Calculator built via Test-Driven Development (TDD).
 * 
 * Rules:
 * - Amount < $100         : 0% discount
 * - $100 <= Amount < $500 : 5% discount
 * - $500 <= Amount < $1000: 10% discount
 * - Amount >= $1000       : 15% discount
 * - Loyalty Customer Bonus: +2% additional discount
 * - Maximum Total Cap     : 20% discount
 */
public class DiscountCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal FIVE_HUNDRED = new BigDecimal("500.00");
    private static final BigDecimal THOUSAND = new BigDecimal("1000.00");
    private static final BigDecimal MAX_DISCOUNT_PERCENT = new BigDecimal("20.00");

    public BigDecimal calculateDiscount(BigDecimal originalAmount, boolean isLoyalCustomer) {
        if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Original purchase amount cannot be null or negative");
        }

        if (originalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal discountPercentage = BigDecimal.ZERO;

        if (originalAmount.compareTo(THOUSAND) >= 0) {
            discountPercentage = new BigDecimal("15.00");
        } else if (originalAmount.compareTo(FIVE_HUNDRED) >= 0) {
            discountPercentage = new BigDecimal("10.00");
        } else if (originalAmount.compareTo(HUNDRED) >= 0) {
            discountPercentage = new BigDecimal("5.00");
        }

        if (isLoyalCustomer) {
            discountPercentage = discountPercentage.add(new BigDecimal("2.00"));
        }

        if (discountPercentage.compareTo(MAX_DISCOUNT_PERCENT) > 0) {
            discountPercentage = MAX_DISCOUNT_PERCENT;
        }

        BigDecimal discountMultiplier = discountPercentage.divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return originalAmount.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
