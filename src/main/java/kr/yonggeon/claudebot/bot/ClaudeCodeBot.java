package kr.yonggeon.claudebot.bot;

import kr.yonggeon.claudebot.bot.callback.CallbackHandler;
import kr.yonggeon.claudebot.bot.command.CommandRouter;
import kr.yonggeon.claudebot.config.TelegramBotConfig;
import kr.yonggeon.claudebot.util.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeCodeBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramBotConfig botConfig;
    private final CommandRouter commandRouter;
    private final CallbackHandler callbackHandler;
    private final MessageSender messageSender;

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        try {
            if (update.hasMessage()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallback(update.getCallbackQuery());
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
            handleError(update, e);
        }
    }

    private void handleMessage(Message message) {
        Long telegramId = message.getFrom().getId();
        Long chatId = message.getChatId();

        // Check if user is allowed
        if (!botConfig.isUserAllowed(telegramId)) {
            log.warn("Unauthorized access attempt: telegramId={}", telegramId);
            messageSender.sendText(chatId, "You are not authorized to use this bot.");
            return;
        }

        if (message.hasText()) {
            String text = message.getText().trim();

            if (text.startsWith("/")) {
                // Handle command
                commandRouter.route(message);
            } else {
                // Handle regular text (for future conversational features)
                log.debug("Received text message: chatId={}, text={}", chatId, text);
            }
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        Long telegramId = callbackQuery.getFrom().getId();

        // Check if user is allowed
        if (!botConfig.isUserAllowed(telegramId)) {
            log.warn("Unauthorized callback attempt: telegramId={}", telegramId);
            return;
        }

        callbackHandler.handle(callbackQuery);
    }

    private void handleError(Update update, Exception e) {
        Long chatId = null;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }

        if (chatId != null) {
            messageSender.sendText(chatId, "An error occurred. Please try again.");
        }
    }
}
