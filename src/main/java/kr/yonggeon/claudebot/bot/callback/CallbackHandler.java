package kr.yonggeon.claudebot.bot.callback;

import kr.yonggeon.claudebot.bot.command.DeleteCommand;
import kr.yonggeon.claudebot.domain.Project;
import kr.yonggeon.claudebot.domain.User;
import kr.yonggeon.claudebot.service.ProjectService;
import kr.yonggeon.claudebot.service.SessionService;
import kr.yonggeon.claudebot.service.UserService;
import kr.yonggeon.claudebot.util.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private static final String SELECT_PROJECT_PREFIX = "select_project:";
    private static final String CONFIRM_DELETE_PREFIX = "confirm_delete:";
    private static final String CANCEL_DELETE = "cancel_delete";

    private final UserService userService;
    private final ProjectService projectService;
    private final SessionService sessionService;
    private final MessageSender messageSender;
    private final DeleteCommand deleteCommand;

    public void handle(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        User user = userService.getOrCreateUser(callbackQuery.getFrom());

        log.debug("Handling callback: data={}, userId={}", data, user.getId());

        try {
            if (data.startsWith(SELECT_PROJECT_PREFIX)) {
                handleSelectProject(chatId, user, data);
            } else if (data.startsWith(CONFIRM_DELETE_PREFIX)) {
                handleConfirmDelete(chatId, user, data);
            } else if (data.equals(CANCEL_DELETE)) {
                handleCancelDelete(chatId);
            } else {
                log.warn("Unknown callback data: {}", data);
                messageSender.sendText(chatId, "Unknown action.");
            }

            // Answer callback query to remove loading state
            messageSender.answerCallback(callbackQuery.getId());

        } catch (Exception e) {
            log.error("Error handling callback: {}", e.getMessage(), e);
            messageSender.sendText(chatId, "An error occurred. Please try again.");
            messageSender.answerCallback(callbackQuery.getId());
        }
    }

    private void handleSelectProject(Long chatId, User user, String data) {
        String projectIdStr = data.substring(SELECT_PROJECT_PREFIX.length());
        Long projectId;

        try {
            projectId = Long.parseLong(projectIdStr);
        } catch (NumberFormatException e) {
            messageSender.sendText(chatId, "Invalid project selection.");
            return;
        }

        Optional<Project> projectOpt = projectService.findById(projectId);

        if (projectOpt.isEmpty()) {
            messageSender.sendText(chatId, "Project not found.");
            return;
        }

        Project project = projectOpt.get();

        // Verify ownership
        if (!project.getUserId().equals(user.getId())) {
            messageSender.sendText(chatId, "You don't have permission to select this project.");
            return;
        }

        if (project.getStatus() != Project.ProjectStatus.ACTIVE) {
            messageSender.sendText(chatId, "This project is not active.");
            return;
        }

        sessionService.selectProject(user.getId(), projectId);

        String responseMessage = String.format("""
                Project '%s' selected!

                You can now interact with Claude Code in this project context.
                """, project.getName());

        messageSender.sendText(chatId, responseMessage);
        log.info("Project selected via callback: userId={}, projectId={}", user.getId(), projectId);
    }

    private void handleConfirmDelete(Long chatId, User user, String data) {
        String projectIdStr = data.substring(CONFIRM_DELETE_PREFIX.length());
        Long projectId;

        try {
            projectId = Long.parseLong(projectIdStr);
        } catch (NumberFormatException e) {
            messageSender.sendText(chatId, "Invalid project.");
            return;
        }

        deleteCommand.confirmDelete(chatId, user, projectId);
    }

    private void handleCancelDelete(Long chatId) {
        deleteCommand.cancelDelete(chatId);
    }
}
