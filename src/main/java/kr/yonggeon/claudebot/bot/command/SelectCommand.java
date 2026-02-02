package kr.yonggeon.claudebot.bot.command;

import kr.yonggeon.claudebot.domain.Project;
import kr.yonggeon.claudebot.domain.User;
import kr.yonggeon.claudebot.service.ProjectService;
import kr.yonggeon.claudebot.service.SessionService;
import kr.yonggeon.claudebot.util.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectCommand implements CommandHandler {

    private final ProjectService projectService;
    private final SessionService sessionService;
    private final MessageSender messageSender;

    @Override
    public String getCommand() {
        return "select";
    }

    @Override
    public String getDescription() {
        return "Select a project to work with";
    }

    @Override
    public void handle(Message message, User user, String args) {
        Long chatId = message.getChatId();

        if (args == null || args.isBlank()) {
            messageSender.sendText(chatId, "Usage: /select <project-name>\n\nUse /projects to see your projects.");
            return;
        }

        String projectName = args.trim();

        Optional<Project> projectOpt = projectService.findByUserIdAndName(user.getId(), projectName);

        if (projectOpt.isEmpty()) {
            messageSender.sendText(chatId,
                    String.format("Project '%s' not found.\n\nUse /projects to see your projects.", projectName));
            return;
        }

        Project project = projectOpt.get();

        if (project.getStatus() == Project.ProjectStatus.ARCHIVED) {
            messageSender.sendText(chatId,
                    String.format("Project '%s' is archived. Unarchive it first to select it.", projectName));
            return;
        }

        sessionService.selectProject(user.getId(), project.getId());

        String responseMessage = String.format("""
                Project '%s' selected!

                You can now interact with Claude Code in this project context.

                Use /current to see project details.
                """, projectName);

        messageSender.sendText(chatId, responseMessage);
        log.info("Project selected: userId={}, projectId={}, projectName={}", user.getId(), project.getId(), projectName);
    }
}
