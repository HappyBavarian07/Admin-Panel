package de.happybavarian07.events;

public class NotAPanelEventException extends Exception {
    public NotAPanelEventException() {
    }

    public NotAPanelEventException(String message) {
        super(message);
    }

    public NotAPanelEventException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAPanelEventException(Throwable cause) {
        super(cause);
    }

    public NotAPanelEventException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
