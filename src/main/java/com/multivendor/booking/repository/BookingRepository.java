package com.multivendor.booking.repository;

import com.multivendor.booking.entity.Booking;
import com.multivendor.common.enums.BookingStatus;
import com.multivendor.customer.entity.Customer;
import com.multivendor.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingRef(String bookingRef);
    Page<Booking> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);
    Page<Booking> findByVendorOrderByCreatedAtDesc(Vendor vendor, Pageable pageable);
    Page<Booking> findByVendorAndStatusOrderByCreatedAtDesc(Vendor vendor, BookingStatus status, Pageable pageable);
    Page<Booking> findByCustomerAndStatusOrderByCreatedAtDesc(Customer customer, BookingStatus status, Pageable pageable);
    Page<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status, Pageable pageable);

    long countByStatus(BookingStatus status);
    long countByVendorAndStatus(Vendor vendor, BookingStatus status);
    long countByCustomer(Customer customer);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :from AND b.createdAt <= :to")
    long countBookingsInPeriod(LocalDateTime from, LocalDateTime to);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.status = 'COMPLETED'")
    Double getTotalRevenue();

    @Query("SELECT SUM(b.platformFee) FROM Booking b WHERE b.status = 'COMPLETED'")
    Double getTotalPlatformFee();
}
