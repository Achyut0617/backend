package com.multivendor.vendor.repository;

import com.multivendor.common.enums.VerificationStatus;
import com.multivendor.vendor.entity.Vendor;
import com.multivendor.vendor.entity.VendorVerification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorVerificationRepository extends JpaRepository<VendorVerification, Long> {
    Optional<VendorVerification> findByVendor(Vendor vendor);
    Page<VendorVerification> findByVerificationStatus(VerificationStatus status, Pageable pageable);
}
