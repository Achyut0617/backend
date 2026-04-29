package com.multivendor.complaint.repository;

import com.multivendor.common.enums.ComplaintStatus;
import com.multivendor.complaint.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Page<Complaint> findByRaisedByIdAndRaisedByType(Long id, com.multivendor.common.enums.UserType type, Pageable pageable);
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);
    long countByStatus(ComplaintStatus status);
}
