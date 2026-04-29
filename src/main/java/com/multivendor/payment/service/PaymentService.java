package com.multivendor.payment.service;

import com.multivendor.payment.dto.PaymentDto;
import com.multivendor.payment.entity.Payment;
import com.multivendor.payment.repository.PaymentRepository;
import com.multivendor.vendor.entity.Vendor;
import com.multivendor.vendor.repository.VendorRepository;
import com.multivendor.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final VendorRepository vendorRepository;

    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getVendorPayments(Long vendorId, Pageable pageable) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));
        return paymentRepository.findByVendorOrderByCreatedAtDesc(vendor, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> getCustomerPayments(Long customerId, Pageable pageable) {
        return paymentRepository.findByCustomerId(customerId, pageable).map(this::mapToResponse);
    }

    private PaymentDto.Response mapToResponse(Payment p) {
        PaymentDto.Response r = new PaymentDto.Response();
        r.setId(p.getId());
        r.setBookingId(p.getBooking().getId());
        r.setBookingRef(p.getBooking().getBookingRef());
        r.setCustomerId(p.getCustomer().getId());
        r.setCustomerName(p.getCustomer().getName());
        r.setVendorId(p.getVendor().getId());
        r.setVendorName(p.getVendor().getName());
        r.setAmount(p.getAmount());
        r.setPlatformFee(p.getPlatformFee());
        r.setVendorPayout(p.getVendorPayout());
        r.setMethod(p.getMethod());
        r.setStatus(p.getStatus());
        r.setTxnId(p.getTxnId());
        r.setGatewayOrderId(p.getGatewayOrderId());
        r.setFailureReason(p.getFailureReason());
        r.setPaidAt(p.getPaidAt());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
