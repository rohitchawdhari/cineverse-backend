package com.cineverse.cineversebackend.repositories;

import com.cineverse.cineversebackend.models.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByBookingId(String bookingId);
}
