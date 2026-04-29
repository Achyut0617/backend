package com.multivendor.service.repository;

import com.multivendor.service.entity.Service;
import com.multivendor.service.entity.ServiceCategory;
import com.multivendor.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    Page<Service> findByVendor(Vendor vendor, Pageable pageable);
    List<Service> findByVendorAndIsAvailableTrue(Vendor vendor);
    Page<Service> findByCategory(ServiceCategory category, Pageable pageable);

    @Query("SELECT s FROM Service s JOIN s.vendor v " +
           "WHERE s.isAvailable = true AND v.status = 'APPROVED' " +
           "AND (:categoryId IS NULL OR s.category.id = :categoryId) " +
           "AND (:city = '' OR LOWER(v.city) = LOWER(:city)) " +
           "AND (:keyword = '' OR LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Service> searchServices(@Param("categoryId") Long categoryId,
                                  @Param("city") String city,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);
}
