package com.multivendor.admin.repository;

import com.multivendor.admin.entity.Admin;
import com.multivendor.admin.entity.AdminAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminActionRepository extends JpaRepository<AdminAction, Long> {
    Page<AdminAction> findByAdmin(Admin admin, Pageable pageable);
    Page<AdminAction> findByActionType(String actionType, Pageable pageable);
}
