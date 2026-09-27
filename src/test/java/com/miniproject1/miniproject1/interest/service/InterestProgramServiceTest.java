package com.miniproject1.miniproject1.interest.service;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.interest.entity.InterestProgram;
import com.miniproject1.miniproject1.interest.repository.InterestProgramRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterestProgramServiceTest {

    @Mock
    private InterestProgramRepository interestProgramRepository;

    @InjectMocks
    private InterestProgramService interestProgramService;

    @Test
    void 관심공고를_등록한다() {
        when(interestProgramRepository.existsByEmailAndProgramId("user@example.com", "program-1"))
                .thenReturn(false);
        InterestProgram saved = InterestProgram.builder()
                .email("user@example.com")
                .programId("program-1")
                .build();
        when(interestProgramRepository.save(any(InterestProgram.class))).thenReturn(saved);

        var response = interestProgramService.add("user@example.com", "program-1");

        assertThat(response.programId()).isEqualTo("program-1");
        verify(interestProgramRepository).save(any(InterestProgram.class));
    }

    @Test
    void 중복_관심공고는_등록할수없다() {
        when(interestProgramRepository.existsByEmailAndProgramId("user@example.com", "program-1"))
                .thenReturn(true);

        assertThatThrownBy(() -> interestProgramService.add("user@example.com", "program-1"))
                .isInstanceOf(BusinessException.class);
        verify(interestProgramRepository, never()).save(any(InterestProgram.class));
    }

    @Test
    void 내_관심공고를_조회한다() {
        when(interestProgramRepository.findAllByEmailOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(List.of(InterestProgram.builder()
                        .email("user@example.com")
                        .programId("program-1")
                        .build()));

        var responses = interestProgramService.findAll("user@example.com");

        assertThat(responses).singleElement()
                .extracting(response -> response.programId())
                .isEqualTo("program-1");
    }

    @Test
    void 관심공고를_삭제한다() {
        when(interestProgramRepository.deleteByEmailAndProgramId("user@example.com", "program-1"))
                .thenReturn(1L);

        interestProgramService.delete("user@example.com", "program-1");

        verify(interestProgramRepository).deleteByEmailAndProgramId("user@example.com", "program-1");
    }
}
