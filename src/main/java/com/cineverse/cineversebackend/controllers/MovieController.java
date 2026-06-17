package com.cineverse.cineversebackend.controllers;

import com.cineverse.cineversebackend.models.Movie;
import com.cineverse.cineversebackend.models.User;
import com.cineverse.cineversebackend.models.Theater;
import com.cineverse.cineversebackend.models.ShowTime;
import com.cineverse.cineversebackend.repositories.MovieRepository;
import com.cineverse.cineversebackend.repositories.TheaterRepository;
import com.cineverse.cineversebackend.repositories.ShowRepository;
import com.cineverse.cineversebackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private UserRepository userRepository;

    // --- PUBLIC ENDPOINTS ---

    @GetMapping("/api/movies")
    public List<Movie> getAllMovies(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "city", required = false) String city) {
        
        List<Movie> movies;
        if (search != null && !search.trim().isEmpty()) {
            movies = movieRepository.findByTitleContainingIgnoreCase(search);
        } else {
            movies = movieRepository.findAll();
        }

        if (city != null && !city.trim().isEmpty()) {
            // Filter theaters in this city
            List<Theater> theaters = theaterRepository.findByCityIgnoreCase(city);
            Set<String> theaterIds = theaters.stream().map(Theater::getId).collect(Collectors.toSet());

            // Get showtimes in those theaters
            List<ShowTime> shows = showRepository.findAll().stream()
                    .filter(show -> theaterIds.contains(show.getTheaterId()))
                    .collect(Collectors.toList());

            // Collect unique movie IDs from those shows
            Set<String> movieIdsInCity = shows.stream().map(ShowTime::getMovieId).collect(Collectors.toSet());

            // Filter the movies list
            movies = movies.stream()
                    .filter(movie -> movieIdsInCity.contains(movie.getId()))
                    .collect(Collectors.toList());
        }

        return movies;
    }

    @GetMapping("/api/movies/{id}")
    public ResponseEntity<?> getMovieById(@PathVariable("id") String id) {
        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(movieOpt.get());
    }

    @PostMapping("/api/movies/{id}/reviews")
    public ResponseEntity<?> addMovieReview(@PathVariable("id") String id,
                                            @RequestBody Map<String, Object> request,
                                            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Only logged-in users can leave reviews"));
        }

        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Movie movie = movieOpt.get();
        double rating = Double.parseDouble(request.get("rating").toString());
        String comment = (String) request.get("comment");

        Movie.Review review = new Movie.Review(
                UUID.randomUUID().toString(),
                user.getName(),
                rating,
                comment,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                new ArrayList<>()
        );

        movie.getReviews().add(review);
        movieRepository.save(movie);

        return ResponseEntity.ok(movie);
    }

    @PutMapping("/api/movies/{id}/reviews/{reviewId}")
    public ResponseEntity<?> editMovieReview(@PathVariable("id") String id,
                                             @PathVariable("reviewId") String reviewId,
                                             @RequestBody Map<String, Object> request,
                                             @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Only logged-in users can edit reviews"));
        }

        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Movie movie = movieOpt.get();
        Movie.Review targetReview = null;
        for (Movie.Review r : movie.getReviews()) {
            if (r.getId() != null && r.getId().equals(reviewId)) {
                targetReview = r;
                break;
            }
        }

        if (targetReview == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Review not found"));
        }

        if (!targetReview.getUsername().equals(user.getName())) {
            return ResponseEntity.status(403).body(Map.of("message", "You can only edit your own reviews"));
        }

        if (request.containsKey("rating")) {
            targetReview.setRating(Double.parseDouble(request.get("rating").toString()));
        }
        if (request.containsKey("comment")) {
            targetReview.setComment((String) request.get("comment"));
        }
        targetReview.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        movieRepository.save(movie);
        return ResponseEntity.ok(movie);
    }

    @DeleteMapping("/api/movies/{id}/reviews/{reviewId}")
    public ResponseEntity<?> deleteMovieReview(@PathVariable("id") String id,
                                               @PathVariable("reviewId") String reviewId,
                                               @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Only logged-in users can delete reviews"));
        }

        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Movie movie = movieOpt.get();
        Movie.Review targetReview = null;
        for (Movie.Review r : movie.getReviews()) {
            if (r.getId() != null && r.getId().equals(reviewId)) {
                targetReview = r;
                break;
            }
        }

        if (targetReview == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Review not found"));
        }

        if (!targetReview.getUsername().equals(user.getName()) && !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("message", "You do not have permission to delete this review"));
        }

        movie.getReviews().remove(targetReview);
        movieRepository.save(movie);
        return ResponseEntity.ok(movie);
    }

    @PostMapping("/api/movies/{id}/reviews/{reviewId}/like")
    public ResponseEntity<?> likeMovieReview(@PathVariable("id") String id,
                                             @PathVariable("reviewId") String reviewId,
                                             @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Only logged-in users can like reviews"));
        }

        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Movie movie = movieOpt.get();
        Movie.Review targetReview = null;
        for (Movie.Review r : movie.getReviews()) {
            if (r.getId() != null && r.getId().equals(reviewId)) {
                targetReview = r;
                break;
            }
        }

        if (targetReview == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Review not found"));
        }

        List<String> likedBy = targetReview.getLikedBy();
        if (likedBy == null) {
            likedBy = new ArrayList<>();
        }

        if (likedBy.contains(user.getName())) {
            likedBy.remove(user.getName());
        } else {
            likedBy.add(user.getName());
        }
        targetReview.setLikedBy(likedBy);

        movieRepository.save(movie);
        return ResponseEntity.ok(movie);
    }

    @PostMapping("/api/movies/{id}/wishlist")
    public ResponseEntity<?> toggleWishlist(@PathVariable("id") String id,
                                            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Only logged-in users can manage watchlist"));
        }

        Optional<User> userOpt = userRepository.findById(user.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
        
        User dbUser = userOpt.get();
        List<String> wishlist = dbUser.getWishlist();
        if (wishlist == null) {
            wishlist = new ArrayList<>();
        }

        boolean isAdded;
        if (wishlist.contains(id)) {
            wishlist.remove(id);
            isAdded = false;
        } else {
            wishlist.add(id);
            isAdded = true;
        }
        dbUser.setWishlist(wishlist);
        userRepository.save(dbUser);

        return ResponseEntity.ok(Map.of("wishlist", wishlist, "added", isAdded));
    }

    // --- ADMIN CRUD ENDPOINTS ---

    @PostMapping("/api/admin/movies")
    public ResponseEntity<?> createMovie(@RequestBody Movie movie) {
        if (movie.getTitle() == null || movie.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Movie title is required"));
        }
        Movie savedMovie = movieRepository.save(movie);
        return ResponseEntity.ok(savedMovie);
    }

    @PutMapping("/api/admin/movies/{id}")
    public ResponseEntity<?> updateMovie(@PathVariable("id") String id, @RequestBody Movie movieDetails) {
        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Movie movie = movieOpt.get();
        movie.setTitle(movieDetails.getTitle());
        movie.setDescription(movieDetails.getDescription());
        movie.setDuration(movieDetails.getDuration());
        movie.setGenre(movieDetails.getGenre());
        movie.setLanguage(movieDetails.getLanguage());
        movie.setRating(movieDetails.getRating());
        movie.setReleaseDate(movieDetails.getReleaseDate());
        movie.setPoster(movieDetails.getPoster());
        movie.setBanner(movieDetails.getBanner());
        movie.setTrailerUrl(movieDetails.getTrailerUrl());
        movie.setCast(movieDetails.getCast());
        movie.setDirector(movieDetails.getDirector());

        Movie updatedMovie = movieRepository.save(movie);
        return ResponseEntity.ok(updatedMovie);
    }

    @DeleteMapping("/api/admin/movies/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable("id") String id) {
        if (!movieRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        movieRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Movie deleted successfully"));
    }
}
