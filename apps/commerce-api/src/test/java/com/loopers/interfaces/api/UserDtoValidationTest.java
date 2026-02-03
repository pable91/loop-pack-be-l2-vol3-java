package com.loopers.interfaces.api;


import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class UserDtoValidationTest {

    private static final String DEFAULT_LOGIN_ID = "kim";
    private static final String DEFAULT_PWD = "Password1";
    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(1991, 12, 3);
    private static final String DEFAULT_NAME = "김용권";
    private static final String DEFAULT_EMAIL = "yk@google.com";

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private UserSignUpRequestDto defaultDto() {
        return new UserSignUpRequestDto(
            DEFAULT_LOGIN_ID, DEFAULT_PWD, DEFAULT_BIRTH_DATE, DEFAULT_NAME, DEFAULT_EMAIL
        );
    }

    private UserSignUpRequestDto dtoWithEmail(String email) {
        return new UserSignUpRequestDto(
            DEFAULT_LOGIN_ID, DEFAULT_PWD, DEFAULT_BIRTH_DATE, DEFAULT_NAME, email
        );
    }

    private UserSignUpRequestDto dtoWithBirthDate(LocalDate birthDate) {
        return new UserSignUpRequestDto(
            DEFAULT_LOGIN_ID, DEFAULT_PWD, birthDate, DEFAULT_NAME, DEFAULT_EMAIL
        );
    }

    private UserSignUpRequestDto dtoWithName(String name) {
        return new UserSignUpRequestDto(
            DEFAULT_LOGIN_ID, DEFAULT_PWD, DEFAULT_BIRTH_DATE, name, DEFAULT_EMAIL
        );
    }

    private Set<ConstraintViolation<UserSignUpRequestDto>> validate(UserSignUpRequestDto dto) {
        return validator.validate(dto);
    }

    @DisplayName("이메일 검증")
    @Nested
    class EmailValidation {

        @Test
        @DisplayName("이메일 포맷이 맞으면 성공하는 테스트")
        void emailFormatSuccessTest() {
            UserSignUpRequestDto dto = defaultDto();

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("이메일 포맷이 안맞으면 실패하는 테스트")
        void emailFormatFailTest() {
            UserSignUpRequestDto dto = dtoWithEmail("ykadasdad");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("이메일에 null이 들어오면 실패하는 테스트")
        void emailFormatNullTest() {
            UserSignUpRequestDto dto = dtoWithEmail(null);

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).hasSize(1);
        }
    }

    @DisplayName("생년월일 검증")
    @Nested
    class BirthdayValidation {

        @Test
        @DisplayName("포맷이 맞으면 성공하는 테스트")
        void birthFormatSuccessTest() {
            UserSignUpRequestDto dto = defaultDto();

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("미래 날짜면 실패하는 테스트")
        void birthFormatDateIsFutureTest() {
            UserSignUpRequestDto dto = dtoWithBirthDate(LocalDate.now().plusDays(1));

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("null이면 실패하는 테스트")
        void birthFormatDateIsNullTest() {
            UserSignUpRequestDto dto = dtoWithBirthDate(null);

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isNotEmpty();
        }
    }

    @DisplayName("이름 검증")
    @Nested
    class NameValidation {

        @Test
        @DisplayName("올바른 한글 이름이면 검증에 통과한다")
        void validKoreanSuccessTest() {
            UserSignUpRequestDto dto = defaultDto();

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("올바른 영문 이름이면 검증에 통과한다")
        void validEnglishSuccessTest() {
            UserSignUpRequestDto dto = dtoWithName("John");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("한글과 영문이 섞인 이름이면 검증에 통과한다")
        void mixedKoreanAndEnglishSuccessTest() {
            UserSignUpRequestDto dto = dtoWithName("김John");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("공백이 포함된 이름이면 검증에 통과한다")
        void nameContainsSpaceSuccessTest() {
            UserSignUpRequestDto dto = dtoWithName("홍 길동");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("이름이 2자이면 검증에 통과한다")
        void nameIsMinLengthSuccessTest() {
            UserSignUpRequestDto dto = dtoWithName("김용");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("이름이 null이면 검증에 실패한다")
        void nameIsNullSuccessTest() {
            UserSignUpRequestDto dto = dtoWithName(null);

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.iterator().next().getMessage()).isEqualTo("이름은 필수입니다.");
        }

        @Test
        @DisplayName("이름이 빈 문자열이면 검증에 실패한다")
        void nameIsEmptySuccessTest() {
            UserSignUpRequestDto dto = dtoWithName("");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("이름이 공백만 있으면 검증에 실패한다")
        void nameFormatBlankTest() {
            UserSignUpRequestDto dto = dtoWithName("   ");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isNotEmpty();
            // NotBlank 또는 Pattern 위반 가능 (구현/순서에 따라 메시지 상이)
            assertThat(violations.iterator().next().getMessage())
                .isIn("이름은 필수입니다.", "이름은 한글, 영문, 공백만 입력 가능합니다.");
        }

        @Test
        @DisplayName("이름이 1자이면 검증에 실패한다")
        void nameFormatTooShortTest() {
            UserSignUpRequestDto dto = dtoWithName("김");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.iterator().next().getMessage()).isEqualTo("이름은 2자 이상 30자 이하여야 합니다.");
        }

        @Test
        @DisplayName("이름이 11자 이상이면 검증에 실패한다")
        void nameFormatTooLongTest() {
            UserSignUpRequestDto dto = dtoWithName("가나다라마바사아자차카");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.iterator().next().getMessage()).isEqualTo("이름은 2자 이상 30자 이하여야 합니다.");
        }

        @Test
        @DisplayName("이름에 숫자가 포함되면 검증에 실패한다")
        void nameFormatContainsNumberTest() {
            UserSignUpRequestDto dto = dtoWithName("김용권1");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.iterator().next().getMessage()).isEqualTo("이름은 한글, 영문, 공백만 입력 가능합니다.");
        }

        @Test
        @DisplayName("이름에 특수문자가 포함되면 검증에 실패한다")
        void nameFormatContainsSpecialCharacterTest() {
            UserSignUpRequestDto dto = dtoWithName("김용권!");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.iterator().next().getMessage()).isEqualTo("이름은 한글, 영문, 공백만 입력 가능합니다.");
        }

        @Test
        @DisplayName("이름에 하이픈이 포함되면 검증에 실패한다")
        void nameFormatContainsHyphenTest() {
            UserSignUpRequestDto dto = dtoWithName("김-용권");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.iterator().next().getMessage()).isEqualTo("이름은 한글, 영문, 공백만 입력 가능합니다.");
        }

        @Test
        @DisplayName("이름에 점이 포함되면 검증에 실패한다")
        void nameFormatContainsDotTest() {
            UserSignUpRequestDto dto = dtoWithName("김.용권");

            Set<ConstraintViolation<UserSignUpRequestDto>> violations = validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.iterator().next().getMessage()).isEqualTo("이름은 한글, 영문, 공백만 입력 가능합니다.");
        }
    }
}
