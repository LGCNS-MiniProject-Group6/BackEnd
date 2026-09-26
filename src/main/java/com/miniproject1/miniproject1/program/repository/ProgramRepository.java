package com.miniproject1.miniproject1.program.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniproject1.miniproject1.program.entity.Program;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, String> {

    // pblanc_id 기준 존재 여부 및 조회
    Optional<Program> findByPblancId(String pblancId);
}