package com.multivendor.customer.controller;

import com.multivendor.booking.dto.BookingDto;
import com.multivendor.booking.service.BookingService;
import com.multivendor.common.enums.BookingStatus;
import com.multivendor.common.response.ApiResponse;
import com.multivendor.complaint.dto.ComplaintDto;
import com.multivendor.config.security.JwtAuthenticationToken;
import com.multivendor.customer.dto.CustomerDto;
import com.multivendor.customer.service.CustomerService;
import com.multivendor.notification.entity.Notification;
import com.multivendor.notification.service.NotificationService;
import com.multivendor.review.dto.ReviewDto;
import com.multivendor.review.service.ReviewService;
import com.multivendor.service.dto.ServiceDto;
import com.multivendor.service.service.ServiceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.multivendor.common.enums.UserType;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final BookingService bookingService;
    private final ServiceManagementService serviceManagementService;
    private final ReviewService reviewService;
    private final NotificationService notificationService;

    // ==================== AUTH ====================
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<CustomerDto.LoginResponse>> register(
            @Valid @RequestBody CustomerDto.RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerService.register(request), "Registration successful"));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<CustomerDto.LoginResponse>> login(
            @Valid @RequestBody CustomerDto.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerService.login(request), "Login successful"));
    }

    // ==================== PROFILE ====================
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerDto.ProfileResponse>> getProfile(JwtAuthenticationToken auth) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getProfile(auth.getUserId()), "Profile retrieved"));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerDto.ProfileResponse>> updateProfile(
            JwtAuthenticationToken auth,
            @RequestBody CustomerDto.UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerService.updateProfile(auth.getUserId(), request), "Profile updated"));
    }

    // ==================== SERVICE DISCOVERY ====================
    @GetMapping("/services/search")
    public ResponseEntity<ApiResponse<Page<ServiceDto.Response>>> searchServices(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                serviceManagementService.searchServices(categoryId, city, keyword,
                        PageRequest.of(page, size)),
                "Services found"));
    }

    @GetMapping("/services/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceDto.Response>> getService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(ApiResponse.success(serviceManagementService.getServiceById(serviceId), "Service retrieved"));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<?>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(serviceManagementService.getAllCategories(), "Categories retrieved"));
    }

    // ==================== BOOKINGS ====================
    @PostMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingDto.Response>> createBooking(
            JwtAuthenticationToken auth,
            @Valid @RequestBody BookingDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.createBooking(auth.getUserId(), request), "Booking created successfully"));
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<BookingDto.Response>>> getMyBookings(
            JwtAuthenticationToken auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getCustomerBookings(auth.getUserId(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending())),
                "Bookings retrieved"));
    }

    @GetMapping("/bookings/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingDto.Response>> getBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingById(bookingId), "Booking retrieved"));
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingDto.Response>> cancelBooking(
            JwtAuthenticationToken auth,
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingDto.CancelRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.cancelBooking(auth.getUserId(), bookingId, request), "Booking cancelled"));
    }

    // ==================== REVIEWS ====================
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ReviewDto.Response>> createReview(
            JwtAuthenticationToken auth,
            @Valid @RequestBody ReviewDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.createReview(auth.getUserId(), request), "Review submitted"));
    }

    // ==================== FAVORITES ====================
    @PostMapping("/favorites/{vendorId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<String>> toggleFavorite(
            JwtAuthenticationToken auth,
            @PathVariable Long vendorId) {
        boolean added = customerService.toggleFavoriteVendor(auth.getUserId(), vendorId);
        return ResponseEntity.ok(ApiResponse.success(added ? "Added to favorites" : "Removed from favorites",
                added ? "Added to favorites" : "Removed from favorites"));
    }

    // ==================== NOTIFICATIONS ====================
    @GetMapping("/notifications")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<Notification>>> getNotifications(
            JwtAuthenticationToken auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getNotifications(UserType.CUSTOMER, auth.getUserId(),
                        PageRequest.of(page, size)),
                "Notifications retrieved"));
    }

    @GetMapping("/notifications/unread-count")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(JwtAuthenticationToken auth) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUnreadCount(UserType.CUSTOMER, auth.getUserId()), "Count retrieved"));
    }

    @PostMapping("/notifications/mark-all-read")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> markAllRead(JwtAuthenticationToken auth) {
        notificationService.markAllAsRead(UserType.CUSTOMER, auth.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Notifications marked as read"));
    }
}
