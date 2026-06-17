package com.cineverse.cineversebackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "shows")
public class ShowTime {
    @Id
    private String id;
    private String movieId;
    private String movieTitle;
    private String theaterId;
    private String theaterName;
    private String screenId;
    private String screenNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, Double> pricing = new HashMap<>();
    private List<String> bookedSeats = new ArrayList<>();

    public ShowTime() {}

    public ShowTime(String id, String movieId, String movieTitle, String theaterId, String theaterName, String screenId, String screenNumber, LocalDateTime startTime, LocalDateTime endTime, Map<String, Double> pricing, List<String> bookedSeats) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.screenId = screenId;
        this.screenNumber = screenNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pricing = pricing != null ? pricing : new HashMap<>();
        this.bookedSeats = bookedSeats != null ? bookedSeats : new ArrayList<>();
    }

    public static ShowTimeBuilder builder() {
        return new ShowTimeBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getTheaterId() { return theaterId; }
    public void setTheaterId(String theaterId) { this.theaterId = theaterId; }

    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }

    public String getScreenId() { return screenId; }
    public void setScreenId(String screenId) { this.screenId = screenId; }

    public String getScreenNumber() { return screenNumber; }
    public void setScreenNumber(String screenNumber) { this.screenNumber = screenNumber; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Map<String, Double> getPricing() { return pricing; }
    public void setPricing(Map<String, Double> pricing) { this.pricing = pricing; }

    public List<String> getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(List<String> bookedSeats) { this.bookedSeats = bookedSeats; }

    public static class ShowTimeBuilder {
        private String id;
        private String movieId;
        private String movieTitle;
        private String theaterId;
        private String theaterName;
        private String screenId;
        private String screenNumber;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Map<String, Double> pricing;
        private List<String> bookedSeats;

        public ShowTimeBuilder id(String id) { this.id = id; return this; }
        public ShowTimeBuilder movieId(String movieId) { this.movieId = movieId; return this; }
        public ShowTimeBuilder movieTitle(String title) { this.movieTitle = title; return this; }
        public ShowTimeBuilder theaterId(String theaterId) { this.theaterId = theaterId; return this; }
        public ShowTimeBuilder theaterName(String name) { this.theaterName = name; return this; }
        public ShowTimeBuilder screenId(String screenId) { this.screenId = screenId; return this; }
        public ShowTimeBuilder screenNumber(String screenNumber) { this.screenNumber = screenNumber; return this; }
        public ShowTimeBuilder startTime(LocalDateTime time) { this.startTime = time; return this; }
        public ShowTimeBuilder endTime(LocalDateTime time) { this.endTime = time; return this; }
        public ShowTimeBuilder pricing(Map<String, Double> pricing) { this.pricing = pricing; return this; }
        public ShowTimeBuilder bookedSeats(List<String> booked) { this.bookedSeats = booked; return this; }

        public ShowTime build() {
            return new ShowTime(id, movieId, movieTitle, theaterId, theaterName, screenId, screenNumber, startTime, endTime, pricing, bookedSeats);
        }
    }
}
