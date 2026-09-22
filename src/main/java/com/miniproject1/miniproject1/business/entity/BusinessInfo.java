package com.miniproject1.miniproject1.business.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.AccessLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.miniproject1.miniproject1.business.dto.request.BusinessInfoRequestDTO;

@Entity
@Table(name = "business_info")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BusinessInfo {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "industry", length = 50, nullable = false)
    private String industry;

    @Column(name = "region", length = 50, nullable = false)
    private String region;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Column(name = "employee_count", nullable = false)
    private Integer employeeCount;
    
    @Column(name = "annual_revenue", nullable = false)
    private Long annualRevenue;

    @Column(name = "is_default")
    private Boolean isDefault;

    @CreatedDate 
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void update(BusinessInfoRequestDTO request) {
    this.industry = request.getIndustry();
    this.region = request.getRegion();
    this.openingDate = request.getOpeningDate();
    this.employeeCount = request.getEmployeeCount();
    this.annualRevenue = request.getAnnualRevenue();
    }
}
