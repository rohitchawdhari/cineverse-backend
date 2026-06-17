package com.cineverse.cineversebackend.services;

import com.cineverse.cineversebackend.models.Booking;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@cineverse.com}")
    private String fromEmail;

    public void sendVerificationEmail(String email, String token) {
        String verifyUrl = "http://localhost:5173/verify-email?token=" + token;
        String htmlContent = "<h3>Welcome to Cineverse!</h3>"
                + "<p>Please verify your email address by clicking the link below:</p>"
                + "<p><a href=\"" + verifyUrl + "\" style=\"background-color:#3f51b5;color:#fff;padding:8px 16px;text-decoration:none;border-radius:4px;\">Verify Email</a></p>"
                + "<p>Or copy and paste this URL into your browser:</p>"
                + "<p>" + verifyUrl + "</p>";
        
        sendHtmlEmail(email, "Verify Your Cineverse Account", htmlContent, null, null);
    }

    public void sendPasswordResetEmail(String email, String token) {
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;
        String htmlContent = "<h3>Cineverse Password Reset Request</h3>"
                + "<p>You requested a password reset. Click the link below to set a new password:</p>"
                + "<p><a href=\"" + resetUrl + "\" style=\"background-color:#f44336;color:#fff;padding:8px 16px;text-decoration:none;border-radius:4px;\">Reset Password</a></p>"
                + "<p>Or copy and paste this URL into your browser:</p>"
                + "<p>" + resetUrl + "</p>"
                + "<p>If you did not request this, you can ignore this email.</p>";
        
        sendHtmlEmail(email, "Reset Your Cineverse Password", htmlContent, null, null);
    }

    public void sendBookingConfirmationEmail(String email, Booking booking, byte[] pdfBytes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String htmlContent = "<div style=\"font-family:sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #ddd;border-radius:8px;\">"
                + "<h2 style=\"color:#3f51b5;text-align:center;\">Cineverse Ticket Confirmed!</h2>"
                + "<p>Hi " + booking.getUserName() + ",</p>"
                + "<p>Thank you for booking with Cineverse. Your transaction was successful. Here are your booking details:</p>"
                + "<table style=\"width:100%;border-collapse:collapse;margin:20px 0;\">"
                + "<tr><td style=\"padding:8px;border-bottom:1px solid #ddd;font-weight:bold;\">Booking Reference:</td><td style=\"padding:8px;border-bottom:1px solid #ddd;\">" + booking.getId() + "</td></tr>"
                + "<tr><td style=\"padding:8px;border-bottom:1px solid #ddd;font-weight:bold;\">Movie Title:</td><td style=\"padding:8px;border-bottom:1px solid #ddd;\">" + booking.getMovieTitle() + "</td></tr>"
                + "<tr><td style=\"padding:8px;border-bottom:1px solid #ddd;font-weight:bold;\">Theater:</td><td style=\"padding:8px;border-bottom:1px solid #ddd;\">" + booking.getTheaterName() + "</td></tr>"
                + "<tr><td style=\"padding:8px;border-bottom:1px solid #ddd;font-weight:bold;\">Screen:</td><td style=\"padding:8px;border-bottom:1px solid #ddd;\">" + booking.getScreenNumber() + "</td></tr>"
                + "<tr><td style=\"padding:8px;border-bottom:1px solid #ddd;font-weight:bold;\">Show Time:</td><td style=\"padding:8px;border-bottom:1px solid #ddd;\">" + booking.getShowStartTime().format(formatter) + "</td></tr>"
                + "<tr><td style=\"padding:8px;border-bottom:1px solid #ddd;font-weight:bold;\">Seats:</td><td style=\"padding:8px;border-bottom:1px solid #ddd;\">" + String.join(", ", booking.getSeats()) + "</td></tr>"
                + "<tr><td style=\"padding:8px;border-bottom:1px solid #ddd;font-weight:bold;\">Amount Paid:</td><td style=\"padding:8px;border-bottom:1px solid #ddd;\">INR " + booking.getTotalAmount() + "</td></tr>"
                + "</table>"
                + "<p style=\"text-align:center;font-size:12px;color:#888;\">We have attached your official PDF ticket receipt to this email.</p>"
                + "<p style=\"text-align:center;\">Happy viewing!<br>Team Cineverse</p>"
                + "</div>";

        sendHtmlEmail(email, "Cineverse Booking Confirmation - " + booking.getId(), htmlContent, "ticket_" + booking.getId() + ".pdf", pdfBytes);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent, String attachmentName, byte[] attachmentBytes) {
        if (mailSender == null) {
            System.out.println("[Email Service Simulation] Mail sender is not configured. Sending simulated email:");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Content: " + htmlContent);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            if (attachmentName != null && attachmentBytes != null) {
                helper.addAttachment(attachmentName, new ByteArrayResource(attachmentBytes));
            }
            
            mailSender.send(message);
            System.out.println("[Email Service] Successfully sent email to " + to);
        } catch (Exception e) {
            System.err.println("[Email Service] Failed to send email to " + to + ": " + e.getMessage());
            // Fail gracefully to prevent blocking user actions if local SMTP settings are offline
        }
    }
}
