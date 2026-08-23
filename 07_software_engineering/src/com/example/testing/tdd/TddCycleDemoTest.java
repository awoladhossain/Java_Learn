package com.example.testing.tdd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test-Driven Development (TDD) Red-Green-Refactor Specification Suite")
public class TddCycleDemoTest {

    private DiscountCalculator discountCalculator;

    @BeforeEach
    void setUp() {
        discountCalculator = new DiscountCalculator();
    }

    @Test
    @DisplayName("TDD Spec 1: Purchases under $100 receive 0% discount")
    void testPurchasesUnder100GetNoDiscount() {
        BigDecimal discount = discountCalculator.calculateDiscount(new BigDecimal("99.99"), false);
        assertEquals(new BigDecimal("0.00"), discount);
    }

    @Test
    @DisplayName("TDD Spec 2: Purchases between $100 and $499.99 receive 5% discount")
    void testPurchasesBetween100And500GetFivePercentDiscount() {
        BigDecimal discount = discountCalculator.calculateDiscount(new BigDecimal("200.00"), false);
        assertEquals(new BigDecimal("10.00"), discount); // 5% of 200 = 10.00
    }

    @Test
    @DisplayName("TDD Spec 3: Purchases between $500 and $999.99 receive 10% discount")
    void testPurchasesBetween500And1000GetTenPercentDiscount() {
        BigDecimal discount = discountCalculator.calculateDiscount(new BigDecimal("500.00"), false);
        assertEquals(new BigDecimal("50.00"), discount); // 10% of 500 = 50.00
    }

    @Test
    @DisplayName("TDD Spec 4: Purchases $1000 and above receive 15% discount")
    void testPurchasesOver1000GetFifteenPercentDiscount() {
        BigDecimal discount = discountCalculator.calculateDiscount(new BigDecimal("1000.00"), false);
        assertEquals(new BigDecimal("150.00"), discount); // 15% of 1000 = 150.00
    }

    @Test
    @DisplayName("TDD Spec 5: Loyal Customers get additional 2% discount bonus")
    void testLoyalCustomerBonusDiscount() {
        // $200 purchase + Loyal (+2% -> 7% total) = 14.00
        BigDecimal discount = discountCalculator.calculateDiscount(new BigDecimal("200.00"), true);
        assertEquals(new BigDecimal("14.00"), discount);
    }

    @ParameterizedTest(name = "TDD Boundary Check: Amount {0}, Loyal: {1} -> Expected Discount: {2}")
    @CsvSource({
        "0.00, false, 0.00",
        "100.00, false, 5.00",
        "100.00, true, 7.00",
        "500.00, true, 60.00",    // 10% + 2% = 12% of 500 = 60.00
        "1000.00, true, 170.00",  // 15% + 2% = 17% of 1000 = 170.00
        "2000.00, true, 340.00"   // 15% + 2% = 17% of 2000 = 340.00
    })
    @DisplayName("TDD Parameterized Boundary Assertions")
    void testTddBoundaryConditions(BigDecimal amount, boolean isLoyal, BigDecimal expectedDiscount) {
        BigDecimal discount = discountCalculator.calculateDiscount(amount, isLoyal);
        assertEquals(expectedDiscount, discount);
    }

    @Test
    @DisplayName("TDD Spec 6: Negative purchase amount throws IllegalArgumentException")
    void testNegativeAmountThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            discountCalculator.calculateDiscount(new BigDecimal("-50.00"), false)
        );
    }
}
