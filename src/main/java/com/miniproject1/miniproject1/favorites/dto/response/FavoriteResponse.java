package com.miniproject1.miniproject1.favorites.dto.response;

import com.miniproject1.miniproject1.favorites.entity.Favorite;

import java.time.LocalDateTime;

public record FavoriteResponse(
        Long id,
        String programId,
        LocalDateTime createdAt
) {
    public static FavoriteResponse from(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getId(),
                favorite.getProgramId(),
                favorite.getCreatedAt());
    }
}
