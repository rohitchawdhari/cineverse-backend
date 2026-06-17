package com.cineverse.cineversebackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "payments")
public class Payment {
    @Id
    private String id;
    private String bookingId;
    private double amount;
    private String status; // "SUCCESS", "FAILED"
    private LocalDateTime timestamp;
    private String paymentMethod;
    private String transactionId;

    public Payment() {}

    public Payment(String id, String bookingId, double amount, String status, LocalDateTime timestamp, String paymentMethod, String transactionId) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
    }

    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public static class PaymentBuilder {
        private String id;
        private String bookingId;
        private double amount;
        private String status;
        private LocalDateTime timestamp;
        private String paymentMethod;
        private String transactionId;

        public PaymentBuilder id(String id) { this.id = id; return this; }
        public PaymentBuilder bookingId(String bookingId) { this.bookingId = bookingId; return this; }
        public PaymentBuilder amount(double amount) { this.amount = amount; return this; }
        public PaymentBuilder status(String status) { this.status = status; return this; }
        public PaymentBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public PaymentBuilder paymentMethod(String method) { this.paymentMethod = method; return this; }
        public PaymentBuilder transactionId(String txnId) { this.transactionId = txnId; return this; }

        public Payment build() {
            return new Payment(id, bookingId, amount, status, timestamp, paymentMethod, transactionId);
        }
    }
}
