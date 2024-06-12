package de.happybavarian07.webui.log;

import de.happybavarian07.webui.websockethandlers.ConsoleLogWebSocketHandler;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

@Plugin(name = "ConsoleLogAppender", category = "Core", elementType = "appender", printObject = true)
public class Log4JAppender extends AbstractAppender {
    public Log4JAppender() {
        super("ConsoleLogAppender", null,
                PatternLayout.newBuilder()
                        .withPattern("[%d{HH:mm:ss}] [%t/%p]: %m%n") // Adjust the pattern here
                        .withCharset(StandardCharsets.UTF_8)
                        .build(),
                false, null);
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void append(LogEvent e) {
        // Format the log event
        String logMessage = String.format("[%s] [%s/%s]: %s",
                new SimpleDateFormat("HH:mm:ss").format(new Date(e.getTimeMillis())),
                e.getThreadName(),
                e.getLevel(),
                e.getMessage().getFormattedMessage());

        // Check if an exception was thrown
        if (e.getThrownProxy() != null || e.getThrown() != null) {
            // Get the stack trace
            String stackTrace = e.getThrownProxy().getExtendedStackTraceAsString();

            // Append the stack trace to the log message
            logMessage += "\n" + stackTrace;
        }

        // Send the formatted log event
        ConsoleLogWebSocketHandler.getInstance().broadcastMessage(logMessage);
    }
}