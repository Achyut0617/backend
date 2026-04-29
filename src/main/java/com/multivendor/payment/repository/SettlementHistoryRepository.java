package com.multivendor.payment.repository;

import com.multivendor.payment.entity.SettlementHistory;
import com.multivendor.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementHistoryRepository extends JpaRepository<SettlementHistory, Long> {
    Page<SettlementHistory> findByVendorOrderByCreatedAtDesc(Vendor vendor, Pageable pageable);
}
