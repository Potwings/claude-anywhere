package kr.yonggeon.claudebot.bot.keyboard;

import kr.yonggeon.claudebot.domain.Project;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class InlineKeyboardFactory {

    private static final String SELECT_PROJECT_PREFIX = "select_project:";
    private static final String CONFIRM_DELETE_PREFIX = "confirm_delete:";
    private static final String CANCEL_DELETE = "cancel_delete";

    /**
     * Creates a keyboard for selecting projects from a list.
     */
    public InlineKeyboardMarkup createProjectSelectionKeyboard(List<Project> projects) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (Project project : projects) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(project.getName())
                    .callbackData(SELECT_PROJECT_PREFIX + project.getId())
                    .build();
            rows.add(new InlineKeyboardRow(button));
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    /**
     * Creates a confirmation keyboard for delete action.
     */
    public InlineKeyboardMarkup createConfirmDeleteKeyboard(Long projectId) {
        InlineKeyboardButton confirmButton = InlineKeyboardButton.builder()
                .text("Yes, Delete")
                .callbackData(CONFIRM_DELETE_PREFIX + projectId)
                .build();

        InlineKeyboardButton cancelButton = InlineKeyboardButton.builder()
                .text("Cancel")
                .callbackData(CANCEL_DELETE)
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow(confirmButton, cancelButton);

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .build();
    }

    /**
     * Creates a simple yes/no confirmation keyboard.
     */
    public InlineKeyboardMarkup createYesNoKeyboard(String yesCallback, String noCallback) {
        InlineKeyboardButton yesButton = InlineKeyboardButton.builder()
                .text("Yes")
                .callbackData(yesCallback)
                .build();

        InlineKeyboardButton noButton = InlineKeyboardButton.builder()
                .text("No")
                .callbackData(noCallback)
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow(yesButton, noButton);

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .build();
    }

    /**
     * Creates a cancel button keyboard.
     */
    public InlineKeyboardMarkup createCancelKeyboard(String cancelCallback) {
        InlineKeyboardButton cancelButton = InlineKeyboardButton.builder()
                .text("Cancel")
                .callbackData(cancelCallback)
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow(cancelButton);

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .build();
    }
}
