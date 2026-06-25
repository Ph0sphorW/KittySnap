package org.icarus.kittysnap.napcat.handler.handlers.image;

import io.wdsj.imagepreviewer.api.ImagePreviewerAPI;
import io.wdsj.imagepreviewer.image.ImageLoader;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

/**
 * ImagePreviewer 插件集成 — 通过反射调用其 API，无需编译期依赖。
 * <p>
 * 若服务器未安装 ImagePreviewer，所有方法安全地返回无操作。
 */
public class ImagePreviewerIntegration {

    @Getter
    private static boolean available = false;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private static JavaPlugin plugin;

    public static void init(JavaPlugin ourPlugin) {
        plugin = ourPlugin;
        available = Bukkit.getPluginManager().getPlugin("ImagePreviewer") != null;
    }

    /**
     * 异步加载图片 URL 并自动为所有指定玩家显示预览
     * 默认 128 * 128，可能压糊
     */
    public static void previewForPlayers(String url, Collection<? extends Player> players) {
        if (!available || url == null || url.isEmpty() || players.isEmpty()) return;
        ImageLoader.imageAsData(url)
                .thenAcceptOnMain(imageData -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (ImagePreviewerAPI.getApi().isPreviewRunning(player)) continue;
                        ImagePreviewerAPI.getApi().spawnPreview(player, imageData);
                    }
                });
    }
}
