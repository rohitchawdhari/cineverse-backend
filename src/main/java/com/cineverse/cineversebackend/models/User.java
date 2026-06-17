package com.cineverse.cineversebackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private String role; // "ADMIN" or "USER"
    private boolean isVerified;
    private String verificationToken;
    private String resetToken;
    private List<String> wishlist = new ArrayList<>();
    private boolean isSuspended = false;

    public User() {}

    public User(String id, String name, String email, String password, String role, boolean isVerified, String verificationToken, String resetToken, List<String> wishlist, boolean isSuspended) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isVerified = isVerified;
        this.verificationToken = verificationToken;
        this.resetToken = resetToken;
        this.wishlist = wishlist != null ? wishlist : new ArrayList<>();
        this.isSuspended = isSuspended;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public List<String> getWishlist() { return wishlist; }
    public void setWishlist(List<String> wishlist) { this.wishlist = wishlist; }

    public boolean isSuspended() { return isSuspended; }
    public void setSuspended(boolean suspended) { isSuspended = suspended; }

    public static class UserBuilder {
        private String id;
        private String name;
        private String email;
        private String password;
        private String role;
        private boolean isVerified;
        private String verificationToken;
        private String resetToken;
        private List<String> wishlist = new ArrayList<>();
        private boolean isSuspended;

        public UserBuilder id(String id) { this.id = id; return this; }
        public UserBuilder name(String name) { this.name = name; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder role(String role) { this.role = role; return this; }
        public UserBuilder isVerified(boolean isVerified) { this.isVerified = isVerified; return this; }
        public UserBuilder verificationToken(String token) { this.verificationToken = token; return this; }
        public UserBuilder resetToken(String token) { this.resetToken = token; return this; }
        public UserBuilder wishlist(List<String> wishlist) { this.wishlist = wishlist; return this; }
        public UserBuilder isSuspended(boolean isSuspended) { this.isSuspended = isSuspended; return this; }

        public User build() {
            return new User(id, name, email, password, role, isVerified, verificationToken, resetToken, wishlist, isSuspended);
        }
    }
}
