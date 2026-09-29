package com.miniproject1.miniproject1.program.repository;

import com.miniproject1.miniproject1.program.entity.ProgramDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramDocumentRepository extends JpaRepository<ProgramDocument, String> {
}
