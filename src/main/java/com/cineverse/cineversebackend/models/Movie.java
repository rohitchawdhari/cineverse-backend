package com.cineverse.cineversebackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "movies")
public class Movie {
    @Id
    private String id;
    private String title;
    private String description;
    private int duration;
    private String genre;
    private String language;
    private String rating;
    private String releaseDate;
    private String poster;
    private String banner;
    private String trailerUrl;
    private String cast;
    private String director;
    private List<Review> reviews = new ArrayList<>();

    public Movie() {}

    public Movie(String id, String title, String description, int duration, String genre, String language, String rating, String releaseDate, String poster, String banner, String trailerUrl, String cast, String director, List<Review> reviews) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.genre = genre;
        this.language = language;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.poster = poster;
        this.banner = banner;
        this.trailerUrl = trailerUrl;
        this.cast = cast;
        this.director = director;
        this.reviews = reviews != null ? reviews : new ArrayList<>();
    }

    public static MovieBuilder builder() {
        return new MovieBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }

    public static class Review {
        private String id;
        private String username;
        private double rating;
        private String comment;
        private String timestamp;
        private List<String> likedBy = new ArrayList<>();

        public Review() {
            this.id = UUID.randomUUID().toString();
        }

        public Review(String id, String username, double rating, String comment, String timestamp, List<String> likedBy) {
            this.id = id != null ? id : UUID.randomUUID().toString();
            this.username = username;
            this.rating = rating;
            this.comment = comment;
            this.timestamp = timestamp;
            this.likedBy = likedBy != null ? likedBy : new ArrayList<>();
        }

        public static ReviewBuilder builder() {
            return new ReviewBuilder();
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public List<String> getLikedBy() { return likedBy; }
        public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }

        public static class ReviewBuilder {
            private String id;
            private String username;
            private double rating;
            private String comment;
            private String timestamp;
            private List<String> likedBy = new ArrayList<>();

            public ReviewBuilder id(String id) { this.id = id; return this; }
            public ReviewBuilder username(String username) { this.username = username; return this; }
            public ReviewBuilder rating(double rating) { this.rating = rating; return this; }
            public ReviewBuilder comment(String comment) { this.comment = comment; return this; }
            public ReviewBuilder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
            public ReviewBuilder likedBy(List<String> likedBy) { this.likedBy = likedBy; return this; }

            public Review build() {
                return new Review(id, username, rating, comment, timestamp, likedBy);
            }
        }
    }

    public static class MovieBuilder {
        private String id;
        private String title;
        private String description;
        private int duration;
        private String genre;
        private String language;
        private String rating;
        private String releaseDate;
        private String poster;
        private String banner;
        private String trailerUrl;
        private String cast;
        private String director;
        private List<Review> reviews;

        public MovieBuilder id(String id) { this.id = id; return this; }
        public MovieBuilder title(String title) { this.title = title; return this; }
        public MovieBuilder description(String desc) { this.description = desc; return this; }
        public MovieBuilder duration(int dur) { this.duration = dur; return this; }
        public MovieBuilder genre(String gen) { this.genre = gen; return this; }
        public MovieBuilder language(String lang) { this.language = lang; return this; }
        public MovieBuilder rating(String rat) { this.rating = rat; return this; }
        public MovieBuilder releaseDate(String date) { this.releaseDate = date; return this; }
        public MovieBuilder poster(String pos) { this.poster = pos; return this; }
        public MovieBuilder banner(String ban) { this.banner = ban; return this; }
        public MovieBuilder trailerUrl(String url) { this.trailerUrl = url; return this; }
        public MovieBuilder cast(String cast) { this.cast = cast; return this; }
        public MovieBuilder director(String director) { this.director = director; return this; }
        public MovieBuilder reviews(List<Review> rev) { this.reviews = rev; return this; }

        public Movie build() {
            return new Movie(id, title, description, duration, genre, language, rating, releaseDate, poster, banner, trailerUrl, cast, director, reviews);
        }
    }
}
