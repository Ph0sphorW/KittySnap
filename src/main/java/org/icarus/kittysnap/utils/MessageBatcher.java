package org.icarus.kittysnap.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 消息批处理器
 * <p>
 * 行为：
 * <ul>
 *   <li>首条消息入队时启动计时器</li>
 *   <li>队列达到 {@code maxBatchSize} 条时立即刷出</li>
 *   <li>计时器到期时自动刷出</li>
 *   <li>刷出后重置计时器</li>
 * </ul>
 */
public class MessageBatcher implements AutoCloseable {

    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private final int maxBatchSize;
    private final long maxIntervalTicks;
    private final JavaPlugin plugin;
    private final Consumer<List<String>> flushHandler;
    private final ReentrantLock lock = new ReentrantLock();
    private int taskId = -1;

    /**
     * @param plugin          用于调度
     * @param maxBatchSize    批次最大条数
     * @param maxIntervalSec  批次最大等待时间
     * @param flushHandler    刷出回调，接收一批消息
     */
    public MessageBatcher(JavaPlugin plugin, int maxBatchSize, int maxIntervalSec,
                          Consumer<List<String>> flushHandler) {
        this.plugin = plugin;
        this.maxBatchSize = Math.max(1, maxBatchSize);
        this.maxIntervalTicks = Math.max(1, maxIntervalSec) * 20L;
        this.flushHandler = flushHandler;
    }

    /**
     * 添加一条消息到队列
     * 若队列为空则启动计时器
     * 若队列已满则立即刷出
     */
    public void add(String message) {
        queue.add(message);

        boolean isFirst;
        lock.lock();
        try {
            isFirst = (taskId == -1);
            if (isFirst) {
                taskId = scheduleTimer();
            }
        } finally {
            lock.unlock();
        }

        if (queue.size() >= maxBatchSize) {
            flush();
        }
    }

    /**
     * 刷出所有积压消息并取消计时器
     */
    public void flush() {
        List<String> batch = new ArrayList<>();
        lock.lock();
        try {
            if (taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
                taskId = -1;
            }
            String msg;
            while ((msg = queue.poll()) != null) {
                batch.add(msg);
            }
        } finally {
            lock.unlock();
        }

        if (!batch.isEmpty()) {
            flushHandler.accept(batch);
        }
    }

    /**
     * 是否正在积压（队列非空或有计时器在跑）
     */
    public boolean isPending() {
        return !queue.isEmpty() || taskId != -1;
    }

    /**
     * 清空队列并取消计时器
     */
    public void clear() {
        lock.lock();
        try {
            if (taskId != -1) {
                Bukkit.getScheduler().cancelTask(taskId);
                taskId = -1;
            }
            queue.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        flush();
    }

    private int scheduleTimer() {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this::onTimer, maxIntervalTicks).getTaskId();
    }

    private void onTimer() {
        lock.lock();
        try {
            taskId = -1;
        } finally {
            lock.unlock();
        }
        flush();
    }
}
