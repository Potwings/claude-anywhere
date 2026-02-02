package kr.yonggeon.claudebot.bot.command;

import kr.yonggeon.claudebot.bot.keyboard.InlineKeyboardFactory;
import kr.yonggeon.claudebot.domain.Project;
import kr.yonggeon.claudebot.domain.User;
import kr.yonggeon.claudebot.service.ProjectService;
import kr.yonggeon.claudebot.service.SessionService;
import kr.yonggeon.claudebot.util.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectsCommand implements CommandHandler {

    private final ProjectService projectService;
    private final SessionService sessionService;
    private final MessageSender messageSender;
    private final InlineKeyboardFactory keyboardFactory;

    @Override
    public String getCommand() {
        return "projects";
    }

    @Override
    public String getDescription() {
        return "List all projects";
    }

    @Override
    public void handle(Message message, User user, String args) {
        Long chatId = message.getChatId();

        List<Project> activeProjects = projectService.findActiveProjects(user.getId());
        List<Project> archivedProjects = projectService.findArchivedProjects(user.getId());
        Optional<Project> currentProject = sessionService.getCurrentProject(user.getId());

        if (activeProjects.isEmpty() && archivedProjects.isEmpty()) {
            messageSender.sendText(chatId, "You don't have any projects yet.\n\nCreate one with /newproject <name>");
            return;
        }

        StringBuilder messageText = new StringBuilder();
        messageText.append("Your Projects:\n\n");

        if (!activeProjects.isEmpty()) {
            messageText.append("Active Projects:\n");
            for (Project project : activeProjects) {
                String marker = currentProject.map(p -> p.getId().equals(project.getId())).orElse(false)
                        ? " [SELECTED]" : "";
                messageText.append(String.format("  - %s%s\n", project.getName(), marker));
            }
            messageText.append("\n");
        }

        if (!archivedProjects.isEmpty()) {
            messageText.append("Archived Projects:\n");
            for (Project project : archivedProjects) {
                messageText.append(String.format("  - %s\n", project.getName()));
            }
            messageText.append("\n");
        }

        messageText.append("Select a project to work with:");

        // Create inline keyboard for project selection
        InlineKeyboardMarkup keyboard = keyboardFactory.createProjectSelectionKeyboard(activeProjects);

        messageSender.sendTextWithKeyboard(chatId, messageText.toString(), keyboard);
    }
}
