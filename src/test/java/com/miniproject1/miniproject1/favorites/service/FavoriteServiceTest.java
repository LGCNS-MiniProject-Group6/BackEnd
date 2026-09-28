package com.miniproject1.miniproject1.favorites.service;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.favorites.entity.Favorite;
import com.miniproject1.miniproject1.favorites.repository.FavoriteRepository;
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
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    void 관심공고를_등록한다() {
        when(favoriteRepository.existsByEmailAndProgramId("user@example.com", "program-1"))
                .thenReturn(false);
        Favorite saved = Favorite.builder()
                .email("user@example.com")
                .programId("program-1")
                .build();
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(saved);

        var response = favoriteService.add("user@example.com", "program-1");

        assertThat(response.programId()).isEqualTo("program-1");
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    void 중복_관심공고는_등록할수없다() {
        when(favoriteRepository.existsByEmailAndProgramId("user@example.com", "program-1"))
                .thenReturn(true);

        assertThatThrownBy(() -> favoriteService.add("user@example.com", "program-1"))
                .isInstanceOf(BusinessException.class);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void 내_관심공고를_조회한다() {
        when(favoriteRepository.findAllByEmailOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(List.of(Favorite.builder()
                        .email("user@example.com")
                        .programId("program-1")
                        .build()));

        var responses = favoriteService.findAll("user@example.com");

        assertThat(responses).singleElement()
                .extracting(response -> response.programId())
                .isEqualTo("program-1");
    }

    @Test
    void 관심공고를_삭제한다() {
        when(favoriteRepository.deleteByEmailAndProgramId("user@example.com", "program-1"))
                .thenReturn(1L);

        favoriteService.delete("user@example.com", "program-1");

        verify(favoriteRepository).deleteByEmailAndProgramId("user@example.com", "program-1");
    }
}
