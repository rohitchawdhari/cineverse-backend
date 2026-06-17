package com.cineverse.cineversebackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "bookings")
public class Booking {
    @Id
    private String id; // Starts with MOV-2026-
    private String ticketNumber;
    private String userId;
    private String userName;
    private String userEmail;
    private String showId;
    private String movieTitle;
    private String moviePoster;
    private String theaterName;
    private String screenNumber;
    private LocalDateTime showStartTime;
    private List<String> seats = new ArrayList<>();
    private double totalAmount;
    private String status; // "CONFIRMED", "CANCELLED"
    private String qrCodeBase64;
    private LocalDateTime timestamp;

    public Booking() {}

    public Booking(String id, String ticketNumber, String userId, String userName, String userEmail, String showId, String movieTitle, String moviePoster, String theaterName, String screenNumber, LocalDateTime showStartTime, List<String> seats, double totalAmount, String status, String qrCodeBase64, LocalDateTime timestamp) {
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.moviePoster = moviePoster;
        this.theaterName = theaterName;
        this.screenNumber = screenNumber;
        this.showStartTime = showStartTime;
        this.seats = seats != null ? seats : new ArrayList<>();
        this.totalAmount = totalAmount;
        this.status = status;
        this.qrCodeBase64 = qrCodeBase64;
        this.timestamp = timestamp;
    }

    public static BookingBuilder builder() {
        return new BookingBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getMoviePoster() { return moviePoster; }
    public void setMoviePoster(String moviePoster) { this.moviePoster = moviePoster; }

    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }

    public String getScreenNumber() { return screenNumber; }
    public void setScreenNumber(String screenNumber) { this.screenNumber = screenNumber; }

    public LocalDateTime getShowStartTime() { return showStartTime; }
    public void setShowStartTime(LocalDateTime showStartTime) { this.showStartTime = showStartTime; }

    public List<String> getSeats() { return seats; }
    public void setSeats(List<String> seats) { this.seats = seats; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static class BookingBuilder {
        private String id;
        private String ticketNumber;
        private String userId;
        private String userName;
        private String userEmail;
        private String showId;
        private String movieTitle;
        private String moviePoster;
        private String theaterName;
        private String screenNumber;
        private LocalDateTime showStartTime;
        private List<String> seats;
        private double totalAmount;
        private String status;
        private String qrCodeBase64;
        private LocalDateTime timestamp;

        public BookingBuilder id(String id) { this.id = id; return this; }
        public BookingBuilder ticketNumber(String num) { this.ticketNumber = num; return this; }
        public BookingBuilder userId(String userId) { this.userId = userId; return this; }
        public BookingBuilder userName(String name) { this.userName = name; return this; }
        public BookingBuilder userEmail(String email) { this.userEmail = email; return this; }
        public BookingBuilder showId(String showId) { this.showId = showId; return this; }
        public BookingBuilder movieTitle(String title) { this.movieTitle = title; return this; }
        public BookingBuilder moviePoster(String poster) { this.moviePoster = poster; return this; }
        public BookingBuilder theaterName(String name) { this.theaterName = name; return this; }
        public BookingBuilder screenNumber(String num) { this.screenNumber = num; return this; }
        public BookingBuilder showStartTime(LocalDateTime time) { this.showStartTime = time; return this; }
        public BookingBuilder seats(List<String> seats) { this.seats = seats; return this; }
        public BookingBuilder totalAmount(double amount) { this.totalAmount = amount; return this; }
        public BookingBuilder status(String status) { this.status = status; return this; }
        public BookingBuilder qrCodeBase64(String base64) { this.qrCodeBase64 = base64; return this; }
        public BookingBuilder timestamp(LocalDateTime time) { this.timestamp = time; return this; }

        public Booking build() {
            return new Booking(id, ticketNumber, userId, userName, userEmail, showId, movieTitle, moviePoster, theaterName, screenNumber, showStartTime, seats, totalAmount, status, qrCodeBase64, timestamp);
        }
    }
}
