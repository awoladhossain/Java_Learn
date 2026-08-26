package com.example.patterns.behavioral;

/**
 * 🛠️ Behavioral Pattern: State
 * 
 * Allows an object to alter its behavior when its internal state changes.
 * The object will appear to change its class.
 * E.g., Order Processing state machine (Created -> Paid -> Shipped -> Delivered).
 */
public class StatePattern {

    // Context
    public static class OrderContext {
        private OrderState currentState;
        private final String orderId;

        public OrderContext(String orderId) {
            this.orderId = orderId;
            this.currentState = new CreatedState(); // Initial state
        }

        public void setState(OrderState state) {
            this.currentState = state;
        }

        public OrderState getState() {
            return currentState;
        }

        public String getOrderId() {
            return orderId;
        }

        public String proceedNext() {
            return currentState.next(this);
        }

        public String cancel() {
            return currentState.cancel(this);
        }
    }

    // State Interface
    public interface OrderState {
        String next(OrderContext context);
        String cancel(OrderContext context);
        String getStatus();
    }

    // Concrete State 1: Created
    public static class CreatedState implements OrderState {
        @Override
        public String next(OrderContext context) {
            context.setState(new PaidState());
            return "Order " + context.getOrderId() + " payment processed -> State: PAID";
        }

        @Override
        public String cancel(OrderContext context) {
            context.setState(new CancelledState());
            return "Order " + context.getOrderId() + " cancelled -> State: CANCELLED";
        }

        @Override
        public String getStatus() { return "CREATED"; }
    }

    // Concrete State 2: Paid
    public static class PaidState implements OrderState {
        @Override
        public String next(OrderContext context) {
            context.setState(new ShippedState());
            return "Order " + context.getOrderId() + " dispatched to carrier -> State: SHIPPED";
        }

        @Override
        public String cancel(OrderContext context) {
            context.setState(new CancelledState());
            return "Order " + context.getOrderId() + " refunded and cancelled -> State: CANCELLED";
        }

        @Override
        public String getStatus() { return "PAID"; }
    }

    // Concrete State 3: Shipped
    public static class ShippedState implements OrderState {
        @Override
        public String next(OrderContext context) {
            context.setState(new DeliveredState());
            return "Order " + context.getOrderId() + " handed to recipient -> State: DELIVERED";
        }

        @Override
        public String cancel(OrderContext context) {
            return "ERROR: Cannot cancel order " + context.getOrderId() + " after it has been shipped!";
        }

        @Override
        public String getStatus() { return "SHIPPED"; }
    }

    // Concrete State 4: Delivered (Terminal state)
    public static class DeliveredState implements OrderState {
        @Override
        public String next(OrderContext context) {
            return "Order " + context.getOrderId() + " is already DELIVERED.";
        }

        @Override
        public String cancel(OrderContext context) {
            return "ERROR: Delivered order cannot be cancelled.";
        }

        @Override
        public String getStatus() { return "DELIVERED"; }
    }

    // Concrete State 5: Cancelled (Terminal state)
    public static class CancelledState implements OrderState {
        @Override
        public String next(OrderContext context) {
            return "ERROR: Cancelled order cannot proceed.";
        }

        @Override
        public String cancel(OrderContext context) {
            return "Order is already CANCELLED.";
        }

        @Override
        public String getStatus() { return "CANCELLED"; }
    }
}
