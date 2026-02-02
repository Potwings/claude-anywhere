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
public class ArchiveCommand implements CommandHandler {

    private final ProjectService projectService;
    private final SessionService sessionService;
    private final MessageSender messageSender;

    @Override
    public String getCommand() {
        return "archive";
    }

    @Override
    public String getDescription() {
        return "Archive a project";
    }

    @Override
    public void handle(Message message, User user, String args) {
        Long chatId = message.getChatId();

        if (args == null || args.isBlank()) {
            messageSender.sendText(chatId, "Usage: /archive <project-name>\n\nUse /projects to see your projects.");
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
                    String.format("Project '%s' is already archived.", projectName));
            return;
        }

        projectService.archiveProject(project.getId());

        // Clear current project if this was the selected project
        Optional<Project> currentProject = sessionService.getCurrentProject(user.getId());
        if (currentProject.isPresent() && currentProject.get().getId().equals(project.getId())) {
            sessionService.clearCurrentProject(user.getId());
        }

        String responseMessage = String.format("""
                Project '%s' has been archived.

                Archived projects are not deleted and can be viewed with /projects.
                """, projectName);

        messageSender.sendText(chatId, responseMessage);
        log.info("Project archived: userId={}, projectId={}, projectName={}", user.getId(), project.getId(), projectName);
    }
}
