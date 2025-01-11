package ru.joke.profiler.output.sinks.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static ru.joke.profiler.util.ArgUtil.checkPositive;

public final class ConcurrentLinkedBlockingQueue<E> {

    private final Semaphore emptySemaphore;
    private final Semaphore fullSemaphore;
    private final Queue<E> sourceQueue;

    public ConcurrentLinkedBlockingQueue(final int capacity) {
        checkPositive(capacity, "capacity");
        this.sourceQueue = new ConcurrentLinkedQueue<>();
        this.emptySemaphore = new Semaphore(1);
        this.fullSemaphore = new Semaphore(capacity);
    }

    public E poll() {
        E result;
        while ((result = this.sourceQueue.poll()) == null
                && this.emptySemaphore.tryAcquire());

        if (result != null) {
            this.fullSemaphore.release();
        }

        return result;
    }

    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        E result;
        while ((result = this.sourceQueue.poll()) == null
                && this.emptySemaphore.tryAcquire(timeout, unit));

        if (result != null) {
            this.fullSemaphore.release();
        }

        return result;
    }

    public boolean offer(final E elem) {

        if (!this.fullSemaphore.tryAcquire()) {
            return false;
        }

        this.sourceQueue.offer(elem);
        this.emptySemaphore.release();

        return true;
    }

    public void forEach(final Consumer<? super E> action) {
        this.sourceQueue.forEach(action);
    }
}
