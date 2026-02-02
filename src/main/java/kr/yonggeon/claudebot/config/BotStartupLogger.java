package kr.yonggeon.claudebot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotStartupLogger implements CommandLineRunner {

    private final TelegramBotConfig botConfig;

    @Override
    public void run(String... args) {
        log.info("Telegram Bot initialized: {}", botConfig.getUsername());
        log.info("Bot is now ready to receive messages.");
    }
}
