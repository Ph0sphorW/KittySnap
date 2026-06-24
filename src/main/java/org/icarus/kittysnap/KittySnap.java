package org.icarus.kittysnap;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.icarus.kittysnap.napcat.NapcatWebSocketClient;
import org.icarus.kittysnap.chat.ChatToGroupForwarder;
import org.icarus.kittysnap.chat.QQToGameBroadcaster;
import org.icarus.kittysnap.command.KittySnapCommand;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.database.DatabaseManager;

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
    private KittySnapCommand commandExecutor;

    @Getter
    private boolean debugMode = false;

    public void setDebugMode(boolean debug, CommandSender toggler) {
        this.debugMode = debug;
        if (debug) {
            var debugConsumer = new BiConsumer<String, Object[]>() {
                @Override
                public void accept(String key, Object[] args) {
                    getLogger().info("[DEBUG] " + configManager.raw(key, args));
                    Component component = configManager.component(key, args);
                    Bukkit.getScheduler().runTask(KittySnap.this, () ->
                        Bukkit.getOnlinePlayers().stream()
                                .filter(p -> p.hasPermission("kittysnap.admin"))
                                .forEach(p -> p.sendMessage(component))
                    );
                }
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

        var configGroups = configManager.getListenGroups();
        var runtimeGroups = napcatClient.getMonitoredGroups();
        var broadcaster = new QQToGameBroadcaster(configManager);

        for (long gid : configGroups) {
            if (!runtimeGroups.contains(gid)) {
                napcatClient.addGroup(gid, broadcaster);
            }
        }
        for (long gid : runtimeGroups) {
            if (!configGroups.contains(gid)) {
                napcatClient.removeGroup(gid);
            }
        }

        configManager.logInfo("config-reloaded");
    }

    @Override
    public void onEnable() {
        configManager = new ConfigurationManager(this);
        configManager.logInfo("plugin-enabled");

        napcatClient = new NapcatWebSocketClient(this, configManager);

        databaseManager = new DatabaseManager(configManager);
        databaseManager.init();
        napcatClient.setDatabaseManager(databaseManager);

        commandExecutor = new KittySnapCommand(this, configManager);
        var cmd = getCommand("kittysnap");
        if (cmd != null) {
            cmd.setExecutor(commandExecutor);
            cmd.setTabCompleter(commandExecutor);
        } else {
            configManager.logWarning("command-register-failed");
        }

        var listenGroups = configManager.getListenGroups();
        if (!listenGroups.isEmpty()) {
            napcatClient.addGroups(listenGroups, new QQToGameBroadcaster(configManager));
        } else {
            configManager.logWarning("no-listen-groups-configured");
        }

        napcatClient.connect();

        if (configManager.isChatForwardEnabled()) {
            chatForwarder = new ChatToGroupForwarder(napcatClient, configManager);
            getServer().getPluginManager().registerEvents(chatForwarder, this);
            configManager.logInfo("chat-forward-enabled");
        } else {
            configManager.logInfo("chat-forward-disabled");
        }
    }

    @Override
    public void onDisable() {
        chatForwarder = null;
        commandExecutor = null;
        if (napcatClient != null) {
            napcatClient.disconnect();
            napcatClient = null;
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
            databaseManager = null;
        }
        if (configManager != null) {
            configManager.logInfo("plugin-disabled");
        }
    }
}
