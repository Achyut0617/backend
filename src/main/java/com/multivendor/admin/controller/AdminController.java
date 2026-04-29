package com.multivendor.admin.controller;

import com.multivendor.admin.dto.AdminDto;
import com.multivendor.admin.service.AdminService;
import com.multivendor.booking.dto.BookingDto;
import com.multivendor.booking.service.BookingService;
import com.multivendor.common.enums.BookingStatus;
import com.multivendor.common.enums.VendorStatus;
import com.multivendor.common.response.ApiResponse;
import com.multivendor.complaint.dto.ComplaintDto;
import com.multivendor.complaint.entity.Complaint;
import com.multivendor.complaint.repository.ComplaintRepository;
import com.multivendor.config.security.JwtAuthenticationToken;
import com.multivendor.customer.dto.CustomerDto;
import com.multivendor.customer.service.CustomerService;
import com.multivendor.service.dto.ServiceDto;
import com.multivendor.service.service.ServiceManagementService;
import com.multivendor.vendor.dto.VendorDto;
import com.multivendor.vendor.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final VendorService vendorService;
    private final CustomerService customerService;
    private final BookingService bookingService;
    private final ServiceManagementService serviceManagementService;

    // ==================== AUTH ====================
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<AdminDto.LoginResponse>> login(
            @Valid @RequestBody AdminDto.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.login(request), "Login successful"));
    }

    // ==================== DASHBOARD ====================
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDto.DashboardStats>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats(), "Stats retrieved"));
    }

    // ==================== VENDOR MANAGEMENT ====================
    @GetMapping("/vendors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<VendorDto.Summary>>> getVendors(
            @RequestParam(required = false) VendorStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<VendorDto.Summary> vendors = status != null
                ? vendorService.getVendorsByStatus(status, pageable)
                : vendorService.getAllVendors(pageable);
        return ResponseEntity.ok(ApiResponse.success(vendors, "Vendors retrieved"));
    }

    @PostMapping("/vendors/{vendorId}/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> vendorAction(
            JwtAuthenticationToken auth,
            @PathVariable Long vendorId,
            @Valid @RequestBody AdminDto.VendorActionRequest request) {
        adminService.performVendorAction(auth.getUserId(), vendorId, request);
        return ResponseEntity.ok(ApiResponse.success("Action performed successfully"));
    }

    @PutMapping("/vendors/{vendorId}/commission")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateCommission(
            JwtAuthenticationToken auth,
            @PathVariable Long vendorId,
            @RequestBody AdminDto.UpdateCommissionRequest request) {
        adminService.updateVendorCommission(auth.getUserId(), vendorId, request);
        return ResponseEntity.ok(ApiResponse.success("Commission updated"));
    }

    // ==================== CUSTOMER MANAGEMENT ====================
    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CustomerDto.Summary>>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(customerService.getAllCustomers(pageable), "Customers retrieved"));
    }

    // ==================== BOOKING MONITORING ====================
    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BookingDto.Response>>> getAllBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(bookingService.getAllBookings(status, pageable), "Bookings retrieved"));
    }

    @GetMapping("/bookings/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingDto.Response>> getBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingById(bookingId), "Booking retrieved"));
    }

    // ==================== SERVICE CATEGORIES ====================
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(serviceManagementService.getAllCategoriesAdmin(), "Categories retrieved"));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceDto.CategoryResponse>> createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String icon) {
        return ResponseEntity.ok(ApiResponse.success(
                serviceManagementService.createCategory(name, description, icon), "Category created"));
    }
}
