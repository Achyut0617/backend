package com.multivendor.review.repository;

import com.multivendor.review.entity.Review;
import com.multivendor.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByBookingId(Long bookingId);
    Page<Review> findByVendorAndIsVisibleTrueOrderByCreatedAtDesc(Vendor vendor, Pageable pageable);
    Page<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    boolean existsByBookingId(Long bookingId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.vendor = :vendor AND r.isVisible = true")
    Double calculateVendorRating(Vendor vendor);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.vendor = :vendor AND r.isVisible = true")
    long countVendorReviews(Vendor vendor);
}
