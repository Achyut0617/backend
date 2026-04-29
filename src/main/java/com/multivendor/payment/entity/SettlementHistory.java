package com.multivendor.payment.entity;

import com.multivendor.common.enums.SettlementStatus;
import com.multivendor.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "period_from")
    private LocalDate periodFrom;

    @Column(name = "period_to")
    private LocalDate periodTo;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "utr_number")
    private String utrNumber;

    @Column(name = "settled_by")
    private Long settledBy;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "remarks")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
