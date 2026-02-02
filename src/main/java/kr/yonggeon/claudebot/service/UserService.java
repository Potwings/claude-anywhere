package kr.yonggeon.claudebot.service;

import kr.yonggeon.claudebot.domain.User;
import kr.yonggeon.claudebot.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Optional<User> findByTelegramId(Long telegramId) {
        return userMapper.findByTelegramId(telegramId);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userMapper.findById(id);
    }

    @Transactional
    public User getOrCreateUser(Message message) {
        org.telegram.telegrambots.meta.api.objects.User telegramUser = message.getFrom();
        Long telegramId = telegramUser.getId();

        return userMapper.findByTelegramId(telegramId)
                .map(existingUser -> updateUserIfNeeded(existingUser, telegramUser))
                .orElseGet(() -> createUser(telegramUser));
    }

    @Transactional
    public User getOrCreateUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        Long telegramId = telegramUser.getId();

        return userMapper.findByTelegramId(telegramId)
                .map(existingUser -> updateUserIfNeeded(existingUser, telegramUser))
                .orElseGet(() -> createUser(telegramUser));
    }

    private User createUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        User user = User.builder()
                .telegramId(telegramUser.getId())
                .username(telegramUser.getUserName())
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .languageCode(telegramUser.getLanguageCode())
                .isActive(true)
                .build();

        userMapper.insert(user);
        log.info("Created new user: telegramId={}, username={}", user.getTelegramId(), user.getUsername());
        return user;
    }

    private User updateUserIfNeeded(User existingUser, org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        boolean needsUpdate = false;

        if (!equals(existingUser.getUsername(), telegramUser.getUserName())) {
            existingUser.setUsername(telegramUser.getUserName());
            needsUpdate = true;
        }
        if (!equals(existingUser.getFirstName(), telegramUser.getFirstName())) {
            existingUser.setFirstName(telegramUser.getFirstName());
            needsUpdate = true;
        }
        if (!equals(existingUser.getLastName(), telegramUser.getLastName())) {
            existingUser.setLastName(telegramUser.getLastName());
            needsUpdate = true;
        }
        if (!equals(existingUser.getLanguageCode(), telegramUser.getLanguageCode())) {
            existingUser.setLanguageCode(telegramUser.getLanguageCode());
            needsUpdate = true;
        }

        if (needsUpdate) {
            userMapper.update(existingUser);
            log.debug("Updated user info: telegramId={}", existingUser.getTelegramId());
        }

        return existingUser;
    }

    private boolean equals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        userMapper.updateActiveStatus(userId, false);
        log.info("Deactivated user: id={}", userId);
    }

    @Transactional
    public void activateUser(Long userId) {
        userMapper.updateActiveStatus(userId, true);
        log.info("Activated user: id={}", userId);
    }
}
