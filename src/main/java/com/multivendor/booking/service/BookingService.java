package com.multivendor.booking.service;

import com.multivendor.booking.dto.BookingDto;
import com.multivendor.booking.entity.Booking;
import com.multivendor.booking.repository.BookingRepository;
import com.multivendor.common.enums.BookingStatus;
import com.multivendor.common.enums.UserType;
import com.multivendor.common.enums.VendorStatus;
import com.multivendor.common.exception.BusinessException;
import com.multivendor.common.exception.ResourceNotFoundException;
import com.multivendor.common.exception.UnauthorizedException;
import com.multivendor.customer.entity.Customer;
import com.multivendor.customer.repository.CustomerRepository;
import com.multivendor.notification.service.NotificationService;
import com.multivendor.service.entity.Service;
import com.multivendor.service.repository.ServiceRepository;
import com.multivendor.vendor.entity.Vendor;
import com.multivendor.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final ServiceRepository serviceRepository;
    private final NotificationService notificationService;

    @Transactional
    public BookingDto.Response createBooking(Long customerId, BookingDto.CreateRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", request.getServiceId()));

        if (!service.isAvailable()) {
            throw new BusinessException("This service is currently unavailable");
        }

        Vendor vendor = service.getVendor();
        if (vendor.getStatus() != VendorStatus.APPROVED) {
            throw new BusinessException("Vendor is not accepting bookings at this time");
        }

        // Calculate financials
        BigDecimal totalAmount = service.getPrice();
        BigDecimal commissionRate = vendor.getCommissionRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal platformFee = totalAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal vendorPayout = totalAmount.subtract(platformFee);

        String bookingRef = "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Booking booking = Booking.builder()
                .bookingRef(bookingRef)
                .customer(customer)
                .vendor(vendor)
                .service(service)
                .status(BookingStatus.PENDING)
                .scheduledAt(request.getScheduledAt())
                .serviceAddress(request.getServiceAddress())
                .city(request.getCity())
                .pincode(request.getPincode())
                .notes(request.getNotes())
                .totalAmount(totalAmount)
                .platformFee(platformFee)
                .vendorPayout(vendorPayout)
                .build();

        booking = bookingRepository.save(booking);

        // Update counters
        customer.setTotalBookings(customer.getTotalBookings() + 1);
        customerRepository.save(customer);

        service.setTotalBookings(service.getTotalBookings() + 1);
        serviceRepository.save(service);

        // Notifications
        String scheduledFormatted = request.getScheduledAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        notificationService.sendNotification(UserType.VENDOR, vendor.getId(),
                "New Booking Request! 📋",
                "You have a new booking request for " + service.getTitle() + " on " + scheduledFormatted +
                ". Booking ID: " + bookingRef,
                "NEW_BOOKING");

        notificationService.sendNotification(UserType.CUSTOMER, customerId,
                "Booking Created Successfully! ✅",
                "Your booking for " + service.getTitle() + " has been sent to " + vendor.getName() +
                ". Awaiting confirmation. Booking ID: " + bookingRef,
                "BOOKING_CREATED");

        return mapToResponse(booking);
    }

    @Transactional
    public BookingDto.Response vendorAction(Long vendorId, Long bookingId, BookingDto.VendorActionRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.getVendor().getId().equals(vendorId)) {
            throw new UnauthorizedException("You don't have access to this booking");
        }

        switch (request.getAction().toUpperCase()) {
            case "ACCEPT" -> {
                if (booking.getStatus() != BookingStatus.PENDING) {
                    throw new BusinessException("Booking is no longer in pending state");
                }
                booking.setStatus(BookingStatus.ACCEPTED);
                notificationService.sendNotification(UserType.CUSTOMER, booking.getCustomer().getId(),
                        "Booking Confirmed! 🎉",
                        "Great news! " + booking.getVendor().getName() + " has accepted your booking for " +
                        booking.getService().getTitle() + ". Booking ID: " + booking.getBookingRef(),
                        "BOOKING_ACCEPTED");
            }
            case "REJECT" -> {
                if (booking.getStatus() != BookingStatus.PENDING) {
                    throw new BusinessException("Booking cannot be rejected now");
                }
                booking.setStatus(BookingStatus.REJECTED);
                booking.setRejectionReason(request.getReason());
                notificationService.sendNotification(UserType.CUSTOMER, booking.getCustomer().getId(),
                        "Booking Update",
                        "Unfortunately, your booking request was declined. Reason: " + request.getReason() +
                        ". Please try another vendor.",
                        "BOOKING_REJECTED");
            }
            case "START" -> {
                if (booking.getStatus() != BookingStatus.ACCEPTED) {
                    throw new BusinessException("Booking must be accepted before starting");
                }
                booking.setStatus(BookingStatus.IN_PROGRESS);
                booking.setStartedAt(LocalDateTime.now());
                notificationService.sendNotification(UserType.CUSTOMER, booking.getCustomer().getId(),
                        "Service Started! 🔧",
                        booking.getVendor().getName() + " has started working on your booking. " +
                        "Booking ID: " + booking.getBookingRef(),
                        "SERVICE_STARTED");
            }
            case "COMPLETE" -> {
                if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
                    throw new BusinessException("Service must be in progress to complete");
                }
                booking.setStatus(BookingStatus.COMPLETED);
                booking.setCompletedAt(LocalDateTime.now());
                booking.setVendorNotes(request.getVendorNotes());

                // Update vendor stats
                Vendor vendor = booking.getVendor();
                vendor.setTotalBookings(vendor.getTotalBookings() + 1);
                vendor.setTotalEarnings(vendor.getTotalEarnings().add(booking.getVendorPayout()));
                vendorRepository.save(vendor);

                notificationService.sendNotification(UserType.CUSTOMER, booking.getCustomer().getId(),
                        "Service Completed! ⭐",
                        "Your service has been completed. Please rate your experience with " +
                        booking.getVendor().getName() + ". Booking ID: " + booking.getBookingRef(),
                        "SERVICE_COMPLETED");
            }
            default -> throw new BusinessException("Invalid action: " + request.getAction());
        }

        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto.Response cancelBooking(Long customerId, Long bookingId, BookingDto.CancelRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("You don't have access to this booking");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking cannot be cancelled in its current state");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getReason());

        notificationService.sendNotification(UserType.VENDOR, booking.getVendor().getId(),
                "Booking Cancelled",
                "Customer has cancelled the booking for " + booking.getService().getTitle() +
                ". Booking ID: " + booking.getBookingRef(),
                "BOOKING_CANCELLED");

        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingDto.Response getBookingById(Long bookingId) {
        return mapToResponse(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId)));
    }

    @Transactional(readOnly = true)
    public Page<BookingDto.Response> getCustomerBookings(Long customerId, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        return bookingRepository.findByCustomerOrderByCreatedAtDesc(customer, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookingDto.Response> getVendorBookings(Long vendorId, BookingStatus status, Pageable pageable) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));
        if (status != null) {
            return bookingRepository.findByVendorAndStatusOrderByCreatedAtDesc(vendor, status, pageable).map(this::mapToResponse);
        }
        return bookingRepository.findByVendorOrderByCreatedAtDesc(vendor, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookingDto.Response> getAllBookings(BookingStatus status, Pageable pageable) {
        if (status != null) {
            return bookingRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map(this::mapToResponse);
        }
        return bookingRepository.findAll(pageable).map(this::mapToResponse);
    }

    private BookingDto.Response mapToResponse(Booking b) {
        BookingDto.Response r = new BookingDto.Response();
        r.setId(b.getId()); r.setBookingRef(b.getBookingRef()); r.setStatus(b.getStatus());
        r.setScheduledAt(b.getScheduledAt()); r.setServiceAddress(b.getServiceAddress());
        r.setCity(b.getCity()); r.setNotes(b.getNotes()); r.setTotalAmount(b.getTotalAmount());
        r.setPlatformFee(b.getPlatformFee()); r.setVendorPayout(b.getVendorPayout());
        r.setStartedAt(b.getStartedAt()); r.setCompletedAt(b.getCompletedAt());
        r.setRejectionReason(b.getRejectionReason()); r.setCancellationReason(b.getCancellationReason());
        r.setVendorNotes(b.getVendorNotes()); r.setCreatedAt(b.getCreatedAt());

        BookingDto.CustomerSummary cs = new BookingDto.CustomerSummary();
        cs.setId(b.getCustomer().getId()); cs.setName(b.getCustomer().getName());
        cs.setPhone(b.getCustomer().getPhone()); cs.setEmail(b.getCustomer().getEmail());
        r.setCustomer(cs);

        BookingDto.VendorSummary vs = new BookingDto.VendorSummary();
        vs.setId(b.getVendor().getId()); vs.setName(b.getVendor().getName());
        vs.setBusinessName(b.getVendor().getBusinessName()); vs.setPhone(b.getVendor().getPhone());
        vs.setEmail(b.getVendor().getEmail());
        r.setVendor(vs);

        BookingDto.ServiceSummary ss = new BookingDto.ServiceSummary();
        ss.setId(b.getService().getId()); ss.setTitle(b.getService().getTitle());
        ss.setCategoryName(b.getService().getCategory().getName()); ss.setPrice(b.getService().getPrice());
        r.setService(ss);

        return r;
    }
}
