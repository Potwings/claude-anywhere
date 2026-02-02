package kr.yonggeon.claudebot.bot.command;

import kr.yonggeon.claudebot.domain.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface CommandHandler {

    /**
     * Returns the command name without the leading slash.
     * For example, "start" for /start command.
     */
    String getCommand();

    /**
     * Returns a brief description of the command for help text.
     */
    String getDescription();

    /**
     * Handles the command execution.
     *
     * @param message The incoming message containing the command
     * @param user    The authenticated user who sent the command
     * @param args    The command arguments (text after the command)
     */
    void handle(Message message, User user, String args);
}
