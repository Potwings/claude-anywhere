package kr.yonggeon.claudebot.exception;

public class ProjectNotFoundException extends BotException {

    public ProjectNotFoundException(String message) {
        super(message);
    }

    public ProjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
