package com.example.testing.mockito;

import com.example.testing.domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Mockito 5 Isolation & Behavior Verification Test Suite")
public class MockitoFrameworkTest {

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentProcessor paymentProcessor;

    @Captor
    private ArgumentCaptor<OrderStatus> statusCaptor;

    @Captor
    private ArgumentCaptor<String> transactionIdCaptor;

    private AutoCloseable mockSession;
    private Order testOrder;

    @BeforeEach
    void setUpMocks() {
        mockSession = MockitoAnnotations.openMocks(this);
        testOrder = new Order("ORD-1001", "CUST-500", new BigDecimal("250.00"), OrderStatus.PENDING);
    }

    @AfterEach
    void tearDownMocks() throws Exception {
        if (mockSession != null) {
            mockSession.close();
        }
    }

    @Test
    @DisplayName("Verify Successful Payment Flow (when().thenReturn() & verify())")
    void testSuccessfulPaymentFlow() {
        // Arrange
        String paymentToken = "tok_visa_success";
        String expectedTxId = "tx_99887766";
        PaymentResult successResult = PaymentResult.successful(expectedTxId);

        when(orderRepository.findById("ORD-1001")).thenReturn(Optional.of(testOrder));
        when(paymentGateway.processPayment(eq("ORD-1001"), eq(new BigDecimal("250.00")), eq(paymentToken)))
                .thenReturn(successResult);

        // Act
        PaymentResult result = paymentProcessor.executePayment("ORD-1001", paymentToken);

        // Assert
        assertTrue(result.success(), "Payment result should be successful");
        assertEquals(expectedTxId, result.transactionId());

        // Verify Interactions & Argument Captor
        verify(orderRepository, times(1)).findById("ORD-1001");
        verify(paymentGateway, times(1)).processPayment("ORD-1001", new BigDecimal("250.00"), paymentToken);
        
        verify(orderRepository, times(1)).updateStatus(eq("ORD-1001"), statusCaptor.capture());
        assertEquals(OrderStatus.PAID, statusCaptor.getValue(), "Order status must transition to PAID");

        verify(notificationService, times(1))
                .sendPaymentSuccessNotification(eq("CUST-500"), eq("ORD-1001"), transactionIdCaptor.capture());
        assertEquals(expectedTxId, transactionIdCaptor.getValue(), "Captured transaction ID should match gateway response");

        verify(notificationService, never()).sendPaymentFailureNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Verify Failed Gateway Payment (when().thenReturn() with failure result)")
    void testFailedGatewayPaymentFlow() {
        // Arrange
        String paymentToken = "tok_declined";
        PaymentResult failureResult = PaymentResult.failed("Insufficient Funds on Card");

        when(orderRepository.findById("ORD-1001")).thenReturn(Optional.of(testOrder));
        when(paymentGateway.processPayment(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(failureResult);

        // Act
        PaymentResult result = paymentProcessor.executePayment("ORD-1001", paymentToken);

        // Assert
        assertFalse(result.success());
        assertEquals("Insufficient Funds on Card", result.errorMessage());

        // Verify Status set to FAILED and Failure Notification dispatched
        verify(orderRepository, times(1)).updateStatus("ORD-1001", OrderStatus.FAILED);
        verify(notificationService, times(1))
                .sendPaymentFailureNotification("CUST-500", "ORD-1001", "Insufficient Funds on Card");
        verify(notificationService, never()).sendPaymentSuccessNotification(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Verify Gateway Exception Handling (when().thenThrow() & doThrow())")
    void testGatewayExceptionThrowsPaymentFailedException() {
        // Arrange
        when(orderRepository.findById("ORD-1001")).thenReturn(Optional.of(testOrder));
        when(paymentGateway.processPayment(anyString(), any(BigDecimal.class), anyString()))
                .thenThrow(new RuntimeException("Payment Gateway Timeout (HTTP 504)"));

        // Act & Assert
        PaymentFailedException exception = assertThrows(
                PaymentFailedException.class,
                () -> paymentProcessor.executePayment("ORD-1001", "tok_timeout")
        );

        assertTrue(exception.getMessage().contains("Payment Gateway Timeout"));

        // Verify status updated to FAILED even during network crash
        verify(orderRepository).updateStatus("ORD-1001", OrderStatus.FAILED);
        verify(notificationService).sendPaymentFailureNotification(eq("CUST-500"), eq("ORD-1001"), contains("Payment Gateway Timeout"));
    }

    @Test
    @DisplayName("Verify Already Paid Order Throws IllegalStateException without invoking Gateway")
    void testAlreadyPaidOrderRejection() {
        // Arrange
        Order paidOrder = new Order("ORD-1001", "CUST-500", new BigDecimal("250.00"), OrderStatus.PAID);
        when(orderRepository.findById("ORD-1001")).thenReturn(Optional.of(paidOrder));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            paymentProcessor.executePayment("ORD-1001", "tok_valid")
        );

        // Zero interactions with PaymentGateway or NotificationService
        verifyNoInteractions(paymentGateway);
        verifyNoInteractions(notificationService);
    }
}
