package com.example.testing.junit;

import com.example.testing.domain.Order;
import com.example.testing.domain.OrderStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JUnit 5 Jupiter Mechanics & Core Assertions Test Suite")
public class Junit5MechanicsTest {

    private static int totalTestExecutions = 0;
    private Order sampleOrder;

    @BeforeAll
    static void initGlobalTestSuite() {
        System.out.println("   [JUNIT 5 @BeforeAll] Initializing test suite execution context...");
        totalTestExecutions = 0;
    }

    @AfterAll
    static void teardownGlobalTestSuite() {
        System.out.println("   [JUNIT 5 @AfterAll] Completed test suite execution. Total tests executed: " + totalTestExecutions);
    }

    @BeforeEach
    void setUpPerTestEnvironment() {
        totalTestExecutions++;
        sampleOrder = new Order("ORD-999", "CUST-001", new BigDecimal("150.00"), OrderStatus.PENDING);
    }

    @AfterEach
    void tearDownPerTestEnvironment() {
        sampleOrder = null;
    }

    @Test
    @DisplayName("Verify valid Order creation assertions")
    void testValidOrderCreation() {
        assertNotNull(sampleOrder, "Sample order should be initialized");
        assertEquals("ORD-999", sampleOrder.id(), "Order ID should match");
        assertEquals("CUST-001", sampleOrder.customerId(), "Customer ID should match");
        assertEquals(new BigDecimal("150.00"), sampleOrder.amount(), "Order amount should match");
        assertEquals(OrderStatus.PENDING, sampleOrder.status(), "Order status should default to PENDING");
    }

    @Test
    @DisplayName("Verify Grouped Assertions (assertAll)")
    void testGroupedAssertions() {
        assertAll("Order State Verification",
            () -> assertEquals("ORD-999", sampleOrder.id()),
            () -> assertEquals("CUST-001", sampleOrder.customerId()),
            () -> assertEquals(OrderStatus.PENDING, sampleOrder.status())
        );
    }

    @Test
    @DisplayName("Verify Exception Assertions (assertThrows)")
    void testInvalidOrderCreationThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Order("", "CUST-001", new BigDecimal("100.00"), OrderStatus.PENDING),
            "Expected IllegalArgumentException for blank Order ID"
        );

        assertTrue(exception.getMessage().contains("Order ID cannot be null or blank"));
    }

    @Test
    @DisplayName("Verify Performance SLA Timeout (assertTimeout)")
    void testExecutionTimeoutSla() {
        assertTimeout(Duration.ofMillis(200), () -> {
            // Fast computational work inside SLA threshold
            Thread.sleep(10);
            return "SUCCESS";
        });
    }

    @ParameterizedTest(name = "Test Order ID format validation for value: {0}")
    @ValueSource(strings = {"ORD-001", "ORD-002", "ORD-999", "ORDER-VIP-100"})
    @DisplayName("Parameterized Test with @ValueSource")
    void testValidOrderIds(String orderId) {
        Order order = new Order(orderId, "CUST-100", new BigDecimal("50.00"), OrderStatus.PENDING);
        assertEquals(orderId, order.id());
    }

    @ParameterizedTest(name = "Order amount {0} calculate 10% tax -> expected tax {1}")
    @CsvSource({
        "100.00, 10.00",
        "250.00, 25.00",
        "500.00, 50.00"
    })
    @DisplayName("Parameterized Test with @CsvSource")
    void testTaxCalculationFromCsvSource(BigDecimal amount, BigDecimal expectedTax) {
        BigDecimal calculatedTax = amount.multiply(new BigDecimal("0.10"));
        assertEquals(0, expectedTax.compareTo(calculatedTax), "Tax calculation must match expected CSV value");
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    @DisplayName("Parameterized Test with @EnumSource")
    void testAllOrderStatusEnumValues(OrderStatus status) {
        Order order = new Order("ORD-888", "CUST-100", new BigDecimal("99.99"), status);
        assertNotNull(order.status());
        assertEquals(status, order.status());
    }

    @Nested
    @DisplayName("Nested Class: Order Boundary Condition Tests")
    class OrderBoundaryConditionTests {

        @Test
        @DisplayName("Reject zero or negative order amounts")
        void testZeroOrNegativeAmountRejection() {
            assertThrows(IllegalArgumentException.class, () -> 
                new Order("ORD-1", "CUST-1", new BigDecimal("0.00"), OrderStatus.PENDING)
            );

            assertThrows(IllegalArgumentException.class, () -> 
                new Order("ORD-1", "CUST-1", new BigDecimal("-15.00"), OrderStatus.PENDING)
            );
        }
    }
}
