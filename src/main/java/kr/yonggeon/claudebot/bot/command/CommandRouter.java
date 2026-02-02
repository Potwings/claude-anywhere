package kr.yonggeon.claudebot.bot.command;

import kr.yonggeon.claudebot.domain.User;
import kr.yonggeon.claudebot.service.UserService;
import kr.yonggeon.claudebot.util.MessageSender;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandRouter {

    private final List<CommandHandler> handlers;
    private final UserService userService;
    private final MessageSender messageSender;

    private final Map<String, CommandHandler> commandMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (CommandHandler handler : handlers) {
            commandMap.put(handler.getCommand().toLowerCase(), handler);
            log.debug("Registered command handler: /{}", handler.getCommand());
        }
        log.info("Registered {} command handlers", commandMap.size());
    }

    public void route(Message message) {
        String text = message.getText().trim();
        Long chatId = message.getChatId();

        // Parse command and arguments
        String[] parts = text.split("\\s+", 2);
        String commandPart = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        // Remove leading slash and bot username suffix if present
        String command = commandPart.substring(1); // Remove '/'
        int atIndex = command.indexOf('@');
        if (atIndex > 0) {
            command = command.substring(0, atIndex);
        }

        // Find handler
        CommandHandler handler = commandMap.get(command);
        if (handler == null) {
            log.debug("Unknown command: {}", command);
            messageSender.sendText(chatId, "Unknown command. Use /help to see available commands.");
            return;
        }

        // Get or create user
        User user = userService.getOrCreateUser(message);
        log.debug("Routing command: /{} for user: {}", command, user.getTelegramId());

        // Execute handler
        try {
            handler.handle(message, user, args);
        } catch (Exception e) {
            log.error("Error executing command /{}: {}", command, e.getMessage(), e);
            messageSender.sendText(chatId, "Error executing command. Please try again.");
        }
    }

    public Map<String, CommandHandler> getCommandMap() {
        return Map.copyOf(commandMap);
    }
}
