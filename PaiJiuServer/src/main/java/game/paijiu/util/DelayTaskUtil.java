package game.paijiu.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DelayTaskUtil {
    private static final DelayTaskUtil INSTANCE = new DelayTaskUtil();

    public static DelayTaskUtil getInstance() {
        return INSTANCE;
    }

    private final ScheduledThreadPoolExecutor executor;

    private DelayTaskUtil() {
        AtomicInteger index = new AtomicInteger(1);

        this.executor = new ScheduledThreadPoolExecutor(
                4,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("game-delay-task-" + index.getAndIncrement());
                    thread.setDaemon(false);
                    return thread;
                }
        );

        // 取消任务后，立即从队列移除，避免内存堆积
        this.executor.setRemoveOnCancelPolicy(true);
    }

    /**
     * 延迟执行任务
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return executor.schedule(wrap(task), delay, unit);
    }

    /**
     * 延迟多少毫秒执行
     */
    public ScheduledFuture<?> scheduleMillis(Runnable task, long delayMillis) {
        return schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 延迟多少秒执行
     */
    public ScheduledFuture<?> scheduleSeconds(Runnable task, long delaySeconds) {
        return schedule(task, delaySeconds, TimeUnit.SECONDS);
    }

    /**
     * 取消任务
     */
    public void cancel(ScheduledFuture<?> future) {
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }

    /**
     * 包一层 try-catch，防止异常导致线程问题
     */
    private Runnable wrap(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("延时任务执行异常", e);
            }
        };
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        executor.shutdown();
    }
}
