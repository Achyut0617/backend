package com.multivendor.payment.dto;

import com.multivendor.common.enums.SettlementStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SettlementDto {

    @Data
    public static class Response {
        private Long id;
        private Long vendorId;
        private String vendorName;
        private BigDecimal amount;
        private LocalDate periodFrom;
        private LocalDate periodTo;
        private SettlementStatus status;
        private String utrNumber;
        private Long settledBy;
        private LocalDateTime settledAt;
        private String remarks;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ProcessRequest {
        private String utrNumber;
        private String remarks;
    }
}
