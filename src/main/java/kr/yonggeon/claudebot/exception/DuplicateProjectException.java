package kr.yonggeon.claudebot.exception;

public class DuplicateProjectException extends BotException {

    public DuplicateProjectException(String message) {
        super(message);
    }

    public DuplicateProjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
