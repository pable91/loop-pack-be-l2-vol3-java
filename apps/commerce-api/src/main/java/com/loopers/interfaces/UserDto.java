package com.loopers.interfaces;

import com.loopers.application.UserInfo;
import java.time.LocalDate;

public class UserDto {

    public record SignUpResponse(Long id) {
        public static SignUpResponse from(UserInfo userInfo) {
            return new SignUpResponse(
                userInfo.id()
            );
        }
    }

    public record MyInfoResponse(String loginId, String name, LocalDate birthDate, String email) {

        public static MyInfoResponse from(UserInfo userInfo) {
            return new MyInfoResponse(
                userInfo.loginId(),
                userInfo.name(),
                userInfo.birthDate(),
                userInfo.email()
            );
        }
    }
}
