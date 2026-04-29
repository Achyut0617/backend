package com.multivendor.service.service;

import com.multivendor.common.exception.BusinessException;
import com.multivendor.common.exception.ResourceNotFoundException;
import com.multivendor.common.exception.UnauthorizedException;
import com.multivendor.service.dto.ServiceDto;
import com.multivendor.service.entity.ServiceCategory;
import com.multivendor.service.repository.ServiceCategoryRepository;
import com.multivendor.service.repository.ServiceRepository;
import com.multivendor.vendor.entity.Vendor;
import com.multivendor.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final VendorRepository vendorRepository;

    @Transactional(readOnly = true)
    public List<ServiceDto.CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrder()
                .stream().map(this::mapCategory).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceDto.CategoryResponse> getAllCategoriesAdmin() {
        return categoryRepository.findAll()
                .stream().map(this::mapCategory).collect(Collectors.toList());
    }

    @Transactional
    public ServiceDto.CategoryResponse createCategory(String name, String description, String icon) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Category already exists: " + name);
        }
        ServiceCategory category = ServiceCategory.builder()
                .name(name).description(description).icon(icon).isActive(true).build();
        return mapCategory(categoryRepository.save(category));
    }

    @Transactional
    public ServiceDto.Response createService(Long vendorId, ServiceDto.CreateRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));

        ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        com.multivendor.service.entity.Service service = com.multivendor.service.entity.Service.builder()
                .vendor(vendor)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .unit(request.getUnit() != null ? request.getUnit() : "per visit")
                .durationMins(request.getDurationMins())
                .areaServed(request.getAreaServed())
                .isAvailable(true)
                .build();

        return mapService(serviceRepository.save(service));
    }

    @Transactional
    public ServiceDto.Response updateService(Long vendorId, Long serviceId, ServiceDto.UpdateRequest request) {
        com.multivendor.service.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        if (!service.getVendor().getId().equals(vendorId)) {
            throw new UnauthorizedException("You don't own this service");
        }

        if (request.getTitle() != null) service.setTitle(request.getTitle());
        if (request.getDescription() != null) service.setDescription(request.getDescription());
        if (request.getPrice() != null) service.setPrice(request.getPrice());
        if (request.getUnit() != null) service.setUnit(request.getUnit());
        if (request.getDurationMins() != null) service.setDurationMins(request.getDurationMins());
        if (request.getAreaServed() != null) service.setAreaServed(request.getAreaServed());
        if (request.getIsAvailable() != null) service.setAvailable(request.getIsAvailable());

        return mapService(serviceRepository.save(service));
    }

    @Transactional
    public void deleteService(Long vendorId, Long serviceId) {
        com.multivendor.service.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));
        if (!service.getVendor().getId().equals(vendorId)) {
            throw new UnauthorizedException("You don't own this service");
        }
        serviceRepository.delete(service);
    }

    @Transactional(readOnly = true)
    public Page<ServiceDto.Response> searchServices(Long categoryId, String city, String keyword, Pageable pageable) {
        String safeCity = (city != null) ? city : "";
        String safeKeyword = (keyword != null) ? keyword : "";
        return serviceRepository.searchServices(categoryId, safeCity, safeKeyword, pageable).map(this::mapService);
    }

    @Transactional(readOnly = true)
    public Page<ServiceDto.Response> getVendorServices(Long vendorId, Pageable pageable) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));
        return serviceRepository.findByVendor(vendor, pageable).map(this::mapService);
    }

    @Transactional(readOnly = true)
    public ServiceDto.Response getServiceById(Long serviceId) {
        return mapService(serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId)));
    }

    private ServiceDto.CategoryResponse mapCategory(ServiceCategory c) {
        ServiceDto.CategoryResponse r = new ServiceDto.CategoryResponse();
        r.setId(c.getId()); r.setName(c.getName()); r.setDescription(c.getDescription());
        r.setIcon(c.getIcon()); r.setImageUrl(c.getImageUrl()); r.setActive(c.isActive());
        return r;
    }

    private ServiceDto.Response mapService(com.multivendor.service.entity.Service s) {
        ServiceDto.Response r = new ServiceDto.Response();
        r.setId(s.getId()); r.setVendorId(s.getVendor().getId()); r.setVendorName(s.getVendor().getName());
        r.setVendorCity(s.getVendor().getCity()); r.setVendorRating(s.getVendor().getRating());
        r.setCategoryId(s.getCategory().getId()); r.setCategoryName(s.getCategory().getName());
        r.setTitle(s.getTitle()); r.setDescription(s.getDescription()); r.setPrice(s.getPrice());
        r.setUnit(s.getUnit()); r.setDurationMins(s.getDurationMins()); r.setAreaServed(s.getAreaServed());
        r.setImageUrl(s.getImageUrl()); r.setAvailable(s.isAvailable());
        r.setTotalBookings(s.getTotalBookings()); r.setCreatedAt(s.getCreatedAt());
        return r;
    }
}
