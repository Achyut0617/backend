package com.multivendor.admin.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AdminDto {

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class LoginResponse {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String token;
        private String refreshToken;
    }

    @Data
    public static class DashboardStats {
        private long totalVendors;
        private long pendingVendors;
        private long approvedVendors;
        private long totalCustomers;
        private long totalBookings;
        private long pendingBookings;
        private long completedBookings;
        private long openComplaints;
        private double totalRevenue;
        private double platformRevenue;
        private double monthlyRevenue;
        private long todayBookings;
    }

    @Data
    public static class VendorActionRequest {
        @NotBlank
        private String action; // APPROVE, REJECT, SUSPEND
        private String reason;
    }

    @Data
    public static class UpdateCommissionRequest {
        private java.math.BigDecimal commissionRate;
    }
}
