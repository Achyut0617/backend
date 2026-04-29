package com.multivendor.customer.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

public class CustomerDto {

    @Data
    public static class RegisterRequest {
        @NotBlank private String name;
        @NotBlank @Email private String email;
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number")
        private String phone;
        @NotBlank @Size(min = 8) private String password;
        private String city;
        private String state;
    }

    @Data
    public static class LoginRequest {
        @NotBlank @Email private String email;
        @NotBlank private String password;
    }

    @Data
    public static class LoginResponse {
        private Long id;
        private String name;
        private String email;
        private String city;
        private String token;
        private String refreshToken;
    }

    @Data
    public static class ProfileResponse {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String profilePic;
        private boolean isVerified;
        private Integer totalBookings;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UpdateProfileRequest {
        private String name;
        private String phone;
        private String address;
        private String city;
        private String state;
        private String pincode;
    }

    @Data
    public static class Summary {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String city;
        private boolean isActive;
        private Integer totalBookings;
        private LocalDateTime createdAt;
    }
}
