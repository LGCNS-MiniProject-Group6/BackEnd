package com.miniproject1.miniproject1.review.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "pblanc_id", length = 100, nullable = false)
    private String pblancId;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", length = 30, nullable = false)
    private ReviewStatus overallStatus;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "prompt_version", length = 50)
    private String promptVersion;

    @Column(name = "document_hash", length = 128)
    private String documentHash;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReviewCondition> conditions = new ArrayList<>();

    @Builder
    public Review(String email, String pblancId, ReviewStatus overallStatus, String model,
                  String promptVersion, String documentHash) {
        this.email = email;
        this.pblancId = pblancId;
        this.overallStatus = overallStatus;
        this.model = model;
        this.promptVersion = promptVersion;
        this.documentHash = documentHash;
    }

    public void addCondition(ReviewCondition condition) {
        conditions.add(condition);
        condition.assignReview(this);
    }
}
