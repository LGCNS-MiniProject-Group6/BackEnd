package com.miniproject1.miniproject1.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_conditions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "condition_type", length = 50, nullable = false)
    private String conditionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private ReviewConditionStatus status;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "page")
    private Integer page;

    @Column(name = "evidence_text", columnDefinition = "TEXT")
    private String evidenceText;

    @Builder
    public ReviewCondition(String conditionType, ReviewConditionStatus status,
                           String reason, Integer page, String evidenceText) {
        this.conditionType = conditionType;
        this.status = status;
        this.reason = reason;
        this.page = page;
        this.evidenceText = evidenceText;
    }

    void assignReview(Review review) {
        this.review = review;
    }
}
