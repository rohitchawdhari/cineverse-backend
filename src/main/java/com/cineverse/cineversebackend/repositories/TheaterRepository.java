package com.cineverse.cineversebackend.repositories;

import com.cineverse.cineversebackend.models.Theater;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TheaterRepository extends MongoRepository<Theater, String> {
    List<Theater> findByCityIgnoreCase(String city);
}
