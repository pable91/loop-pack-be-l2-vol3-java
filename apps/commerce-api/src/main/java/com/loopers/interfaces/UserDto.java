package com.loopers.interfaces;

import com.loopers.application.UserInfo;

public class UserDto {

    public record SignUpResponse(Long id) {
        public static SignUpResponse from(UserInfo userInfo) {
            return new SignUpResponse(
                userInfo.id()
            );
        }
    }
}
