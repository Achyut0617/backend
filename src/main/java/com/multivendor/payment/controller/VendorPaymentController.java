package com.multivendor.payment.controller;

import com.multivendor.common.response.ApiResponse;
import com.multivendor.config.security.JwtAuthenticationToken;
import com.multivendor.payment.dto.PaymentDto;
import com.multivendor.payment.dto.SettlementDto;
import com.multivendor.payment.service.PaymentService;
import com.multivendor.payment.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
public class VendorPaymentController {

    private final PaymentService paymentService;
    private final SettlementService settlementService;

    @GetMapping("/payments")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<PaymentDto.Response>>> getMyPayments(
            JwtAuthenticationToken auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(paymentService.getVendorPayments(auth.getUserId(), pageable), "Payments retrieved"));
    }

    @GetMapping("/settlements")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<SettlementDto.Response>>> getMySettlements(
            JwtAuthenticationToken auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(settlementService.getVendorSettlements(auth.getUserId(), pageable), "Settlements retrieved"));
    }
}
