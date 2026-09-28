package com.miniproject1.miniproject1.favorites.dto.response;

import com.miniproject1.miniproject1.favorites.entity.Favorite;
import com.miniproject1.miniproject1.program.entity.Program;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FavoriteResponse(
        Long favoriteId,
        String pblancId,
        String title,
        String organization,
        LocalDate applyEndDate,
        LocalDateTime createdAt
) {
    public static FavoriteResponse from(Favorite favorite) {
        Program program = favorite.getProgram();
        return new FavoriteResponse(
                favorite.getId(),
                program.getPblancId(),
                program.getTitle(),
                program.getOrganization(),
                program.getApplyEndDate(),
                favorite.getCreatedAt());
    }
}
