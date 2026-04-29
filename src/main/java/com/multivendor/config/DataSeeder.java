package com.multivendor.config;

import com.multivendor.admin.service.AdminService;
import com.multivendor.service.entity.ServiceCategory;
import com.multivendor.service.repository.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AdminService adminService;
    private final ServiceCategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCategories();
    }

    private void seedAdmin() {
        try {
            adminService.createDefaultAdmin();
            log.info("✅ Default admin seeded: admin@multivendor.com / Admin@123");
        } catch (Exception e) {
            log.warn("Admin seeding skipped: {}", e.getMessage());
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;

        List<ServiceCategory> categories = List.of(
            ServiceCategory.builder().name("Plumbing").description("Pipe repairs, leaks, faucets, drainage").icon("🔧").sortOrder(1).isActive(true).build(),
            ServiceCategory.builder().name("Electrician").description("Wiring, repairs, installations, switches").icon("⚡").sortOrder(2).isActive(true).build(),
            ServiceCategory.builder().name("Home Cleaning").description("Deep cleaning, bathroom, kitchen, full house").icon("🧹").sortOrder(3).isActive(true).build(),
            ServiceCategory.builder().name("Appliance Repair").description("AC, washing machine, fridge, microwave").icon("🔨").sortOrder(4).isActive(true).build(),
            ServiceCategory.builder().name("Farming Services").description("Land preparation, crop advice, irrigation").icon("🌾").sortOrder(5).isActive(true).build(),
            ServiceCategory.builder().name("Tractor Rental").description("Tractor hire for farming and land work").icon("🚜").sortOrder(6).isActive(true).build(),
            ServiceCategory.builder().name("Home Repair").description("Painting, carpentry, masonry, ceiling").icon("🏠").sortOrder(7).isActive(true).build(),
            ServiceCategory.builder().name("Beauty Services").description("Haircut, spa, facial, makeup, waxing").icon("💅").sortOrder(8).isActive(true).build(),
            ServiceCategory.builder().name("Technician").description("Computer repair, phone repair, CCTV, network").icon("💻").sortOrder(9).isActive(true).build(),
            ServiceCategory.builder().name("Pest Control").description("Cockroach, mosquito, termite, rat control").icon("🐛").sortOrder(10).isActive(true).build()
        );

        categoryRepository.saveAll(categories);
        log.info("✅ Seeded {} service categories", categories.size());
    }
}
