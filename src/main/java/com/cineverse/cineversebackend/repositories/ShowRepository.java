package com.cineverse.cineversebackend.repositories;

import com.cineverse.cineversebackend.models.ShowTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ShowRepository extends MongoRepository<ShowTime, String> {
    List<ShowTime> findByMovieId(String movieId);
    List<ShowTime> findByTheaterId(String theaterId);
    List<ShowTime> findByStartTimeAfter(LocalDateTime dateTime);
}
