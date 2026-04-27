package game.common.util;

import java.util.Map;
import java.util.concurrent.*;

public class DelayTaskUtil {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private static final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();


    /**
     * 提交延时任务
     * @param taskId 任务ID
     * @param task 任务
     * @param delay 延迟时间
     * @param unit 时间单位
     */
    public static void submit(String taskId, Runnable task, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                task.run();
            } finally {
                taskMap.remove(taskId);
            }
        }, delay, unit);

        taskMap.put(taskId, future);
    }

    /**
     * 取消任务
     * @param taskId 任务ID
     */
    public boolean cancel(String taskId) {
        ScheduledFuture<?> future = taskMap.get(taskId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            taskMap.remove(taskId);
            return cancelled;
        }
        return false;
    }

    /**
     * 检查任务是否存在
     */
    public boolean contains(String taskId) {
        return taskMap.containsKey(taskId);
    }

    /**
     * 获取任务数量
     */
    public int getTaskCount() {
        return taskMap.size();
    }

}
