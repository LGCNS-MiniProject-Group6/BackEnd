package com.miniproject1.miniproject1.favorites.service;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.favorites.entity.Favorite;
import com.miniproject1.miniproject1.favorites.repository.FavoriteRepository;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String PBLANC_ID = "program-1";

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ProgramRepository programRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    private final Program program = Program.builder()
            .pblancId(PBLANC_ID)
            .title("온라인 판로지원 사업")
            .organization("중소벤처기업부")
            .applyEndDate(LocalDate.of(2026, 10, 4))
            .build();

    @Test
    void 관심공고를_등록한다() {
        when(programRepository.findById(PBLANC_ID)).thenReturn(Optional.of(program));
        when(favoriteRepository.existsByEmailAndProgram_PblancId(EMAIL, PBLANC_ID)).thenReturn(false);
        Favorite saved = Favorite.builder().email(EMAIL).program(program).build();
        when(favoriteRepository.saveAndFlush(any(Favorite.class))).thenReturn(saved);

        var response = favoriteService.add(EMAIL, PBLANC_ID);

        assertThat(response.pblancId()).isEqualTo(PBLANC_ID);
        assertThat(response.title()).isEqualTo("온라인 판로지원 사업");
        verify(favoriteRepository).saveAndFlush(any(Favorite.class));
    }

    @Test
    void 존재하지_않는_공고는_등록할수없다() {
        when(programRepository.findById(PBLANC_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.add(EMAIL, PBLANC_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROGRAM_NOT_FOUND);
        verify(favoriteRepository, never()).saveAndFlush(any(Favorite.class));
    }

    @Test
    void 중복_관심공고는_등록할수없다() {
        when(programRepository.findById(PBLANC_ID)).thenReturn(Optional.of(program));
        when(favoriteRepository.existsByEmailAndProgram_PblancId(EMAIL, PBLANC_ID)).thenReturn(true);

        assertThatThrownBy(() -> favoriteService.add(EMAIL, PBLANC_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FAVORITE_ALREADY_EXISTS);
        verify(favoriteRepository, never()).saveAndFlush(any(Favorite.class));
    }

    @Test
    void 내_관심공고를_공고정보와_함께_조회한다() {
        when(favoriteRepository.findAllByEmailOrderByCreatedAtDesc(EMAIL))
                .thenReturn(List.of(Favorite.builder().email(EMAIL).program(program).build()));

        var responses = favoriteService.findAll(EMAIL);

        assertThat(responses).singleElement().satisfies(response -> {
            assertThat(response.pblancId()).isEqualTo(PBLANC_ID);
            assertThat(response.organization()).isEqualTo("중소벤처기업부");
            assertThat(response.applyEndDate()).isEqualTo(LocalDate.of(2026, 10, 4));
        });
    }

    @Test
    void 관심공고를_삭제한다() {
        when(favoriteRepository.deleteByEmailAndProgram_PblancId(EMAIL, PBLANC_ID)).thenReturn(1L);

        favoriteService.delete(EMAIL, PBLANC_ID);

        verify(favoriteRepository).deleteByEmailAndProgram_PblancId(EMAIL, PBLANC_ID);
    }
}
