package com.miniproject1.miniproject1.program.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSummary {

    @Id
    @Column(name = "pblanc_id")
    private String pblancId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "pblanc_id")
    private Program program;

    @Column(columnDefinition = "TEXT")
    private String bizSummary;

    @Column(columnDefinition = "TEXT")
    private String targetDescription;

    @Column(columnDefinition = "TEXT")
    private String supportContent;

    @Column(columnDefinition = "TEXT")
    private String applyMethod;

    @Column(columnDefinition = "TEXT")
    private String requiredDocuments;

    @Column(columnDefinition = "TEXT")
    private String contactInfo;

    private LocalDateTime updatedAt;
}
