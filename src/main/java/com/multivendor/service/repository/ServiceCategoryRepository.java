package com.multivendor.service.repository;

import com.multivendor.service.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    List<ServiceCategory> findByIsActiveTrueOrderBySortOrder();
    boolean existsByNameIgnoreCase(String name);
}
