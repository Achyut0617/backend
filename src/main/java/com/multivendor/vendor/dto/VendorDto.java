package com.multivendor.vendor.dto;

import com.multivendor.common.enums.VendorStatus;
import com.multivendor.common.enums.VerificationStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VendorDto {

    @Data
    public static class RegisterRequest {
        @NotBlank private String name;
        @NotBlank @Email private String email;
        @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number")
        private String phone;
        @NotBlank @Size(min = 8) private String password;
        private String businessName;
        private String businessDescription;
        @NotBlank private String city;
        @NotBlank private String state;
        private String pincode;
        private String address;
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
        private String businessName;
        private String city;
        private VendorStatus status;
        private BigDecimal rating;
        private boolean isKycVerified;
        private String token;
        private String refreshToken;
    }

    @Data
    public static class ProfileResponse {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String businessName;
        private String businessDescription;
        private String city;
        private String state;
        private String pincode;
        private String address;
        private String profilePic;
        private VendorStatus status;
        private BigDecimal rating;
        private Integer totalReviews;
        private Integer totalBookings;
        private BigDecimal totalEarnings;
        private BigDecimal commissionRate;
        private boolean isAvailable;
        private boolean isKycVerified;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UpdateProfileRequest {
        private String name;
        private String phone;
        private String businessName;
        private String businessDescription;
        private String city;
        private String state;
        private String pincode;
        private String address;
    }

    @Data
    public static class AvailabilityRequest {
        private boolean isAvailable;
    }

    @Data
    public static class KycSubmitRequest {
        @NotBlank private String aadhaarNumber;
        @NotBlank private String panNumber;
        @NotBlank private String bankAccountNumber;
        @NotBlank private String bankIfsc;
        @NotBlank private String bankName;
    }

    @Data
    public static class EarningsDashboard {
        private BigDecimal totalEarnings;
        private BigDecimal pendingSettlement;
        private BigDecimal thisMonthEarnings;
        private long completedJobs;
        private long pendingJobs;
        private BigDecimal avgJobValue;
    }

    @Data
    public static class Summary {
        private Long id;
        private String name;
        private String businessName;
        private String city;
        private VendorStatus status;
        private BigDecimal rating;
        private Integer totalReviews;
        private boolean isAvailable;
        private String profilePic;
        private LocalDateTime createdAt;
    }
}
