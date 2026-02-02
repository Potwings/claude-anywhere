package kr.yonggeon.claudebot.bot.command;

import kr.yonggeon.claudebot.domain.User;
import kr.yonggeon.claudebot.util.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class HelpCommand implements CommandHandler {

    private final MessageSender messageSender;

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show available commands";
    }

    @Override
    public void handle(Message message, User user, String args) {
        Long chatId = message.getChatId();

        StringBuilder helpText = new StringBuilder();
        helpText.append("Available Commands:\n\n");

        // Basic commands
        helpText.append("Basic:\n");
        helpText.append("/start - Start the bot\n");
        helpText.append("/help - Show this help message\n\n");

        // Project management
        helpText.append("Project Management:\n");
        helpText.append("/newproject <name> - Create a new project\n");
        helpText.append("/projects - List all projects\n");
        helpText.append("/select <name> - Select a project\n");
        helpText.append("/current - Show current project\n");
        helpText.append("/archive <name> - Archive a project\n");
        helpText.append("/delete <name> - Delete a project\n");

        messageSender.sendText(chatId, helpText.toString());
    }
}
