package game.paijiu.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class GameThreadPoolUtil {
    private GameThreadPoolUtil() {
    }
    private static final int BUSINESS_CORE_SIZE = 8;
    private static final int BUSINESS_MAX_SIZE = 32;
    private static final int BUSINESS_QUEUE_SIZE = 10000;
    private static final int ROOM_WORKER_SIZE = 16;

    /**
     * 普通业务线程池
     */
    private static final ThreadPoolExecutor BUSINESS_EXECUTOR =
            new ThreadPoolExecutor(
                    BUSINESS_CORE_SIZE,
                    BUSINESS_MAX_SIZE,
                    60L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(BUSINESS_QUEUE_SIZE),
                    new NamedThreadFactory("game-business"),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    /**
     * 房间线程池
     * 同一个 roomId 固定进入同一个单线程，保证房间内逻辑串行
     */
    private static final ExecutorService[] ROOM_EXECUTORS = new ExecutorService[ROOM_WORKER_SIZE];

    static {
        for (int i = 0; i < ROOM_WORKER_SIZE; i++) {
            ROOM_EXECUTORS[i] = Executors.newSingleThreadExecutor(new NamedThreadFactory("room-worker-" + i));
        }
    }

    /**
     * 执行普通业务任务
     */
    public static void execute(Runnable task) {
        BUSINESS_EXECUTOR.execute(task);
    }

    /**
     * 执行房间任务
     */
    public static void executeRoom(String roomId, Runnable task) {
        if (roomId == null || roomId.isBlank()) {
            execute(task);
            return;
        }

        int index = selectRoomWorker(roomId);
        ROOM_EXECUTORS[index].execute(task);
    }

    private static int selectRoomWorker(String roomId) {
        return Math.floorMod(roomId.hashCode(), ROOM_WORKER_SIZE);
    }

    /**
     * 优雅关闭
     */
    public static void shutdown() {
        log.info("GameThreadPool shutdown start");
        BUSINESS_EXECUTOR.shutdown();
        for (ExecutorService executor : ROOM_EXECUTORS) {
            executor.shutdown();
        }
        log.info("GameThreadPool shutdown finish");
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private final String prefix;
        private final AtomicInteger index = new AtomicInteger(1);

        private NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(prefix + "-" + index.getAndIncrement());
            thread.setUncaughtExceptionHandler((t, e) ->
                    log.error("Uncaught exception in thread: {}", t.getName(), e)
            );
            return thread;
        }
    }
}
