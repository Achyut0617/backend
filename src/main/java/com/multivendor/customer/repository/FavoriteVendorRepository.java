package com.multivendor.customer.repository;

import com.multivendor.customer.entity.Customer;
import com.multivendor.customer.entity.FavoriteVendor;
import com.multivendor.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteVendorRepository extends JpaRepository<FavoriteVendor, Long> {
    Optional<FavoriteVendor> findByCustomerAndVendor(Customer customer, Vendor vendor);
    boolean existsByCustomerAndVendor(Customer customer, Vendor vendor);
    Page<FavoriteVendor> findByCustomer(Customer customer, Pageable pageable);
    void deleteByCustomerAndVendor(Customer customer, Vendor vendor);
}
