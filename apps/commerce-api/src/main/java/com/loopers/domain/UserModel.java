package com.loopers.domain;

import com.loopers.interfaces.api.UsersSignUpRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "users")
@NoArgsConstructor
public class UserModel extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @Comment("아이디")
    @Column(name = "login_id", nullable = false)
    private String loginId;

    @Comment("비밀번호")
    @Column(name = "password", nullable = false)
    private String password;

    @Comment("생년월일")
    @Column(name = "birth", nullable = false)
    private LocalDate birthDate;

    @Comment("이름")
    @Column(name = "name", nullable = false)
    private String name;

    @Comment("이메일")
    @Column(name = "email", nullable = false)
    private String email;

    @Builder
    public UserModel(String loginId, String encodedPw, LocalDate birthDate, String name, String email) {
        this.loginId = loginId;
        this.password = encodedPw;
        this.birthDate = birthDate;
        this.name = name;
        this.email = email;
    }

    public static UserModel create(String loginId, String encodedPw, UsersSignUpRequestDto requestDto) {
        return UserModel.builder()
            .loginId(loginId)
            .encodedPw(encodedPw)
            .birthDate(requestDto.getBirthDate())
            .name(requestDto.getName())
            .email(requestDto.getEmail())
            .build();
    }
}
