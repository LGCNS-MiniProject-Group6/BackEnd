package com.miniproject1.miniproject1.program.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "programs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Program {

    @Id
    @Column(name = "pblanc_id")
    private String pblancId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "category")
    private String category;

    @Column(name = "organization")
    private String organization;

    @Column(name = "target_description", columnDefinition = "TEXT")
    private String targetDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ★ 원본 신청기간 텍스트 저장 컬럼 (VARCHAR 250 여유있게 설정)
    @Column(name = "raw_apply_period", length = 250)
    private String rawApplyPeriod;

    @Column(name = "apply_start_date")
    private LocalDate applyStartDate;

    @Column(name = "apply_end_date")
    private LocalDate applyEndDate;

    @Column(name = "api_updated_at")
    private String apiUpdatedAt;

    @Builder
    public Program(String pblancId, String title, String category, String organization,
            String targetDescription, String description, String rawApplyPeriod,
            LocalDate applyStartDate, LocalDate applyEndDate, String apiUpdatedAt) {
        this.pblancId = pblancId;
        this.title = title;
        this.category = category;
        this.organization = organization;
        this.targetDescription = targetDescription;
        this.description = description;
        this.rawApplyPeriod = rawApplyPeriod;
        this.applyStartDate = applyStartDate;
        this.applyEndDate = applyEndDate;
        this.apiUpdatedAt = apiUpdatedAt;
    }

    public void updateFrom(Program updated) {
        this.title = updated.getTitle();
        this.category = updated.getCategory();
        this.organization = updated.getOrganization();
        this.targetDescription = updated.getTargetDescription();
        this.description = updated.getDescription();
        this.rawApplyPeriod = updated.getRawApplyPeriod(); // ★ 추가
        this.applyStartDate = updated.getApplyStartDate();
        this.applyEndDate = updated.getApplyEndDate();
        this.apiUpdatedAt = updated.getApiUpdatedAt();
    }
}