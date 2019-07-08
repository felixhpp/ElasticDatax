package com.example.demo.monitor;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.text.DateFormat;
import java.util.Date;

public class LogFilter extends Filter {
    @Override
    public int decide(LoggingEvent event) {
        LoggerMessage loggerMessage = new LoggerMessage(
                event.getMessage().toString(),
                DateFormat.getDateTimeInstance().format(new Date(event.timeStamp)),
                event.getThreadName(),
                event.getLoggerName(),
                event.getLevel().toString()
        );
        LoggerQueue.getInstance().push(loggerMessage);
        return 1;
    }
}
