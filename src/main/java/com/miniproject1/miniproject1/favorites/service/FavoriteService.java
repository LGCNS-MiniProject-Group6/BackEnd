package com.miniproject1.miniproject1.favorites.service;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.favorites.dto.response.FavoriteResponse;
import com.miniproject1.miniproject1.favorites.entity.Favorite;
import com.miniproject1.miniproject1.favorites.repository.FavoriteRepository;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProgramRepository programRepository;

    @Transactional
    public FavoriteResponse add(String email, String pblancId) {
        Program program = programRepository.findById(pblancId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_NOT_FOUND));

        if (favoriteRepository.existsByEmailAndProgram_PblancId(email, pblancId)) {
            throw new BusinessException(ErrorCode.FAVORITE_ALREADY_EXISTS);
        }

        // 공고 존재는 위에서 확인했으므로, 여기서의 무결성 위반은 동시 요청에 의한 중복 등록입니다.
        try {
            Favorite saved = favoriteRepository.saveAndFlush(
                    Favorite.builder().email(email).program(program).build());
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
    public void delete(String email, String pblancId) {
        if (favoriteRepository.deleteByEmailAndProgram_PblancId(email, pblancId) == 0) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "등록된 관심공고가 없습니다.");
        }
    }
}
