package com.miniproject1.miniproject1.program.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "program_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProgramDocument {

    @Id
    @Column(name = "pblanc_id")
    private String pblancId;

    @Column(name = "original_text", columnDefinition = "LONGTEXT")
    private String originalText;

    @Column(name = "doc_hash", length = 64)
    private String docHash;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
