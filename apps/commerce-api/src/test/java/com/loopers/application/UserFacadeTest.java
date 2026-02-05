package com.loopers.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.loopers.domain.UserModel;
import com.loopers.domain.UserService;
import com.loopers.support.error.CoreException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserFacadeTest {

    @InjectMocks
    private UserFacade userFacade;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void success_signup() {

        String rawPw = "securePassword!@";
        String encodedPw = "encoded_hash";
        SignUpCommand signUpCommand = new SignUpCommand(
            "user123",
            rawPw,
            LocalDate.of(1995, 1, 1),
            "kim",
            "yk@naver.com"
        );

        given(passwordEncoder.encode(rawPw)).willReturn(encodedPw);
        UserModel savedUser = UserModel.create(signUpCommand, encodedPw);
        given(userService.save(any(UserModel.class))).willReturn(savedUser);

        userFacade.signUp(signUpCommand);

        verify(passwordEncoder, times(1)).encode(rawPw);
        verify(userService, times(1)).save(any(UserModel.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호에 생년월일 포함되어있으면 예외 발생")
    void fail_with_birthDate() {

        String rawPw = "pw19911203!!"; // 생일 포함
        SignUpCommand signUpCommand = new SignUpCommand(
            "user123",
            rawPw,
            LocalDate.of(1991, 12, 3),
            "kim",
            "yk@naver.com"
        );

        assertThatThrownBy(() -> userFacade.signUp(signUpCommand))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("생년월일을 포함할 수 없습니다");
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 가입되어 있으면 예외 발생")
    void fail_already_signUp() {

        String rawPw = "securePassword!@";
        SignUpCommand signUpCommand = new SignUpCommand(
            "user123",
            rawPw,
            LocalDate.of(1995, 1, 1),
            "kim",
            "yk@naver.com"
        );

        given(userService.existsByEmail("yk@naver.com")).willReturn(true);

        assertThatThrownBy(() -> userFacade.signUp(signUpCommand))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("이미 가입되어 있는 아이디 입니다.");
    }
}
