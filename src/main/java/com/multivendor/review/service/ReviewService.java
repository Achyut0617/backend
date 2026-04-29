package com.multivendor.review.service;

import com.multivendor.booking.entity.Booking;
import com.multivendor.booking.repository.BookingRepository;
import com.multivendor.common.enums.BookingStatus;
import com.multivendor.common.enums.UserType;
import com.multivendor.common.exception.BusinessException;
import com.multivendor.common.exception.ResourceNotFoundException;
import com.multivendor.common.exception.UnauthorizedException;
import com.multivendor.notification.service.NotificationService;
import com.multivendor.review.dto.ReviewDto;
import com.multivendor.review.entity.Review;
import com.multivendor.review.repository.ReviewRepository;
import com.multivendor.vendor.entity.Vendor;
import com.multivendor.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final VendorRepository vendorRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReviewDto.Response createReview(Long customerId, ReviewDto.CreateRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("You can only review your own bookings");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException("You can only review completed bookings");
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new BusinessException("You have already reviewed this booking");
        }

        Review review = Review.builder()
                .booking(booking)
                .customer(booking.getCustomer())
                .vendor(booking.getVendor())
                .rating(request.getRating())
                .comment(request.getComment())
                .isVisible(true)
                .build();

        review = reviewRepository.save(review);
        updateVendorRating(booking.getVendor());

        notificationService.sendNotification(UserType.VENDOR, booking.getVendor().getId(),
                "New Review Received! ⭐",
                "Customer " + booking.getCustomer().getName() + " gave you a " + request.getRating() +
                "-star review. Check it out!",
                "NEW_REVIEW");

        return mapToResponse(review);
    }

    @Transactional
    public ReviewDto.Response replyToReview(Long vendorId, Long reviewId, ReviewDto.ReplyRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        if (!review.getVendor().getId().equals(vendorId)) {
            throw new UnauthorizedException("You can only reply to your own reviews");
        }

        if (review.getReply() != null) {
            throw new BusinessException("You have already replied to this review");
        }

        review.setReply(request.getReply());
        review.setReplyAt(LocalDateTime.now());
        return mapToResponse(reviewRepository.save(review));
    }

    public Page<ReviewDto.Response> getVendorReviews(Long vendorId, Pageable pageable) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));
        return reviewRepository.findByVendorAndIsVisibleTrueOrderByCreatedAtDesc(vendor, pageable)
                .map(this::mapToResponse);
    }

    private void updateVendorRating(Vendor vendor) {
        Double avgRating = reviewRepository.calculateVendorRating(vendor);
        long totalReviews = reviewRepository.countVendorReviews(vendor);
        vendor.setRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
        vendor.setTotalReviews((int) totalReviews);
        vendorRepository.save(vendor);
    }

    private ReviewDto.Response mapToResponse(Review r) {
        ReviewDto.Response res = new ReviewDto.Response();
        res.setId(r.getId()); res.setBookingId(r.getBooking().getId());
        res.setBookingRef(r.getBooking().getBookingRef());
        res.setCustomerName(r.getCustomer().getName()); res.setVendorName(r.getVendor().getName());
        res.setRating(r.getRating()); res.setComment(r.getComment());
        res.setReply(r.getReply()); res.setReplyAt(r.getReplyAt());
        res.setVisible(r.isVisible()); res.setCreatedAt(r.getCreatedAt());
        return res;
    }
}
