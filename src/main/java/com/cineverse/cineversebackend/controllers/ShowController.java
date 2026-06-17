package com.cineverse.cineversebackend.controllers;

import com.cineverse.cineversebackend.models.Movie;
import com.cineverse.cineversebackend.models.ShowTime;
import com.cineverse.cineversebackend.models.Theater;
import com.cineverse.cineversebackend.repositories.MovieRepository;
import com.cineverse.cineversebackend.repositories.ShowRepository;
import com.cineverse.cineversebackend.repositories.TheaterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class ShowController {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    // Helper to calculate show status dynamically relative to current server time
    private String calculateShowStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) {
            return "UPCOMING";
        } else if (now.isAfter(end)) {
            return "COMPLETED";
        } else {
            return "RUNNING";
        }
    }

    private Map<String, Object> mapShowWithStatus(ShowTime show) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", show.getId());
        map.put("movieId", show.getMovieId());
        map.put("movieTitle", show.getMovieTitle());
        map.put("theaterId", show.getTheaterId());
        map.put("theaterName", show.getTheaterName());
        map.put("screenId", show.getScreenId());
        map.put("screenNumber", show.getScreenNumber());
        map.put("startTime", show.getStartTime().toString());
        map.put("endTime", show.getEndTime().toString());
        map.put("pricing", show.getPricing());
        map.put("bookedSeats", show.getBookedSeats());
        map.put("status", calculateShowStatus(show.getStartTime(), show.getEndTime()));
        return map;
    }

    // --- PUBLIC ENDPOINTS ---

    @GetMapping("/api/shows")
    public ResponseEntity<?> getAllShows(
            @RequestParam(value = "movieId", required = false) String movieId,
            @RequestParam(value = "theaterId", required = false) String theaterId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "city", required = false) String city) {
        
        List<ShowTime> shows;
        if (movieId != null && !movieId.trim().isEmpty()) {
            shows = showRepository.findByMovieId(movieId);
        } else if (theaterId != null && !theaterId.trim().isEmpty()) {
            shows = showRepository.findByTheaterId(theaterId);
        } else {
            shows = showRepository.findAll();
        }

        if (city != null && !city.trim().isEmpty()) {
            List<Theater> theaters = theaterRepository.findByCityIgnoreCase(city);
            Set<String> theaterIds = theaters.stream().map(Theater::getId).collect(Collectors.toSet());
            shows = shows.stream()
                    .filter(show -> theaterIds.contains(show.getTheaterId()))
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> response = shows.stream()
                .map(this::mapShowWithStatus)
                .collect(Collectors.toList());

        if (status != null && !status.trim().isEmpty()) {
            response = response.stream()
                    .filter(s -> s.get("status").toString().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/shows/{id}")
    public ResponseEntity<?> getShowById(@PathVariable("id") String id) {
        Optional<ShowTime> showOpt = showRepository.findById(id);
        if (showOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ShowTime show = showOpt.get();
        return ResponseEntity.ok(mapShowWithStatus(show));
    }

    // --- ADMIN ENDPOINTS (Secured under /api/admin/**) ---

    @PostMapping("/api/admin/shows")
    public ResponseEntity<?> createShow(@RequestBody Map<String, Object> request) {
        String movieId = (String) request.get("movieId");
        String theaterId = (String) request.get("theaterId");
        String screenId = (String) request.get("screenId");
        
        LocalDateTime startTime = LocalDateTime.parse((String) request.get("startTime"));
        LocalDateTime endTime = LocalDateTime.parse((String) request.get("endTime"));
        
        @SuppressWarnings("unchecked")
        Map<String, Double> pricing = (Map<String, Double>) request.get("pricing");

        // Validate references
        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Movie does not exist"));
        }

        Optional<Theater> theaterOpt = theaterRepository.findById(theaterId);
        if (theaterOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Theater does not exist"));
        }

        Movie movie = movieOpt.get();
        Theater theater = theaterOpt.get();

        // Check if screen exists in theater and get screen name
        Optional<Theater.Screen> screenOpt = theater.getScreens().stream()
                .filter(s -> s.getId().equals(screenId))
                .findFirst();

        if (screenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Screen does not exist in the specified theater"));
        }

        Theater.Screen screen = screenOpt.get();

        ShowTime show = ShowTime.builder()
                .movieId(movieId)
                .movieTitle(movie.getTitle())
                .theaterId(theaterId)
                .theaterName(theater.getName())
                .screenId(screenId)
                .screenNumber(screen.getScreenNumber())
                .startTime(startTime)
                .endTime(endTime)
                .pricing(pricing)
                .build();

        ShowTime savedShow = showRepository.save(show);
        return ResponseEntity.ok(mapShowWithStatus(savedShow));
    }

    @DeleteMapping("/api/admin/shows/{id}")
    public ResponseEntity<?> deleteShow(@PathVariable("id") String id) {
        if (!showRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        showRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Show deleted successfully"));
    }
}
