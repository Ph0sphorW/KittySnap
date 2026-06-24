package org.icarus.kittysnap.handler.handlers;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * ImagePreviewer 插件集成 — 通过反射调用其 API，无需编译期依赖。
 * <p>
 * 若服务器未安装 ImagePreviewer，所有方法安全地返回无操作。
 */
public class ImagePreviewerIntegration {

    @Getter
    private static boolean available = false;
    private static Method spawnPreviewMethod;
    private static Method spawnPreviewWithLifecycle;
    private static Method imageAsDataMethod;
    private static Object apiInstance;
    private static JavaPlugin plugin;

    public static void init(JavaPlugin ourPlugin) {
        plugin = ourPlugin;
        if (Bukkit.getPluginManager().getPlugin("ImagePreviewer") == null) {
            plugin.getLogger().info("[ImagePreviewer] 未检测到 ImagePreviewer 插件，图片预览降级为点击命令模式");
            return;
        }
        try {
            Class<?> apiClass = Class.forName("io.wdsj.imagepreviewer.api.ImagePreviewerAPI");
            Method getApi = apiClass.getMethod("getApi");
            apiInstance = getApi.invoke(null);
            spawnPreviewMethod = apiClass.getMethod("spawnPreview", Player.class,
                    Class.forName("io.wdsj.imagepreviewer.image.ImageData"));
            spawnPreviewWithLifecycle = apiClass.getMethod("spawnPreview", Player.class,
                    Class.forName("io.wdsj.imagepreviewer.image.ImageData"), long.class);

            Class<?> loaderClass = Class.forName("io.wdsj.imagepreviewer.image.ImageLoader");
            imageAsDataMethod = loaderClass.getMethod("imageAsData", String.class);

            available = true;
            plugin.getLogger().info("[ImagePreviewer] 已连接 ImagePreviewer API，图片将自动预览");
        } catch (Exception e) {
            available = false;
            plugin.getLogger().warning("[ImagePreviewer] 反射加载 ImagePreviewer API 失败: " + e.getMessage());
        }
    }

    /**
     * 异步加载图片 URL 并自动为所有指定玩家显示预览
     */
    @SuppressWarnings({"rawtypes"})
    public static void previewForPlayers(String url, Collection<? extends Player> players) {
        if (!available || url == null || url.isEmpty() || players.isEmpty()) return;
        try {
            Object fetchTask = imageAsDataMethod.invoke(null, url);
            // ImageFetchTask 是 record，字段 future 是 CompletableFuture<ImageData>
            Class<?> taskClass = fetchTask.getClass();
            java.lang.reflect.Field futureField = taskClass.getDeclaredField("future");
            futureField.setAccessible(true);
            CompletableFuture<?> future = (CompletableFuture<?>) futureField.get(fetchTask);

            // 取第一个返回值中的帧长度作为 lifecycleTicks（每帧显示 3 秒）
            future.whenComplete((data, err) -> {
                if (err != null || data == null) {
                    plugin.getLogger().warning("[ImagePreviewer] 加载图片失败: " +
                            (err != null ? err.getMessage() : "未知错误"));
                    return;
                }
                // 在主线程中为每个玩家渲染预览
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        // 获取帧数以决定 lifecycle
                        java.lang.reflect.Method frameMethod = data.getClass().getMethod("frameData");
                        java.util.List frameList = (java.util.List) frameMethod.invoke(data);
                        long ticks = frameList.size() * 60L; // 每帧 3 秒

                        for (Player player : players) {
                            if (!player.isOnline()) continue;
                            if (frameList.size() > 1) {
                                // 动图用 lifecycle 控制
                                spawnPreviewWithLifecycle.invoke(apiInstance, player, data, ticks);
                            } else {
                                spawnPreviewMethod.invoke(apiInstance, player, data);
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("[ImagePreviewer] 渲染预览失败: " + e.getMessage());
                    }
                });
            });
        } catch (Exception e) {
            plugin.getLogger().warning("[ImagePreviewer] 发起图片加载失败: " + e.getMessage());
        }
    }
}
