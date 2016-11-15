package org.coinage.core;

import org.slf4j.Logger;

/**
 * Created At: 2016-11-15
 */
public class LogTimer implements AutoCloseable
{
    private final Logger log;
    private final String name;
    private final long startTime;

    public LogTimer(Logger log, String name)
    {
        this.log = log;
        this.name = name;
        log.info(String.format("(Timer) %s start.", name));
        this.startTime = System.nanoTime();
    }

    @Override
    public void close()
    {
        long elapsedTime = System.nanoTime() - this.startTime;
        this.log.info(
                "(Timer) {} end: {}",
                this.name,
                Formatters.formatPrettyElapsed(((double) elapsedTime) / 1000_000));
    }
}
