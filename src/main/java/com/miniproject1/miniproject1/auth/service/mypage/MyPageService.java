package com.miniproject1.miniproject1.auth.service.mypage;

import com.miniproject1.miniproject1.auth.dto.mypage.request.ProfileUpdateRequest;
import com.miniproject1.miniproject1.auth.dto.mypage.response.ProfileResponse;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.user.entity.User;
import com.miniproject1.miniproject1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(String email) {
        return ProfileResponse.from(findUser(email));
    }

    @Transactional
    public ProfileResponse updateMyProfile(String email, ProfileUpdateRequest request) {
        User user = findUser(email);
        user.updateProfile(request.name(), request.phone());
        return ProfileResponse.from(user);
    }

    private User findUser(String email) {
        return userRepository.findById(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
