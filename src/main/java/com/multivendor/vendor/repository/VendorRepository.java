package com.multivendor.vendor.repository;

import com.multivendor.common.enums.VendorStatus;
import com.multivendor.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Page<Vendor> findByStatus(VendorStatus status, Pageable pageable);
    Page<Vendor> findByCityIgnoreCase(String city, Pageable pageable);

    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.status = :status")
    long countByStatus(VendorStatus status);

    @Query("SELECT v FROM Vendor v WHERE v.status = 'APPROVED' AND " +
           "(LOWER(v.city) = LOWER(:city) OR :city IS NULL)")
    Page<Vendor> findApprovedVendorsByCity(String city, Pageable pageable);
}
