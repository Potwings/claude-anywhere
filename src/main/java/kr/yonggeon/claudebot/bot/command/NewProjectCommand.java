package kr.yonggeon.claudebot.bot.command;

import kr.yonggeon.claudebot.domain.Project;
import kr.yonggeon.claudebot.domain.User;
import kr.yonggeon.claudebot.exception.DuplicateProjectException;
import kr.yonggeon.claudebot.service.ProjectService;
import kr.yonggeon.claudebot.service.SessionService;
import kr.yonggeon.claudebot.util.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewProjectCommand implements CommandHandler {

    private final ProjectService projectService;
    private final SessionService sessionService;
    private final MessageSender messageSender;

    @Override
    public String getCommand() {
        return "newproject";
    }

    @Override
    public String getDescription() {
        return "Create a new project";
    }

    @Override
    public void handle(Message message, User user, String args) {
        Long chatId = message.getChatId();

        if (args == null || args.isBlank()) {
            messageSender.sendText(chatId, "Usage: /newproject <project-name>\n\nExample: /newproject my-api");
            return;
        }

        String projectName = args.trim();

        // Validate project name
        if (!isValidProjectName(projectName)) {
            messageSender.sendText(chatId,
                    "Invalid project name. Use only letters, numbers, hyphens, and underscores.");
            return;
        }

        try {
            Project project = projectService.createProject(user.getId(), projectName);

            // Auto-select the new project
            sessionService.selectProject(user.getId(), project.getId());

            String responseMessage = String.format("""
                    Project '%s' created successfully!

                    The project has been automatically selected as your current project.

                    Use /current to see project details.
                    """, projectName);

            messageSender.sendText(chatId, responseMessage);
            log.info("Project created: userId={}, projectName={}", user.getId(), projectName);

        } catch (DuplicateProjectException e) {
            messageSender.sendText(chatId,
                    String.format("A project named '%s' already exists. Please choose a different name.", projectName));
        }
    }

    private boolean isValidProjectName(String name) {
        // Allow letters, numbers, hyphens, and underscores
        return name.matches("^[a-zA-Z0-9_-]+$") && name.length() <= 100;
    }
}
