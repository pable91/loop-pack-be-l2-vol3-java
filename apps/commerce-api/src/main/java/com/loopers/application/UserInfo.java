package com.loopers.application;

import com.loopers.domain.UserModel;

public record UserInfo(Long id, String name, String email) {

    public static UserInfo from(UserModel userModel) {
        return new UserInfo(
            userModel.getId(),
            userModel.getName(),
            userModel.getEmail()
        );
    }
}
