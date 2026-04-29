package com.multivendor.payment.dto;

import com.multivendor.common.enums.PaymentMethod;
import com.multivendor.common.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    @Data
    public static class Response {
        private Long id;
        private Long bookingId;
        private String bookingRef;
        private Long customerId;
        private String customerName;
        private Long vendorId;
        private String vendorName;
        private BigDecimal amount;
        private BigDecimal platformFee;
        private BigDecimal vendorPayout;
        private PaymentMethod method;
        private PaymentStatus status;
        private String txnId;
        private String gatewayOrderId;
        private String failureReason;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;
    }
}
