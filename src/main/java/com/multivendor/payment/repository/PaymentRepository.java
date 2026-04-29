package com.multivendor.payment.repository;

import com.multivendor.payment.entity.Payment;
import com.multivendor.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);
    Page<Payment> findByVendorOrderByCreatedAtDesc(Vendor vendor, Pageable pageable);
    Page<Payment> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.vendorPayout), 0) FROM Payment p WHERE p.vendor = :vendor AND p.status = 'SUCCESS'")
    BigDecimal sumVendorEarnings(Vendor vendor);

    @Query("SELECT COALESCE(SUM(p.platformFee), 0) FROM Payment p WHERE p.status = 'SUCCESS'")
    BigDecimal sumTotalPlatformRevenue();
}
