package com.loopers.infrastructure.queue;

import com.loopers.domain.queue.EntryToken;
import com.loopers.domain.queue.EntryTokenRepository;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class EntryTokenRedisStore implements EntryTokenRepository {

    static final String KEY_PREFIX = "entry-token:";
    static final Duration TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;

    public EntryTokenRedisStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(EntryToken token) {
        redisTemplate.opsForValue().set(key(token.getUserId()), token.getToken(), TTL);
    }

    @Override
    public Optional<EntryToken> findByUserId(Long userId) {
        String token = redisTemplate.opsForValue().get(key(userId));
        if (token == null) {
            return Optional.empty();
        }
        return Optional.of(EntryToken.of(userId, token));
    }

    @Override
    public void deleteByUserId(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
