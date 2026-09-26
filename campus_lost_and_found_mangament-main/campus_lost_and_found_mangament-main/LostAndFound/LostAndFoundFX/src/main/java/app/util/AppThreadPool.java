package app.util;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages an application-wide thread pool for asynchronous background processing
 * (Multi-threading, Thread Pools, and JavaFX Tasks).
 * Fulfills the Concurrency requirement.
 */
public class AppThreadPool {

    private static final int THREAD_POOL_SIZE = 4;
    private static final AtomicInteger threadCounter = new AtomicInteger(1);

    private static final ExecutorService executor = Executors.newFixedThreadPool(
            THREAD_POOL_SIZE,
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "LostFound-Worker-" + threadCounter.getAndIncrement());
                    thread.setDaemon(true); // Daemon threads ensure JVM exits smoothly
                    return thread;
                }
            }
    );

    public static ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Executes a runnable task in the background worker thread pool.
     */
    public static void execute(Runnable task) {
        executor.execute(task);
    }

    /**
     * Submits a callable task returning a Future.
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * Runs a JavaFX Task on the background thread pool.
     */
    public static <T> void runTask(Task<T> task) {
        executor.execute(task);
    }

    /**
     * Shuts down the thread pool gracefully.
     */
    public static void shutdown() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
