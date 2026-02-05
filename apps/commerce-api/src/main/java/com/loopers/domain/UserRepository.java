package com.loopers.domain;

import java.util.Optional;

public interface UserRepository {

    UserModel save(UserModel userModel);

    Optional<UserModel> findById(Long id);

    Optional<UserModel> findByLoginId(String loginId);

    Boolean existsByEmail(String email);
}
