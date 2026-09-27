package com.miniproject1.miniproject1.interest.repository;

import com.miniproject1.miniproject1.interest.entity.InterestProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestProgramRepository extends JpaRepository<InterestProgram, Long> {

    boolean existsByEmailAndProgramId(String email, String programId);

    List<InterestProgram> findAllByEmailOrderByCreatedAtDesc(String email);

    long deleteByEmailAndProgramId(String email, String programId);
}
