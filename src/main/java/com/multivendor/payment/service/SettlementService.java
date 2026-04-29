package com.multivendor.payment.service;

import com.multivendor.common.enums.SettlementStatus;
import com.multivendor.common.exception.BusinessException;
import com.multivendor.common.exception.ResourceNotFoundException;
import com.multivendor.payment.dto.SettlementDto;
import com.multivendor.payment.entity.SettlementHistory;
import com.multivendor.payment.repository.SettlementHistoryRepository;
import com.multivendor.vendor.entity.Vendor;
import com.multivendor.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementHistoryRepository settlementRepository;
    private final VendorRepository vendorRepository;

    @Transactional(readOnly = true)
    public Page<SettlementDto.Response> getAllSettlements(Pageable pageable) {
        return settlementRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<SettlementDto.Response> getVendorSettlements(Long vendorId, Pageable pageable) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));
        return settlementRepository.findByVendorOrderByCreatedAtDesc(vendor, pageable).map(this::mapToResponse);
    }

    @Transactional
    public SettlementDto.Response processSettlement(Long adminId, Long settlementId, SettlementDto.ProcessRequest request) {
        SettlementHistory settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", settlementId));

        if (settlement.getStatus() == SettlementStatus.SETTLED) {
            throw new BusinessException("Settlement is already completed.");
        }

        settlement.setStatus(SettlementStatus.SETTLED);
        settlement.setUtrNumber(request.getUtrNumber());
        settlement.setRemarks(request.getRemarks());
        settlement.setSettledBy(adminId);
        settlement.setSettledAt(LocalDateTime.now());

        return mapToResponse(settlementRepository.save(settlement));
    }

    private SettlementDto.Response mapToResponse(SettlementHistory s) {
        SettlementDto.Response r = new SettlementDto.Response();
        r.setId(s.getId());
        r.setVendorId(s.getVendor().getId());
        r.setVendorName(s.getVendor().getName());
        r.setAmount(s.getAmount());
        r.setPeriodFrom(s.getPeriodFrom());
        r.setPeriodTo(s.getPeriodTo());
        r.setStatus(s.getStatus());
        r.setUtrNumber(s.getUtrNumber());
        r.setSettledBy(s.getSettledBy());
        r.setSettledAt(s.getSettledAt());
        r.setRemarks(s.getRemarks());
        r.setCreatedAt(s.getCreatedAt());
        return r;
    }
}
