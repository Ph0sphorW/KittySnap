package org.icarus.kittysnap;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.icarus.kittysnap.napcat.NapcatWebSocketClient;
import org.icarus.kittysnap.chat.ChatToGroupForwarder;
import org.icarus.kittysnap.chat.QQToGameBroadcaster;
import org.icarus.kittysnap.command.KittySnapCommand;
import org.icarus.kittysnap.utils.listeners.LoggingGroupListener;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.database.DatabaseManager;
import org.icarus.kittysnap.napcat.handler.SegmentHandler;
import org.icarus.kittysnap.napcat.handler.handlers.image.ImagePreviewerIntegration;
import org.slf4j.Logger;

import java.util.function.BiConsumer;

public final class KittySnap extends JavaPlugin {

    @Getter
    private ConfigurationManager configManager;
    @Getter
    private NapcatWebSocketClient napcatClient;
    @Getter
    private ChatToGroupForwarder chatForwarder;
    @Getter
    private DatabaseManager databaseManager;
    @SuppressWarnings({"FieldCanBeLocal"})
    private KittySnapCommand commandExecutor;

    public static Logger LOGGER;

    @Override
    public void onLoad() {
        LOGGER = getSLF4JLogger();
    }

    @Getter
    private boolean debugMode = false;

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
        if (debug) {
            BiConsumer<String, Object[]> debugConsumer = (key, args) -> {
                getLogger().info("[DEBUG] " + configManager.raw(key, args));
                Component component = configManager.component(key, args);
                Bukkit.getScheduler().runTask(KittySnap.this, () ->
                    Bukkit.getOnlinePlayers().stream()
                            .filter(p -> p.hasPermission("kittysnap.admin"))
                            .forEach(p -> p.sendMessage(component))
                );
            };
            napcatClient.setDebugConsumer(debugConsumer);
            if (databaseManager != null) {
                databaseManager.setDebugConsumer(debugConsumer);
            }
        } else {
            napcatClient.setDebugConsumer(null);
            if (databaseManager != null) {
                databaseManager.setDebugConsumer(null);
            }
        }
    }

    public void reload() {
        configManager.reload();

        // 清理已存在，防止反复注册
        napcatClient.removeListenersByType(QQToGameBroadcaster.class);
        napcatClient.removeListenersByType(LoggingGroupListener.class);

        // 用新配置重新注册
        var configGroups = configManager.getConfig().getGroups();
        if (!configGroups.isEmpty()) {
            napcatClient.addGroups(configGroups, new QQToGameBroadcaster(configManager));
        }

        configManager.logInfo("plugin.config-reloaded");
    }

    @Override
    public void onEnable() {
        configManager = new ConfigurationManager(this);
        configManager.logInfo("plugin.enabled");

        napcatClient = new NapcatWebSocketClient(this, configManager);

        databaseManager = new DatabaseManager(configManager);
        databaseManager.init();
        napcatClient.setDatabaseManager(databaseManager);

        // 初始化消息片段处理器
        SegmentHandler segmentHandler = new SegmentHandler(napcatClient, configManager);
        napcatClient.setSegmentHandler(segmentHandler);

        // 初始化 ImagePreviewer 集成，reflect 调用，感谢多多
        ImagePreviewerIntegration.init(this);

        commandExecutor = new KittySnapCommand(this, configManager);
        var cmd = getCommand("kittysnap");
        if (cmd != null) {
            cmd.setExecutor(commandExecutor);
            cmd.setTabCompleter(commandExecutor);
        } else {
            configManager.logWarning("internal.command-register-failed");
        }

        var listenGroups = configManager.getConfig().getGroups();
        if (!listenGroups.isEmpty()) {
            napcatClient.addGroups(listenGroups, new QQToGameBroadcaster(configManager));
        } else {
            configManager.logWarning("internal.no-listen-groups-configured");
        }

        napcatClient.connect();

        if (configManager.getConfig().getChatForward().isEnabled()) {
            chatForwarder = new ChatToGroupForwarder(napcatClient, configManager, this);
            getServer().getPluginManager().registerEvents(chatForwarder, this);
            configManager.logInfo("chat-forward.enabled");
        } else {
            configManager.logInfo("chat-forward.disabled");
        }
    }

    @Override
    public void onDisable() {
        if (chatForwarder != null) {
            chatForwarder.shutdown();
        }
        if (napcatClient != null) {
            napcatClient.disconnect();
            napcatClient = null;
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
            databaseManager = null;
        }
        if (configManager != null) {
            configManager.logInfo("plugin.disabled");
        }
    }
}
