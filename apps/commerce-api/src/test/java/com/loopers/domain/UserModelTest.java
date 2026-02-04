package com.loopers.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.application.SignUpCommand;
import com.loopers.support.error.CoreException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserModelTest {

    @DisplayName("UserModel 생성 테스트")
    @Test
    void success_create_userModel() {
        SignUpCommand command = new SignUpCommand(
            "user123",
            "rawPassword",
            LocalDate.of(1991, 12, 3),
            "김용권",
            "yk@google.com"
        );
        String encodedPw = "encoded_hash";

        UserModel user = UserModel.create(command, encodedPw);

        assertThat(user.getLoginId()).isEqualTo(command.getLoginId());
        assertThat(user.getPassword()).isEqualTo(encodedPw);
        assertThat(user.getBirthDate()).isEqualTo(command.getBirthDate());
        assertThat(user.getName()).isEqualTo(command.getName());
        assertThat(user.getEmail()).isEqualTo(command.getEmail());
    }

    @DisplayName("loginId가 null이면 guard에서 예외가 발생한다.")
    @Test
    void guard_fail_when_loginId_is_null() {
        SignUpCommand command = new SignUpCommand(null, "rawPassword", LocalDate.of(1991, 12, 3), "김용권", "yk@google.com");
        UserModel user = UserModel.create(command, "encoded_hash");

        assertThatThrownBy(user::guard).isInstanceOf(CoreException.class);
    }

    @DisplayName("password가 null이면 guard에서 예외가 발생한다.")
    @Test
    void guard_fail_when_password_is_null() {
        SignUpCommand command = new SignUpCommand("user123", "rawPassword", LocalDate.of(1991, 12, 3), "김용권", "yk@google.com");
        UserModel user = UserModel.create(command, null);

        assertThatThrownBy(user::guard).isInstanceOf(CoreException.class);
    }

    @DisplayName("birthDate가 null이면 guard에서 예외가 발생한다.")
    @Test
    void guard_fail_when_birthDate_is_null() {
        SignUpCommand command = new SignUpCommand("user123", "rawPassword", null, "김용권", "yk@google.com");
        UserModel user = UserModel.create(command, "encoded_hash");

        assertThatThrownBy(user::guard).isInstanceOf(CoreException.class);
    }

    @DisplayName("name이 null이면 guard에서 예외가 발생한다.")
    @Test
    void guard_fail_when_name_is_null() {
        SignUpCommand command = new SignUpCommand("user123", "rawPassword", LocalDate.of(1991, 12, 3), null, "yk@google.com");
        UserModel user = UserModel.create(command, "encoded_hash");

        assertThatThrownBy(user::guard).isInstanceOf(CoreException.class);
    }

    @DisplayName("email이 null이면 guard에서 예외가 발생한다.")
    @Test
    void guard_fail_when_email_is_null() {
        SignUpCommand command = new SignUpCommand("user123", "rawPassword", LocalDate.of(1991, 12, 3), "김용권", null);
        UserModel user = UserModel.create(command, "encoded_hash");

        assertThatThrownBy(user::guard).isInstanceOf(CoreException.class);
    }
}
