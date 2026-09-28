package com.miniproject1.miniproject1.favorites.service;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.favorites.dto.response.FavoriteResponse;
import com.miniproject1.miniproject1.favorites.entity.Favorite;
import com.miniproject1.miniproject1.favorites.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    @Transactional
    public FavoriteResponse add(String email, String programId) {
        if (favoriteRepository.existsByEmailAndProgramId(email, programId)) {
            throw new BusinessException(ErrorCode.FAVORITE_ALREADY_EXISTS);
        }

        try {
            Favorite saved = favoriteRepository.save(
                    Favorite.builder().email(email).programId(programId).build());
            return FavoriteResponse.from(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.FAVORITE_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> findAll(String email) {
        return favoriteRepository.findAllByEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    @Transactional
    public void delete(String email, String programId) {
        if (favoriteRepository.deleteByEmailAndProgramId(email, programId) == 0) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "등록된 관심공고가 없습니다.");
        }
    }
}
