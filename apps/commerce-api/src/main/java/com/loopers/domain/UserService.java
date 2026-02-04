package com.loopers.domain;

import com.loopers.application.SignUpCommand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public void signUp(SignUpCommand command) {
        validatePasswordContent(command.getLoginPw(), command.getBirthDate());

        String encodedPw = passwordEncoder.encode(command.getLoginPw());

        userRepository.save(
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
