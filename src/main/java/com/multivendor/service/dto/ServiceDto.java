package com.multivendor.service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceDto {

    @Data
    public static class CreateRequest {
        @NotNull private Long categoryId;
        @NotBlank private String title;
        private String description;
        @NotNull @DecimalMin("0.01") private BigDecimal price;
        private String unit;
        private Integer durationMins;
        private String areaServed;
    }

    @Data
    public static class UpdateRequest {
        private String title;
        private String description;
        private BigDecimal price;
        private String unit;
        private Integer durationMins;
        private String areaServed;
        private Boolean isAvailable;
    }

    @Data
    public static class Response {
        private Long id;
        private Long vendorId;
        private String vendorName;
        private String vendorCity;
        private BigDecimal vendorRating;
        private Long categoryId;
        private String categoryName;
        private String title;
        private String description;
        private BigDecimal price;
        private String unit;
        private Integer durationMins;
        private String areaServed;
        private String imageUrl;
        private boolean isAvailable;
        private Integer totalBookings;
        private LocalDateTime createdAt;
    }

    @Data
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
        private String icon;
        private String imageUrl;
        private boolean isActive;
    }
}
