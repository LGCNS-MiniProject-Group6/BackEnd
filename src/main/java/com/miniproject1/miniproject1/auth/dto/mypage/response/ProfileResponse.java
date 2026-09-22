package com.miniproject1.miniproject1.auth.dto.mypage.response;

import com.miniproject1.miniproject1.user.entity.User;

public record ProfileResponse(String email, String name, String phone) {

    public static ProfileResponse from(User user) {
        return new ProfileResponse(user.getEmail(), user.getName(), user.getPhoneNumber());
    }
}
