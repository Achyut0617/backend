package com.multivendor.customer.service;

import com.multivendor.common.enums.UserType;
import com.multivendor.common.exception.BusinessException;
import com.multivendor.common.exception.ResourceNotFoundException;
import com.multivendor.common.exception.UnauthorizedException;
import com.multivendor.config.security.JwtTokenProvider;
import com.multivendor.customer.dto.CustomerDto;
import com.multivendor.customer.entity.Customer;
import com.multivendor.customer.entity.FavoriteVendor;
import com.multivendor.customer.repository.CustomerRepository;
import com.multivendor.customer.repository.FavoriteVendorRepository;
import com.multivendor.notification.service.NotificationService;
import com.multivendor.vendor.entity.Vendor;
import com.multivendor.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FavoriteVendorRepository favoriteVendorRepository;
    private final VendorRepository vendorRepository;
    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CustomerDto.LoginResponse register(CustomerDto.RegisterRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }
        if (request.getPhone() != null && customerRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone number already registered");
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .city(request.getCity())
                .state(request.getState())
                .build();
        customer = customerRepository.save(customer);

        notificationService.sendNotification(UserType.CUSTOMER, customer.getId(),
                "Welcome to MultiVendor! 🎉",
                "Your account is ready. Browse services and book your first appointment.",
                "WELCOME");

        return buildLoginResponse(customer);
    }

    @Transactional
    public CustomerDto.LoginResponse login(CustomerDto.LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!customer.isActive()) {
            throw new UnauthorizedException("Your account has been deactivated");
        }

        customer.setLastLogin(LocalDateTime.now());
        customerRepository.save(customer);

        return buildLoginResponse(customer);
    }

    private CustomerDto.LoginResponse buildLoginResponse(Customer customer) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "CUSTOMER");
        claims.put("userId", customer.getId());

        String token = jwtTokenProvider.generateToken(customer.getEmail(), claims);
        String refreshToken = jwtTokenProvider.generateRefreshToken(customer.getEmail());

        CustomerDto.LoginResponse response = new CustomerDto.LoginResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setCity(customer.getCity());
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        return response;
    }

    public CustomerDto.ProfileResponse getProfile(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        return mapToProfileResponse(customer);
    }

    @Transactional
    public CustomerDto.ProfileResponse updateProfile(Long customerId, CustomerDto.UpdateProfileRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        if (request.getName() != null) customer.setName(request.getName());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getCity() != null) customer.setCity(request.getCity());
        if (request.getState() != null) customer.setState(request.getState());
        if (request.getPincode() != null) customer.setPincode(request.getPincode());

        return mapToProfileResponse(customerRepository.save(customer));
    }

    @Transactional
    public boolean toggleFavoriteVendor(Long customerId, Long vendorId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));

        if (favoriteVendorRepository.existsByCustomerAndVendor(customer, vendor)) {
            favoriteVendorRepository.deleteByCustomerAndVendor(customer, vendor);
            return false;
        } else {
            favoriteVendorRepository.save(FavoriteVendor.builder()
                    .customer(customer).vendor(vendor).build());
            return true;
        }
    }

    public Page<FavoriteVendor> getFavoriteVendors(Long customerId, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        return favoriteVendorRepository.findByCustomer(customer, pageable);
    }

    public Page<CustomerDto.Summary> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::mapToSummary);
    }

    private CustomerDto.ProfileResponse mapToProfileResponse(Customer customer) {
        CustomerDto.ProfileResponse r = new CustomerDto.ProfileResponse();
        r.setId(customer.getId()); r.setName(customer.getName()); r.setEmail(customer.getEmail());
        r.setPhone(customer.getPhone()); r.setAddress(customer.getAddress()); r.setCity(customer.getCity());
        r.setState(customer.getState()); r.setPincode(customer.getPincode()); r.setProfilePic(customer.getProfilePic());
        r.setVerified(customer.isVerified()); r.setTotalBookings(customer.getTotalBookings());
        r.setCreatedAt(customer.getCreatedAt());
        return r;
    }

    private CustomerDto.Summary mapToSummary(Customer c) {
        CustomerDto.Summary s = new CustomerDto.Summary();
        s.setId(c.getId()); s.setName(c.getName()); s.setEmail(c.getEmail());
        s.setPhone(c.getPhone()); s.setCity(c.getCity()); s.setActive(c.isActive());
        s.setTotalBookings(c.getTotalBookings()); s.setCreatedAt(c.getCreatedAt());
        return s;
    }
}
