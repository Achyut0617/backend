package com.multivendor.vendor.controller;

import com.multivendor.booking.dto.BookingDto;
import com.multivendor.booking.service.BookingService;
import com.multivendor.common.enums.BookingStatus;
import com.multivendor.common.response.ApiResponse;
import com.multivendor.config.security.JwtAuthenticationToken;
import com.multivendor.review.dto.ReviewDto;
import com.multivendor.review.service.ReviewService;
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
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;
    private final BookingService bookingService;
    private final ServiceManagementService serviceManagementService;
    private final ReviewService reviewService;

    // ==================== AUTH ====================
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<VendorDto.LoginResponse>> register(
            @Valid @RequestBody VendorDto.RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.register(request), "Registration successful. Awaiting admin approval."));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<VendorDto.LoginResponse>> login(
            @Valid @RequestBody VendorDto.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.login(request), "Login successful"));
    }

    // ==================== PROFILE ====================
    @GetMapping("/profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<VendorDto.ProfileResponse>> getProfile(JwtAuthenticationToken auth) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getProfile(auth.getUserId()), "Profile retrieved"));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<VendorDto.ProfileResponse>> updateProfile(
            JwtAuthenticationToken auth,
            @RequestBody VendorDto.UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.updateProfile(auth.getUserId(), request), "Profile updated"));
    }

    @PatchMapping("/availability")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> updateAvailability(
            JwtAuthenticationToken auth,
            @RequestBody VendorDto.AvailabilityRequest request) {
        vendorService.updateAvailability(auth.getUserId(), request.isAvailable());
        return ResponseEntity.ok(ApiResponse.success("Availability updated"));
    }

    // ==================== KYC ====================
    @PostMapping("/kyc/submit")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<?>> submitKyc(
            JwtAuthenticationToken auth,
            @Valid @RequestBody VendorDto.KycSubmitRequest request) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.submitKyc(auth.getUserId(), request), "KYC submitted"));
    }

    // ==================== SERVICES ====================
    @GetMapping("/services")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<ServiceDto.Response>>> getMyServices(
            JwtAuthenticationToken auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                serviceManagementService.getVendorServices(auth.getUserId(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending())),
                "Services retrieved"));
    }

    @PostMapping("/services")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ServiceDto.Response>> createService(
            JwtAuthenticationToken auth,
            @Valid @RequestBody ServiceDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                serviceManagementService.createService(auth.getUserId(), request), "Service created"));
    }

    @PutMapping("/services/{serviceId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ServiceDto.Response>> updateService(
            JwtAuthenticationToken auth,
            @PathVariable Long serviceId,
            @RequestBody ServiceDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                serviceManagementService.updateService(auth.getUserId(), serviceId, request), "Service updated"));
    }

    @DeleteMapping("/services/{serviceId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteService(
            JwtAuthenticationToken auth,
            @PathVariable Long serviceId) {
        serviceManagementService.deleteService(auth.getUserId(), serviceId);
        return ResponseEntity.ok(ApiResponse.success("Service deleted"));
    }

    // ==================== BOOKINGS ====================
    @GetMapping("/bookings")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<BookingDto.Response>>> getBookings(
            JwtAuthenticationToken auth,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getVendorBookings(auth.getUserId(), status,
                        PageRequest.of(page, size, Sort.by("createdAt").descending())),
                "Bookings retrieved"));
    }

    @PostMapping("/bookings/{bookingId}/action")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<BookingDto.Response>> performBookingAction(
            JwtAuthenticationToken auth,
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingDto.VendorActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.vendorAction(auth.getUserId(), bookingId, request), "Action performed"));
    }

    // ==================== EARNINGS ====================
    @GetMapping("/earnings")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<VendorDto.EarningsDashboard>> getEarnings(JwtAuthenticationToken auth) {
        return ResponseEntity.ok(ApiResponse.success(
                vendorService.getEarningsDashboard(auth.getUserId()), "Earnings retrieved"));
    }

    // ==================== REVIEWS ====================
    @GetMapping("/reviews")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<ReviewDto.Response>>> getReviews(
            JwtAuthenticationToken auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getVendorReviews(auth.getUserId(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending())),
                "Reviews retrieved"));
    }

    @PostMapping("/reviews/{reviewId}/reply")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ReviewDto.Response>> replyToReview(
            JwtAuthenticationToken auth,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewDto.ReplyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.replyToReview(auth.getUserId(), reviewId, request), "Reply posted"));
    }
}
