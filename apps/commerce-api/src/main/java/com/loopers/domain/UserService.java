package com.loopers.domain;

import com.loopers.interfaces.api.UsersSignUpRequestDto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;

    public void signUp(String loginId, String loginPw, UsersSignUpRequestDto requestDto) {
        validatePasswordContent(loginPw, requestDto.getBirthDate());

        String encodedPw = passwordEncoder.encode(loginPw);

        UserModel.create(
            loginId,
            encodedPw,
            requestDto
        );
    }

    private void validatePasswordContent(String password, LocalDate birthDate) {
        if (password == null || birthDate == null) {
            return;
        }

        String birthStr = birthDate.toString().replace("-", "");

        if (password.contains(birthStr)) {
            throw new CoreException(ErrorType.NOT_INCLUDE_BIRTH_IN_PASSWORD);
        }
    }
}
