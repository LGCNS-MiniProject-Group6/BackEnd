package com.miniproject1.miniproject1.aireview;

import com.miniproject1.miniproject1.aireview.converter.AiReviewAnalysisJsonConverter;
import com.miniproject1.miniproject1.aireview.converter.BusinessProfileJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 검수 당시 사업정보 스냅샷과 검증된 모델 결과를 하나의 이력으로 저장합니다. */
@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AiReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "pblanc_id", length = 100, nullable = false)
    private String pblancId;

    @Convert(converter = BusinessProfileJsonConverter.class)
    @Column(name = "business_snapshot", columnDefinition = "json", nullable = false)
    private AiBusinessProfile businessSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AiReviewStatus status;

    @Convert(converter = AiReviewAnalysisJsonConverter.class)
    @Column(name = "result_detail", columnDefinition = "json", nullable = false)
    private AiReviewAnalysis resultDetail;

    @Column(name = "is_outdated", nullable = false)
    private boolean outdated;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AiReview(String email, String pblancId, AiBusinessProfile businessSnapshot,
                     AiReviewStatus status, AiReviewAnalysis resultDetail) {
        this.email = email;
        this.pblancId = pblancId;
        this.businessSnapshot = businessSnapshot;
        this.status = status;
        this.resultDetail = resultDetail;
        this.outdated = false;
    }
}
