package com.loopers.application;

import com.loopers.domain.UserModel;
import com.loopers.domain.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserFacade {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public void signUp(SignUpCommand command) {
        // 만약 또다른 검증 조건이 생긴다면
        validatePasswordContent(command.getLoginPw(), command.getBirthDate());

        String encodedPw = passwordEncoder.encode(command.getLoginPw());

        userService.save(
            UserModel.create(
                command,
                encodedPw
            )
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
