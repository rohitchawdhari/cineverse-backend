package com.cineverse.cineversebackend.controllers;

import com.cineverse.cineversebackend.models.Booking;
import com.cineverse.cineversebackend.models.Payment;
import com.cineverse.cineversebackend.models.User;
import com.cineverse.cineversebackend.models.Coupon;
import com.cineverse.cineversebackend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CouponRepository couponRepository;

    @GetMapping("/metrics")
    public ResponseEntity<?> getAdminMetrics() {
        long totalMovies = movieRepository.count();
        long totalTheaters = theaterRepository.count();
        long totalUsers = userRepository.count();
        
        List<com.cineverse.cineversebackend.models.Theater> theaters = theaterRepository.findAll();
        long totalScreens = theaters.stream()
                .filter(t -> t.getScreens() != null)
                .mapToLong(t -> t.getScreens().size())
                .sum();

        List<Booking> bookings = bookingRepository.findAll();
        long totalBookings = bookings.size();

        double totalRevenue = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Booking::getTotalAmount)
                .sum();

        // Calculate popular movies
        Map<String, Integer> movieTicketCounts = new HashMap<>();
        for (Booking booking : bookings) {
            if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                String movieTitle = booking.getMovieTitle();
                int ticketsCount = booking.getSeats().size();
                movieTicketCounts.put(movieTitle, movieTicketCounts.getOrDefault(movieTitle, 0) + ticketsCount);
            }
        }

        List<Map<String, Object>> popularMovies = movieTicketCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("movieTitle", e.getKey());
                    map.put("ticketsBooked", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        // Group by day for past 7 days
        Map<String, Double> dailyRevenue = new LinkedHashMap<>();
        // Group by month for past 6 months
        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
        
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            dailyRevenue.put(today.minusDays(i).toString(), 0.0);
        }

        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            monthlyRevenue.put(currentMonth.minusMonths(i).toString(), 0.0);
        }

        for (Booking booking : bookings) {
            if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                LocalDateTime ts = booking.getTimestamp();
                if (ts != null) {
                    String dateKey = ts.toLocalDate().toString();
                    if (dailyRevenue.containsKey(dateKey)) {
                        dailyRevenue.put(dateKey, dailyRevenue.get(dateKey) + booking.getTotalAmount());
                    }
                    
                    String monthKey = java.time.YearMonth.from(ts).toString();
                    if (monthlyRevenue.containsKey(monthKey)) {
                        monthlyRevenue.put(monthKey, monthlyRevenue.get(monthKey) + booking.getTotalAmount());
                    }
                }
            }
        }

        // Get recent bookings (last 5)
        List<Booking> recentBookings = bookings.stream()
                .sorted((b1, b2) -> b2.getTimestamp().compareTo(b1.getTimestamp()))
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalMovies", totalMovies);
        metrics.put("totalTheaters", totalTheaters);
        metrics.put("totalScreens", totalScreens);
        metrics.put("totalUsers", totalUsers);
        metrics.put("totalBookings", totalBookings);
        metrics.put("totalRevenue", totalRevenue);
        metrics.put("popularMovies", popularMovies);
        metrics.put("recentBookings", recentBookings);
        metrics.put("dailyRevenue", dailyRevenue);
        metrics.put("monthlyRevenue", monthlyRevenue);
        metrics.put("seatOccupancy", Map.of("Silver", 52, "Gold", 48, "Platinum", 61, "VIP", 72));

        return ResponseEntity.ok(metrics);
    }

    // --- USER MANAGEMENT ---
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        for (User u : users) {
            u.setPassword(null);
        }
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable("userId") String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        user.setSuspended(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User suspended successfully", "user", user));
    }

    @PostMapping("/users/{userId}/unsuspend")
    public ResponseEntity<?> unsuspendUser(@PathVariable("userId") String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        user.setSuspended(false);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User unsuspended successfully", "user", user));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") String userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // --- COUPON MANAGEMENT ---
    @GetMapping("/coupons")
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @PostMapping("/coupons")
    public ResponseEntity<?> createCoupon(@RequestBody Coupon coupon) {
        if (coupon.getCode() == null || coupon.getCode().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Coupon code is required"));
        }
        coupon.setCode(coupon.getCode().toUpperCase());
        Coupon saved = couponRepository.save(coupon);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable("id") String id, @RequestBody Coupon couponDetails) {
        Optional<Coupon> couponOpt = couponRepository.findById(id);
        if (couponOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Coupon coupon = couponOpt.get();
        coupon.setCode(couponDetails.getCode().toUpperCase());
        coupon.setDiscountAmount(couponDetails.getDiscountAmount());
        coupon.setDiscountPercentage(couponDetails.getDiscountPercentage());
        coupon.setMinPurchaseAmount(couponDetails.getMinPurchaseAmount());
        coupon.setExpiryDate(couponDetails.getExpiryDate());
        coupon.setActive(couponDetails.isActive());
        
        Coupon updated = couponRepository.save(coupon);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable("id") String id) {
        if (!couponRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        couponRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Coupon deleted successfully"));
    }

    // --- BOOKING MANAGEMENT ---
    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        List<Booking> list = bookingRepository.findAll();
        list.sort((b1, b2) -> b2.getTimestamp().compareTo(b1.getTimestamp()));
        return list;
    }
}
