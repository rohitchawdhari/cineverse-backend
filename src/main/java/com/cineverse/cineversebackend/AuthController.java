package com.cineverse.cineversebackend;

import com.cineverse.cineversebackend.models.User;
import com.cineverse.cineversebackend.repositories.UserRepository;
import com.cineverse.cineversebackend.security.JwtService;
import com.cineverse.cineversebackend.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final List<String> ADMIN_EMAILS = List.of("admin@cineverse.com");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String password = request.get("password");

        if (name == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name, email and password are required"));
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is already registered"));
        }

        // Determine role (users cannot become admins through general signup)
        String role = "USER";
        if (ADMIN_EMAILS.contains(email.toLowerCase().trim())) {
            role = "ADMIN";
        }

        // Create user (auto-verified by default, no email verification needed)
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .isVerified(true)
                .verificationToken(null)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Registration successful. You can now login."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String requestedRole = request.get("role"); // ADMIN or USER

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }

        if (user.isSuspended()) {
            return ResponseEntity.status(403).body(Map.of("message", "Your account has been suspended by the administrator"));
        }

        // Role-based access validation for separate portal logins
        boolean isAdminEmail = ADMIN_EMAILS.contains(user.getEmail().toLowerCase().trim());
        if (isAdminEmail) {
            if (!user.getRole().equalsIgnoreCase("ADMIN")) {
                user.setRole("ADMIN");
                userRepository.save(user);
            }
        } else {
            if (user.getRole().equalsIgnoreCase("ADMIN")) {
                user.setRole("USER");
                userRepository.save(user);
            }
        }

        if (requestedRole != null) {
            if (requestedRole.equalsIgnoreCase("ADMIN")) {
                if (!isAdminEmail) {
                    return ResponseEntity.status(403).body(Map.of("message", "Unauthorized Admin Access"));
                }
            } else if (requestedRole.equalsIgnoreCase("USER")) {
                if (isAdminEmail) {
                    return ResponseEntity.status(403).body(Map.of("message", "Access Denied: Administrators must log in through the Admin Portal"));
                }
            }
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                )
        ));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired verification token"));
        }

        User user = userOpt.get();
        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Email verified successfully. You can now login."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            userRepository.save(user);

            emailService.sendPasswordResetEmail(email, resetToken);
        }

        // Return success even if email wasn't found for security reasons (don't leak registered emails)
        return ResponseEntity.ok(Map.of("message", "If the email is registered, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");

        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token and new password are required"));
        }

        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired reset token"));
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully. You can now login with your new password."));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || auth.getPrincipal() instanceof String) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        
        User principal = (User) auth.getPrincipal();
        Optional<User> userOpt = userRepository.findById(principal.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
        User user = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "wishlist", user.getWishlist() != null ? user.getWishlist() : new ArrayList<>()
        ));
    }

    @GetMapping("/test")
    public String test() {
        return "Backend API Connected Successfully";
    }
}