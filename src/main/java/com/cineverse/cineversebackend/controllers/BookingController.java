package com.cineverse.cineversebackend.controllers;

import com.cineverse.cineversebackend.models.Booking;
import com.cineverse.cineversebackend.models.Movie;
import com.cineverse.cineversebackend.models.Payment;
import com.cineverse.cineversebackend.models.ShowTime;
import com.cineverse.cineversebackend.models.User;
import com.cineverse.cineversebackend.models.Coupon;
import com.cineverse.cineversebackend.models.Notification;
import com.cineverse.cineversebackend.repositories.BookingRepository;
import com.cineverse.cineversebackend.repositories.MovieRepository;
import com.cineverse.cineversebackend.repositories.PaymentRepository;
import com.cineverse.cineversebackend.repositories.ShowRepository;
import com.cineverse.cineversebackend.repositories.CouponRepository;
import com.cineverse.cineversebackend.repositories.NotificationRepository;
import com.cineverse.cineversebackend.services.EmailService;
import com.cineverse.cineversebackend.services.PdfService;
import com.cineverse.cineversebackend.services.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EmailService emailService;

    private String generateBookingId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("MOV-2026-");
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateTicketNumber() {
        String chars = "0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("TKT-2026-");
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // --- VALIDATE COUPON ENDPOINT ---
    @GetMapping("/validate-coupon")
    public ResponseEntity<?> validateCoupon(@RequestParam("code") String code,
                                            @RequestParam("amount") double amount) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(code.toUpperCase());
        if (couponOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Coupon code not found"));
        }

        Coupon coupon = couponOpt.get();
        if (!coupon.isActive()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Coupon is inactive"));
        }

        if (amount < coupon.getMinPurchaseAmount()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Minimum purchase amount of ₹" + coupon.getMinPurchaseAmount() + " required"));
        }

        double discount = 0;
        if (coupon.getDiscountPercentage() > 0) {
            discount = amount * (coupon.getDiscountPercentage() / 100);
        } else {
            discount = coupon.getDiscountAmount();
        }

        double finalAmount = Math.max(0, amount - discount);

        return ResponseEntity.ok(Map.of(
                "code", coupon.getCode(),
                "discount", discount,
                "finalAmount", finalAmount,
                "message", "Coupon applied successfully"
        ));
    }

    // --- CREATE BOOKING ---
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> request,
                                           @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User must be authenticated to book tickets"));
        }

        String showId = (String) request.get("showId");
        @SuppressWarnings("unchecked")
        List<String> seats = (List<String>) request.get("seats");
        String paymentMethod = (String) request.get("paymentMethod");
        double totalAmount = Double.parseDouble(request.get("totalAmount").toString());
        String couponCode = (String) request.get("couponCode");

        if (showId == null || seats == null || seats.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Show ID and seats are required"));
        }

        // Retrieve ShowTime
        Optional<ShowTime> showOpt = showRepository.findById(showId);
        if (showOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Show not found"));
        }

        ShowTime show = showOpt.get();

        // Check availability
        List<String> alreadyBooked = new ArrayList<>();
        for (String seat : seats) {
            if (show.getBookedSeats().contains(seat)) {
                alreadyBooked.add(seat);
            }
        }

        if (!alreadyBooked.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "The following seats are already booked: " + String.join(", ", alreadyBooked)
            ));
        }

        // Calculate coupon discount if applicable
        double discount = 0;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            Optional<Coupon> couponOpt = couponRepository.findByCode(couponCode.toUpperCase());
            if (couponOpt.isPresent()) {
                Coupon coupon = couponOpt.get();
                if (coupon.isActive() && totalAmount >= coupon.getMinPurchaseAmount()) {
                    if (coupon.getDiscountPercentage() > 0) {
                        discount = totalAmount * (coupon.getDiscountPercentage() / 100);
                    } else {
                        discount = coupon.getDiscountAmount();
                    }
                }
            }
        }
        double finalAmount = Math.max(0, totalAmount - discount);

        // Book seats
        show.getBookedSeats().addAll(seats);
        showRepository.save(show);

        // Fetch movie poster path for dashboard view
        String moviePoster = "";
        Optional<Movie> movieOpt = movieRepository.findById(show.getMovieId());
        if (movieOpt.isPresent()) {
            moviePoster = movieOpt.get().getPoster();
        }

        // Generate identifiers
        String bookingId = generateBookingId();
        while (bookingRepository.existsById(bookingId)) {
            bookingId = generateBookingId();
        }

        String ticketNumber = generateTicketNumber();

        // Create booking entity
        Booking booking = Booking.builder()
                .id(bookingId)
                .ticketNumber(ticketNumber)
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .showId(showId)
                .movieTitle(show.getMovieTitle())
                .moviePoster(moviePoster)
                .theaterName(show.getTheaterName())
                .screenNumber(show.getScreenNumber())
                .showStartTime(show.getStartTime())
                .seats(seats)
                .totalAmount(finalAmount)
                .status("CONFIRMED")
                .timestamp(LocalDateTime.now())
                .build();

        // Generate QR code data
        String qrContent = String.format("Booking ID: %s\nTicket ID: %s\nMovie: %s\nTheater: %s\nScreen: %s\nSeats: %s\nShowtime: %s",
                bookingId, ticketNumber, show.getMovieTitle(), show.getTheaterName(), show.getScreenNumber(),
                String.join(", ", seats), show.getStartTime().toString());
        String qrBase64 = qrCodeService.generateQrCodeBase64(qrContent, 200, 200);
        booking.setQrCodeBase64(qrBase64);

        // Save booking
        bookingRepository.save(booking);

        // Create Payment log
        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .amount(finalAmount)
                .paymentMethod(paymentMethod != null ? paymentMethod : "CARD")
                .status("SUCCESS")
                .timestamp(LocalDateTime.now())
                .transactionId("TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase())
                .build();
        paymentRepository.save(payment);

        // Save user notification
        Notification notification = new Notification(
                null,
                user.getId(),
                "Booking Confirmed 🎟️",
                "Your show of " + show.getMovieTitle() + " is confirmed for " + show.getStartTime() + "! Seats: " + String.join(", ", seats) + ". Reference: " + bookingId,
                LocalDateTime.now(),
                false,
                "BOOKING"
        );
        notificationRepository.save(notification);

        // Generate PDF ticket
        byte[] pdfBytes = pdfService.generateTicketPdf(booking);

        // Send Email confirmation
        emailService.sendBookingConfirmationEmail(user.getEmail(), booking, pdfBytes);

        return ResponseEntity.ok(booking);
    }

    // --- GET USER BOOKINGS ---
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User must be authenticated"));
        }
        
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());
        bookings.sort((b1, b2) -> b2.getTimestamp().compareTo(b1.getTimestamp()));
        return ResponseEntity.ok(bookings);
    }

    // --- DOWNLOAD TICKET PDF ---
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable("id") String id,
                                                    @AuthenticationPrincipal User user) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Booking booking = bookingOpt.get();

        if (user == null || (!user.getRole().equals("ADMIN") && !booking.getUserId().equals(user.getId()))) {
            return ResponseEntity.status(403).build();
        }

        byte[] pdfBytes = pdfService.generateTicketPdf(booking);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // --- CANCEL BOOKING ---
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable("id") String id,
                                           @AuthenticationPrincipal User user) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Booking booking = bookingOpt.get();

        if (user == null || (!user.getRole().equals("ADMIN") && !booking.getUserId().equals(user.getId()))) {
            return ResponseEntity.status(403).body(Map.of("message", "Not authorized"));
        }

        if (booking.getStatus().equals("CANCELLED")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Booking is already cancelled"));
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        // Release seats
        Optional<ShowTime> showOpt = showRepository.findById(booking.getShowId());
        if (showOpt.isPresent()) {
            ShowTime show = showOpt.get();
            show.getBookedSeats().removeAll(booking.getSeats());
            showRepository.save(show);
        }

        // Add Notification
        Notification notification = new Notification(
                null,
                booking.getUserId(),
                "Booking Cancelled ❌",
                "Your booking " + id + " for " + booking.getMovieTitle() + " has been successfully cancelled.",
                LocalDateTime.now(),
                false,
                "BOOKING"
        );
        notificationRepository.save(notification);

        return ResponseEntity.ok(booking);
    }

    // --- GET USER NOTIFICATIONS ---
    @GetMapping("/notifications")
    public ResponseEntity<?> getMyNotifications(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User must be authenticated"));
        }
        List<Notification> list = notificationRepository.findByUserIdOrderByTimestampDesc(user.getId());
        return ResponseEntity.ok(list);
    }

    // --- MARK NOTIFICATION AS READ ---
    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable("id") String id,
                                                    @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User must be authenticated"));
        }
        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Notification n = opt.get();
        if (!n.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }
        n.setRead(true);
        notificationRepository.save(n);
        return ResponseEntity.ok(n);
    }

    // --- MARK ALL NOTIFICATIONS AS READ ---
    @PostMapping("/notifications/read-all")
    public ResponseEntity<?> markAllNotificationsAsRead(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User must be authenticated"));
        }
        List<Notification> list = notificationRepository.findByUserIdOrderByTimestampDesc(user.getId());
        for (Notification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }
}
