package kr.yonggeon.claudebot.bot.command;

import kr.yonggeon.claudebot.domain.User;
import kr.yonggeon.claudebot.service.SessionService;
import kr.yonggeon.claudebot.util.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartCommand implements CommandHandler {

    private final SessionService sessionService;
    private final MessageSender messageSender;

    @Override
    public String getCommand() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Start the bot and see welcome message";
    }

    @Override
    public void handle(Message message, User user, String args) {
        Long chatId = message.getChatId();

        // Ensure session exists
        sessionService.getOrCreateSession(user.getId());

        String welcomeMessage = String.format("""
                Welcome to Claude Code Telegram Bot, %s!

                This bot allows you to remotely control Claude Code running on your WSL2 environment.

                Getting Started:
                1. Create a project with /newproject <name>
                2. Select it with /select <name>
                3. Start interacting with Claude Code!

                Use /help to see all available commands.
                """,
                user.getFirstName() != null ? user.getFirstName() : "there");

        messageSender.sendText(chatId, welcomeMessage);
        log.info("User started bot: telegramId={}", user.getTelegramId());
    }
}
