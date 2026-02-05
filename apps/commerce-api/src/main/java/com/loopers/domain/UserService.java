package com.loopers.domain;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserModel save(UserModel userModel) {
        return userRepository.save(userModel);
    }

    @Transactional
    public UserModel createUser(String loginId, String rawPassword, java.time.LocalDate birthDate, String name, String email) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        UserModel user = UserModel.create(loginId, encodedPassword, birthDate, name, email);
        return userRepository.save(user);
    }

    public UserModel authenticate(String loginId, String rawPassword) {
        UserModel user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다."));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다.");
        }
        return user;
    }

    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserModel findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
