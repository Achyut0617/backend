package com.multivendor.booking.dto;

import com.multivendor.common.enums.BookingStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingDto {

    @Data
    public static class CreateRequest {
        @NotNull private Long serviceId;
        @NotNull @Future private LocalDateTime scheduledAt;
        @NotBlank private String serviceAddress;
        @NotBlank private String city;
        private String pincode;
        private String notes;
    }

    @Data
    public static class Response {
        private Long id;
        private String bookingRef;
        private BookingStatus status;
        private LocalDateTime scheduledAt;
        private String serviceAddress;
        private String city;
        private String notes;
        private BigDecimal totalAmount;
        private BigDecimal platformFee;
        private BigDecimal vendorPayout;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String rejectionReason;
        private String cancellationReason;
        private String vendorNotes;
        private LocalDateTime createdAt;

        // Nested summaries
        private CustomerSummary customer;
        private VendorSummary vendor;
        private ServiceSummary service;
    }

    @Data
    public static class CustomerSummary {
        private Long id;
        private String name;
        private String phone;
        private String email;
    }

    @Data
    public static class VendorSummary {
        private Long id;
        private String name;
        private String businessName;
        private String phone;
        private String email;
    }

    @Data
    public static class ServiceSummary {
        private Long id;
        private String title;
        private String categoryName;
        private BigDecimal price;
    }

    @Data
    public static class VendorActionRequest {
        @NotNull private String action; // ACCEPT, REJECT, START, COMPLETE
        private String reason;
        private String vendorNotes;
    }

    @Data
    public static class CancelRequest {
        @NotBlank private String reason;
    }

    @Data
    public static class StatusUpdate {
        private Long bookingId;
        private BookingStatus newStatus;
        private String message;
    }
}
