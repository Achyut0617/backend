package com.multivendor.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

public class ComplaintDto {

    @Data
    public static class CreateRequest {
        private Long bookingId;
        @NotBlank private String title;
        @NotBlank private String description;
    }

    @Data
    public static class ResolveRequest {
        @NotBlank private String resolutionNotes;
        @NotBlank private String status; // RESOLVED, CLOSED
    }

    @Data
    public static class Response {
        private Long id;
        private Long bookingId;
        private String bookingRef;
        private String raisedByType;
        private Long raisedById;
        private String raisedByName;
        private String title;
        private String description;
        private String status;
        private String adminNotes;
        private String resolutionNotes;
        private LocalDateTime createdAt;
        private LocalDateTime resolvedAt;
    }
}
