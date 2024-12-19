package ru.joke.profiler.core.output.handlers.util;

import ru.joke.profiler.core.output.handlers.OutputDataSink;
import ru.joke.profiler.core.output.handlers.ProfilerOutputSinkException;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RecoveryProcessor {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    private final Runnable cleaningCallback;
    private final Runnable recoveryCallback;
    private final long maxRecoveryAttemptTimeMillis;
    private final long maxRecoveryTimeoutMillis;

    public RecoveryProcessor(
            final Runnable cleaningCallback,
            final Runnable recoveryCallback,
            final long maxRecoveryAttemptTimeMillis,
            final long maxRecoveryTimeoutMillis) {
        this.recoveryCallback = Objects.requireNonNull(recoveryCallback, "recoveryCallback");
        this.cleaningCallback = Objects.requireNonNull(cleaningCallback, "cleaningCallback");
        this.maxRecoveryAttemptTimeMillis = maxRecoveryAttemptTimeMillis;
        this.maxRecoveryTimeoutMillis = maxRecoveryTimeoutMillis;
    }

    public void recover(final Exception exception) {

        logger.log(Level.FINE, "Trying to recover connection, exception detected", exception);

        this.clean();

        if (!this.recover()) {
            logger.severe(String.format("Fatal disconnect, cannot recover connection in %d ms", this.maxRecoveryAttemptTimeMillis));
            throw new ProfilerOutputSinkException(exception);
        }
    }

    private void clean() {

        try {
            this.cleaningCallback.run();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception while execution of cleaning callback", e);
        }
    }

    private boolean recover() {

        final Random rand = new SecureRandom();

        long nextAttemptTimeout = 10;
        boolean isRecovered;
        final long tryToRecoverBefore = System.currentTimeMillis() + this.maxRecoveryTimeoutMillis;

        do {
            isRecovered = this.recoverTry();
            nextAttemptTimeout = Math.min(nextAttemptTimeout * 3, this.maxRecoveryAttemptTimeMillis);

        } while (!isRecovered
                && this.waitNextAttempt(nextAttemptTimeout + rand.nextLong(nextAttemptTimeout / 5))
                && System.currentTimeMillis() < tryToRecoverBefore);

        return isRecovered;
    }

    private boolean waitNextAttempt(final long waitTimeInMillis) {

        try {
            Thread.sleep(waitTimeInMillis);
        } catch (InterruptedException ex) {
            logger.info("Waiting of next attempt is interrupted");
            Thread.currentThread().interrupt();
        }

        return !Thread.currentThread().isInterrupted();
    }

    private boolean recoverTry() {

        try {
            this.recoveryCallback.run();

            logger.fine("Successful connection recovery");
            return true;
        } catch (Exception ex) {
            logger.log(Level.FINE, "Recovery attempt unsuccessful", ex);
        }

        return false;
    }
}