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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;
    private final SettlementService settlementService;

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentDto.Response>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(paymentService.getAllPayments(pageable), "Payments retrieved"));
    }

    @GetMapping("/settlements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<SettlementDto.Response>>> getAllSettlements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(settlementService.getAllSettlements(pageable), "Settlements retrieved"));
    }

    @PostMapping("/settlements/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SettlementDto.Response>> processSettlement(
            JwtAuthenticationToken auth,
            @PathVariable Long id,
            @RequestBody SettlementDto.ProcessRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                settlementService.processSettlement(auth.getUserId(), id, request), "Settlement processed"));
    }
}
