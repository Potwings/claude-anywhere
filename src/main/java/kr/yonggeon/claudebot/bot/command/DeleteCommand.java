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

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteCommand implements CommandHandler {

    private final ProjectService projectService;
    private final SessionService sessionService;
    private final MessageSender messageSender;
    private final InlineKeyboardFactory keyboardFactory;

    @Override
    public String getCommand() {
        return "delete";
    }

    @Override
    public String getDescription() {
        return "Delete a project";
    }

    @Override
    public void handle(Message message, User user, String args) {
        Long chatId = message.getChatId();

        if (args == null || args.isBlank()) {
            messageSender.sendText(chatId, "Usage: /delete <project-name>\n\nUse /projects to see your projects.");
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

        // Ask for confirmation
        String confirmMessage = String.format("""
                Are you sure you want to delete project '%s'?

                This action cannot be undone.
                """, projectName);

        InlineKeyboardMarkup keyboard = keyboardFactory.createConfirmDeleteKeyboard(project.getId());

        messageSender.sendTextWithKeyboard(chatId, confirmMessage, keyboard);
    }

    /**
     * Called by CallbackHandler when user confirms deletion.
     */
    public void confirmDelete(Long chatId, User user, Long projectId) {
        Optional<Project> projectOpt = projectService.findById(projectId);

        if (projectOpt.isEmpty()) {
            messageSender.sendText(chatId, "Project not found or already deleted.");
            return;
        }

        Project project = projectOpt.get();

        // Verify ownership
        if (!project.getUserId().equals(user.getId())) {
            messageSender.sendText(chatId, "You don't have permission to delete this project.");
            return;
        }

        String projectName = project.getName();

        // Clear current project if this was the selected project
        Optional<Project> currentProject = sessionService.getCurrentProject(user.getId());
        if (currentProject.isPresent() && currentProject.get().getId().equals(projectId)) {
            sessionService.clearCurrentProject(user.getId());
        }

        projectService.deleteProject(projectId);

        messageSender.sendText(chatId, String.format("Project '%s' has been deleted.", projectName));
        log.info("Project deleted: userId={}, projectId={}, projectName={}", user.getId(), projectId, projectName);
    }

    /**
     * Called by CallbackHandler when user cancels deletion.
     */
    public void cancelDelete(Long chatId) {
        messageSender.sendText(chatId, "Delete cancelled.");
    }
}
