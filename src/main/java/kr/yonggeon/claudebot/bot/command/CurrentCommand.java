package kr.yonggeon.claudebot.bot.command;

import kr.yonggeon.claudebot.domain.Project;
import kr.yonggeon.claudebot.domain.User;
import kr.yonggeon.claudebot.service.SessionService;
import kr.yonggeon.claudebot.util.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentCommand implements CommandHandler {

    private final SessionService sessionService;
    private final MessageSender messageSender;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String getCommand() {
        return "current";
    }

    @Override
    public String getDescription() {
        return "Show current project information";
    }

    @Override
    public void handle(Message message, User user, String args) {
        Long chatId = message.getChatId();

        Optional<Project> currentProject = sessionService.getCurrentProject(user.getId());

        if (currentProject.isEmpty()) {
            messageSender.sendText(chatId,
                    "No project selected.\n\nUse /projects to see your projects or /newproject to create one.");
            return;
        }

        Project project = currentProject.get();

        StringBuilder info = new StringBuilder();
        info.append("Current Project:\n\n");
        info.append(String.format("Name: %s\n", project.getName()));
        info.append(String.format("Status: %s\n", project.getStatus()));

        if (project.getDescription() != null && !project.getDescription().isBlank()) {
            info.append(String.format("Description: %s\n", project.getDescription()));
        }

        if (project.getWorkingDirectory() != null && !project.getWorkingDirectory().isBlank()) {
            info.append(String.format("Working Directory: %s\n", project.getWorkingDirectory()));
        }

        if (project.getCreatedAt() != null) {
            info.append(String.format("Created: %s\n", project.getCreatedAt().format(DATE_FORMATTER)));
        }

        if (project.getUpdatedAt() != null) {
            info.append(String.format("Last Updated: %s\n", project.getUpdatedAt().format(DATE_FORMATTER)));
        }

        messageSender.sendText(chatId, info.toString());
    }
}
