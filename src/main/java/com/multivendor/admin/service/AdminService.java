package com.multivendor.admin.service;

import com.multivendor.admin.dto.AdminDto;
import com.multivendor.admin.entity.Admin;
import com.multivendor.admin.entity.AdminAction;
import com.multivendor.admin.repository.AdminActionRepository;
import com.multivendor.admin.repository.AdminRepository;
import com.multivendor.booking.repository.BookingRepository;
import com.multivendor.common.enums.BookingStatus;
import com.multivendor.common.enums.UserType;
import com.multivendor.common.enums.VendorStatus;
import com.multivendor.common.exception.BusinessException;
import com.multivendor.common.exception.ResourceNotFoundException;
import com.multivendor.common.exception.UnauthorizedException;
import com.multivendor.config.security.JwtTokenProvider;
import com.multivendor.customer.repository.CustomerRepository;
import com.multivendor.notification.service.NotificationService;
import com.multivendor.payment.repository.PaymentRepository;
import com.multivendor.vendor.entity.Vendor;
import com.multivendor.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final AdminActionRepository adminActionRepository;
    private final VendorRepository vendorRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminDto.LoginResponse login(AdminDto.LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!admin.isActive()) {
            throw new UnauthorizedException("Admin account is deactivated");
        }

        admin.setLastLogin(LocalDateTime.now());
        adminRepository.save(admin);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("userId", admin.getId());

        String token = jwtTokenProvider.generateToken(admin.getEmail(), claims);
        String refreshToken = jwtTokenProvider.generateRefreshToken(admin.getEmail());

        AdminDto.LoginResponse response = new AdminDto.LoginResponse();
        response.setId(admin.getId());
        response.setName(admin.getName());
        response.setEmail(admin.getEmail());
        response.setRole(admin.getRole());
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        return response;
    }

    public AdminDto.DashboardStats getDashboardStats() {
        AdminDto.DashboardStats stats = new AdminDto.DashboardStats();
        stats.setTotalVendors(vendorRepository.count());
        stats.setPendingVendors(vendorRepository.countByStatus(VendorStatus.PENDING));
        stats.setApprovedVendors(vendorRepository.countByStatus(VendorStatus.APPROVED));
        stats.setTotalCustomers(customerRepository.count());
        stats.setTotalBookings(bookingRepository.count());
        stats.setPendingBookings(bookingRepository.countByStatus(BookingStatus.PENDING));
        stats.setCompletedBookings(bookingRepository.countByStatus(BookingStatus.COMPLETED));

        Double totalRev = bookingRepository.getTotalRevenue();
        stats.setTotalRevenue(totalRev != null ? totalRev : 0);

        Double platformFee = bookingRepository.getTotalPlatformFee();
        stats.setPlatformRevenue(platformFee != null ? platformFee : 0);

        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        stats.setTodayBookings(bookingRepository.countBookingsInPeriod(
                LocalDateTime.now().withHour(0).withMinute(0),
                LocalDateTime.now()));

        return stats;
    }

    @Transactional
    public void performVendorAction(Long adminId, Long vendorId, AdminDto.VendorActionRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", adminId));
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));

        switch (request.getAction().toUpperCase()) {
            case "APPROVE" -> {
                vendor.setStatus(VendorStatus.APPROVED);
                vendor.setRejectionReason(null);
                notificationService.sendNotification(UserType.VENDOR, vendorId,
                        "Account Approved! 🎉",
                        "Congratulations! Your vendor account has been approved. You can now start offering services.",
                        "ACCOUNT_APPROVED");
            }
            case "REJECT" -> {
                vendor.setStatus(VendorStatus.REJECTED);
                vendor.setRejectionReason(request.getReason());
                notificationService.sendNotification(UserType.VENDOR, vendorId,
                        "Account Application Update",
                        "Your vendor application was not approved. Reason: " + request.getReason(),
                        "ACCOUNT_REJECTED");
            }
            case "SUSPEND" -> {
                vendor.setStatus(VendorStatus.SUSPENDED);
                notificationService.sendNotification(UserType.VENDOR, vendorId,
                        "Account Suspended",
                        "Your vendor account has been suspended. Reason: " + request.getReason(),
                        "ACCOUNT_SUSPENDED");
            }
            default -> throw new BusinessException("Invalid action: " + request.getAction());
        }

        vendorRepository.save(vendor);

        AdminAction action = AdminAction.builder()
                .admin(admin)
                .actionType("VENDOR_" + request.getAction().toUpperCase())
                .targetType(UserType.VENDOR)
                .targetId(vendorId)
                .notes(request.getReason())
                .build();
        adminActionRepository.save(action);
    }

    @Transactional
    public void updateVendorCommission(Long adminId, Long vendorId, AdminDto.UpdateCommissionRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", adminId));
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));

        vendor.setCommissionRate(request.getCommissionRate());
        vendorRepository.save(vendor);

        AdminAction action = AdminAction.builder()
                .admin(admin)
                .actionType("UPDATE_COMMISSION")
                .targetType(UserType.VENDOR)
                .targetId(vendorId)
                .notes("Commission updated to " + request.getCommissionRate() + "%")
                .build();
        adminActionRepository.save(action);
    }

    @Transactional
    public Admin createDefaultAdmin() {
        if (adminRepository.existsByEmail("admin@multivendor.com")) {
            return adminRepository.findByEmail("admin@multivendor.com").get();
        }
        Admin admin = Admin.builder()
                .name("Super Admin")
                .email("admin@multivendor.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role("ADMIN")
                .isActive(true)
                .build();
        return adminRepository.save(admin);
    }
}
