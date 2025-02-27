package ru.joke.profiler.output.sinks.util.recovery;

import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.joke.profiler.util.ArgUtil.checkNonNegative;
import static ru.joke.profiler.util.ArgUtil.checkNotNull;

public final class RecoveryProcessor {

    private static final Logger logger = Logger.getLogger(RecoveryProcessor.class.getCanonicalName());

    private final Runnable cleaningCallback;
    private final Runnable recoveryCallback;
    private final long maxRecoveryAttemptTimeMillis;
    private final long maxRecoveryTimeoutMillis;

    public RecoveryProcessor(
            final Runnable cleaningCallback,
            final Runnable recoveryCallback,
            final long maxRecoveryAttemptTimeMillis,
            final long maxRecoveryTimeoutMillis
    ) {
        this.recoveryCallback = checkNotNull(recoveryCallback, "recoveryCallback");
        this.cleaningCallback = checkNotNull(cleaningCallback, "cleaningCallback");
        this.maxRecoveryAttemptTimeMillis = checkNonNegative(maxRecoveryAttemptTimeMillis, "maxRecoveryAttemptTimeMillis");
        this.maxRecoveryTimeoutMillis = checkNonNegative(maxRecoveryTimeoutMillis, "maxRecoveryTimeoutMillis");
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

        final ThreadLocalRandom rand = ThreadLocalRandom.current();

        long nextAttemptTimeout = 10;
        boolean isRecovered;
        final long tryToRecoverBefore = Math.max(this.maxRecoveryTimeoutMillis, System.currentTimeMillis() + this.maxRecoveryTimeoutMillis);

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