package kr.yonggeon.claudebot.exception;

public class BotException extends RuntimeException {

    public BotException(String message) {
        super(message);
    }

    public BotException(String message, Throwable cause) {
        super(message, cause);
    }
}
