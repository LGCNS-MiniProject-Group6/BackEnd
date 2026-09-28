package com.miniproject1.miniproject1.interest.service;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.interest.dto.response.InterestProgramResponse;
import com.miniproject1.miniproject1.interest.entity.InterestProgram;
import com.miniproject1.miniproject1.interest.repository.InterestProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestProgramService {

    private final InterestProgramRepository interestProgramRepository;

    @Transactional
    public InterestProgramResponse add(String email, String programId) {
        if (interestProgramRepository.existsByEmailAndProgramId(email, programId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 등록한 관심공고입니다.");
        }

        try {
            InterestProgram saved = interestProgramRepository.save(
                    InterestProgram.builder().email(email).programId(programId).build());
            return InterestProgramResponse.from(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 등록한 관심공고입니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<InterestProgramResponse> findAll(String email) {
        return interestProgramRepository.findAllByEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(InterestProgramResponse::from)
                .toList();
    }

    @Transactional
    public void delete(String email, String programId) {
        if (interestProgramRepository.deleteByEmailAndProgramId(email, programId) == 0) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "등록된 관심공고가 없습니다.");
        }
    }
}
