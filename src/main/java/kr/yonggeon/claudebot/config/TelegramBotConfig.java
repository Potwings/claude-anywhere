package kr.yonggeon.claudebot.config;

import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
@Getter
@Setter
public class TelegramBotConfig {

    private String token;
    private String username;
    private String allowedUsers;

    @Bean
    public TelegramClient telegramClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        return new OkHttpTelegramClient(okHttpClient, token);
    }

    public Set<Long> getAllowedUserIds() {
        if (allowedUsers == null || allowedUsers.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(allowedUsers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    public boolean isUserAllowed(Long telegramId) {
        Set<Long> allowed = getAllowedUserIds();
        return allowed.isEmpty() || allowed.contains(telegramId);
    }
}
