package com.multivendor.vendor.service;

import com.multivendor.common.enums.UserType;
import com.multivendor.common.enums.VendorStatus;
import com.multivendor.common.enums.VerificationStatus;
import com.multivendor.common.exception.BusinessException;
import com.multivendor.common.exception.ResourceNotFoundException;
import com.multivendor.common.exception.UnauthorizedException;
import com.multivendor.config.security.JwtTokenProvider;
import com.multivendor.notification.service.NotificationService;
import com.multivendor.payment.repository.PaymentRepository;
import com.multivendor.vendor.dto.VendorDto;
import com.multivendor.vendor.entity.Vendor;
import com.multivendor.vendor.entity.VendorVerification;
import com.multivendor.vendor.repository.VendorRepository;
import com.multivendor.vendor.repository.VendorVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VendorVerificationRepository verificationRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public VendorDto.LoginResponse register(VendorDto.RegisterRequest request) {
        if (vendorRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }
        if (vendorRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone number already registered");
        }

        Vendor vendor = Vendor.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .businessName(request.getBusinessName())
                .businessDescription(request.getBusinessDescription())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .address(request.getAddress())
                .status(VendorStatus.PENDING)
                .build();
        vendor = vendorRepository.save(vendor);

        notificationService.sendNotification(UserType.VENDOR, vendor.getId(),
                "Registration Successful",
                "Welcome to MultiVendor! Your account is under review. We'll notify you once approved.",
                "WELCOME");

        return buildLoginResponse(vendor);
    }

    @Transactional
    public VendorDto.LoginResponse login(VendorDto.LoginRequest request) {
        Vendor vendor = vendorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), vendor.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (vendor.getStatus() == VendorStatus.PENDING) {
            throw new UnauthorizedException("Your account is pending admin approval");
        }
        if (vendor.getStatus() == VendorStatus.REJECTED || vendor.getStatus() == VendorStatus.SUSPENDED) {
            throw new UnauthorizedException("Your account is " + vendor.getStatus().name().toLowerCase() + ". Please contact support.");
        }

        vendor.setLastLogin(LocalDateTime.now());
        vendorRepository.save(vendor);

        return buildLoginResponse(vendor);
    }

    private VendorDto.LoginResponse buildLoginResponse(Vendor vendor) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "VENDOR");
        claims.put("userId", vendor.getId());

        String token = jwtTokenProvider.generateToken(vendor.getEmail(), claims);
        String refreshToken = jwtTokenProvider.generateRefreshToken(vendor.getEmail());

        VendorDto.LoginResponse response = new VendorDto.LoginResponse();
        response.setId(vendor.getId());
        response.setName(vendor.getName());
        response.setEmail(vendor.getEmail());
        response.setBusinessName(vendor.getBusinessName());
        response.setCity(vendor.getCity());
        response.setStatus(vendor.getStatus());
        response.setRating(vendor.getRating());
        response.setKycVerified(vendor.isKycVerified());
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        return response;
    }

    public VendorDto.ProfileResponse getProfile(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));
        return mapToProfileResponse(vendor);
    }

    @Transactional
    public VendorDto.ProfileResponse updateProfile(Long vendorId, VendorDto.UpdateProfileRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));

        if (request.getName() != null) vendor.setName(request.getName());
        if (request.getPhone() != null) {
            if (!request.getPhone().equals(vendor.getPhone()) && vendorRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("Phone number already in use");
            }
            vendor.setPhone(request.getPhone());
        }
        if (request.getBusinessName() != null) vendor.setBusinessName(request.getBusinessName());
        if (request.getBusinessDescription() != null) vendor.setBusinessDescription(request.getBusinessDescription());
        if (request.getCity() != null) vendor.setCity(request.getCity());
        if (request.getState() != null) vendor.setState(request.getState());
        if (request.getPincode() != null) vendor.setPincode(request.getPincode());
        if (request.getAddress() != null) vendor.setAddress(request.getAddress());

        return mapToProfileResponse(vendorRepository.save(vendor));
    }

    @Transactional
    public void updateAvailability(Long vendorId, boolean isAvailable) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));
        vendor.setAvailable(isAvailable);
        vendorRepository.save(vendor);
    }

    @Transactional
    public VendorVerification submitKyc(Long vendorId, VendorDto.KycSubmitRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));

        VendorVerification verification = verificationRepository.findByVendor(vendor)
                .orElse(VendorVerification.builder().vendor(vendor).build());

        verification.setAadhaarNumber(request.getAadhaarNumber());
        verification.setPanNumber(request.getPanNumber());
        verification.setBankAccountNumber(request.getBankAccountNumber());
        verification.setBankIfsc(request.getBankIfsc());
        verification.setBankName(request.getBankName());
        verification.setVerificationStatus(VerificationStatus.PENDING);

        return verificationRepository.save(verification);
    }

    public VendorDto.EarningsDashboard getEarningsDashboard(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));

        BigDecimal totalEarnings = paymentRepository.sumVendorEarnings(vendor);

        VendorDto.EarningsDashboard dashboard = new VendorDto.EarningsDashboard();
        dashboard.setTotalEarnings(totalEarnings);
        dashboard.setCompletedJobs((long) vendor.getTotalBookings());
        dashboard.setAvgJobValue(vendor.getTotalBookings() > 0
                ? totalEarnings.divide(BigDecimal.valueOf(vendor.getTotalBookings()), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO);
        return dashboard;
    }

    public Page<VendorDto.Summary> getAllVendors(Pageable pageable) {
        return vendorRepository.findAll(pageable).map(this::mapToSummary);
    }

    public Page<VendorDto.Summary> getVendorsByStatus(VendorStatus status, Pageable pageable) {
        return vendorRepository.findByStatus(status, pageable).map(this::mapToSummary);
    }

    private VendorDto.ProfileResponse mapToProfileResponse(Vendor vendor) {
        VendorDto.ProfileResponse r = new VendorDto.ProfileResponse();
        r.setId(vendor.getId()); r.setName(vendor.getName()); r.setEmail(vendor.getEmail());
        r.setPhone(vendor.getPhone()); r.setBusinessName(vendor.getBusinessName());
        r.setBusinessDescription(vendor.getBusinessDescription()); r.setCity(vendor.getCity());
        r.setState(vendor.getState()); r.setPincode(vendor.getPincode()); r.setAddress(vendor.getAddress());
        r.setProfilePic(vendor.getProfilePic()); r.setStatus(vendor.getStatus()); r.setRating(vendor.getRating());
        r.setTotalReviews(vendor.getTotalReviews()); r.setTotalBookings(vendor.getTotalBookings());
        r.setTotalEarnings(vendor.getTotalEarnings()); r.setCommissionRate(vendor.getCommissionRate());
        r.setAvailable(vendor.isAvailable()); r.setKycVerified(vendor.isKycVerified());
        r.setCreatedAt(vendor.getCreatedAt());
        return r;
    }

    private VendorDto.Summary mapToSummary(Vendor vendor) {
        VendorDto.Summary s = new VendorDto.Summary();
        s.setId(vendor.getId()); s.setName(vendor.getName()); s.setBusinessName(vendor.getBusinessName());
        s.setCity(vendor.getCity()); s.setStatus(vendor.getStatus()); s.setRating(vendor.getRating());
        s.setTotalReviews(vendor.getTotalReviews()); s.setAvailable(vendor.isAvailable());
        s.setProfilePic(vendor.getProfilePic()); s.setCreatedAt(vendor.getCreatedAt());
        return s;
    }
}
