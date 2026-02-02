package kr.yonggeon.claudebot.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final TelegramClient telegramClient;

    /**
     * Sends a simple text message.
     */
    public void sendText(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId={}: {}", chatId, e.getMessage(), e);
        }
    }

    /**
     * Sends a text message with an inline keyboard.
     */
    public void sendTextWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message with keyboard to chatId={}: {}", chatId, e.getMessage(), e);
        }
    }

    /**
     * Sends a text message with Markdown formatting.
     */
    public void sendMarkdown(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("Markdown")
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send markdown message to chatId={}: {}", chatId, e.getMessage(), e);
        }
    }

    /**
     * Sends a text message with HTML formatting.
     */
    public void sendHtml(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send HTML message to chatId={}: {}", chatId, e.getMessage(), e);
        }
    }

    /**
     * Edits an existing message.
     */
    public void editMessage(Long chatId, Integer messageId, String text) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .build();

        try {
            telegramClient.execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to edit message: chatId={}, messageId={}: {}", chatId, messageId, e.getMessage(), e);
        }
    }

    /**
     * Edits an existing message with keyboard.
     */
    public void editMessageWithKeyboard(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard)
                .build();

        try {
            telegramClient.execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to edit message with keyboard: chatId={}, messageId={}: {}", chatId, messageId, e.getMessage(), e);
        }
    }

    /**
     * Answers a callback query to remove the loading state.
     */
    public void answerCallback(String callbackQueryId) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();

        try {
            telegramClient.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback query: {}", e.getMessage(), e);
        }
    }

    /**
     * Answers a callback query with a notification text.
     */
    public void answerCallbackWithText(String callbackQueryId, String text) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .build();

        try {
            telegramClient.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback query with text: {}", e.getMessage(), e);
        }
    }

    /**
     * Answers a callback query with an alert (pop-up notification).
     */
    public void answerCallbackWithAlert(String callbackQueryId, String text) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .showAlert(true)
                .build();

        try {
            telegramClient.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback query with alert: {}", e.getMessage(), e);
        }
    }
}
