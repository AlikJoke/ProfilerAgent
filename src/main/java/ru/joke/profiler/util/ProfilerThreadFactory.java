package ru.joke.profiler.util;

import ru.joke.profiler.ProfilerAgent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.joke.profiler.util.ArgUtil.checkNotEmpty;

public final class ProfilerThreadFactory implements ThreadFactory {

    private static final Logger logger = Logger.getLogger(ProfilerAgent.class.getCanonicalName());

    private final String threadName;
    private final AtomicInteger threadCounter;

    public ProfilerThreadFactory(
            final String threadName,
            final boolean useCounters
    ) {
        this.threadName = checkNotEmpty(threadName, "threadName");
        this.threadCounter = useCounters ? new AtomicInteger(0) : null;
    }

    @Override
    public Thread newThread(final Runnable r) {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(this.threadName + (this.threadCounter == null ? "" : this.threadCounter.incrementAndGet()));
        thread.setUncaughtExceptionHandler((t, e) -> logger.log(Level.SEVERE, "Unexpected exception in profiler thread", e));

        return thread;
    }
}
