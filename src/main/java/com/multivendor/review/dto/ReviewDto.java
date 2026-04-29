package com.multivendor.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

public class ReviewDto {

    @Data
    public static class CreateRequest {
        @NotNull private Long bookingId;
        @NotNull @Min(1) @Max(5) private Integer rating;
        private String comment;
    }

    @Data
    public static class ReplyRequest {
        @NotBlank private String reply;
    }

    @Data
    public static class Response {
        private Long id;
        private Long bookingId;
        private String bookingRef;
        private String customerName;
        private String vendorName;
        private Integer rating;
        private String comment;
        private String reply;
        private LocalDateTime replyAt;
        private boolean isVisible;
        private LocalDateTime createdAt;
    }
}
