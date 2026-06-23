package org.icarus.kittySnap.config;

import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurations;
import de.exlll.configlib.YamlConfigurationProperties;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * 配置管理器
 * <p>
 * 基于 ConfigLib 管理 {@code config.yml} 和 {@code messages.yml} 的加载、保存、重载。
 * 对外提供类型安全的配置访问和便利的消息格式化方法。
 */
public class ConfigurationManager {

    private static final String CONFIG_FILE = "config.yml";
    private static final String MESSAGES_FILE = "messages.yml";

    private final JavaPlugin plugin;
    private final Path configPath;
    private final Path messagesPath;
    private final YamlConfigurationProperties properties;
    private final MiniMessage miniMessage;
    private final PlainTextComponentSerializer plainText;

    @Getter
    private KittySnapConfig config;
    @Getter
    private MessagesConfig messages;

    private final Map<String, Method> messageGetters = new ConcurrentHashMap<>();

    public ConfigurationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configPath = plugin.getDataFolder().toPath().resolve(CONFIG_FILE);
        this.messagesPath = plugin.getDataFolder().toPath().resolve(MESSAGES_FILE);
        this.miniMessage = MiniMessage.miniMessage();
        this.plainText = PlainTextComponentSerializer.plainText();

        this.properties = YamlConfigurationProperties.newBuilder()
                .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
                .build();

        plugin.getDataFolder().mkdirs();
        reload();
    }

    public void reload() {
        config = YamlConfigurations.update(configPath, KittySnapConfig.class, properties);
        messages = YamlConfigurations.update(messagesPath, MessagesConfig.class, properties);
        messageGetters.clear();
    }

    public void saveConfig() {
        YamlConfigurations.save(configPath, KittySnapConfig.class, config, properties);
    }

    // ==================== 消息：Component 输出（玩家消息） ====================

    /**
     * 获取格式化为 Adventure Component 的消息（用于 {@code sender.sendMessage()}）
     */
    public Component component(String key, Object... args) {
        String template = resolveMessage(key);
        if (template == null) {
            String fallback = messages != null ? messages.getMissingKeyFallback() : null;
            return Component.text(fallback != null ? String.format(fallback, key) : "<missing: " + key + ">");
        }
        String formatted = args.length > 0 ? String.format(template, escapeArgs(args)) : template;
        return safeDeserialize(formatted);
    }

    /**
     * 获取带前缀的 Component
     */
    public Component prefixed(String key, Object... args) {
        String prefix = messages != null ? messages.getPrefix() : "[KittySnap]";
        return Component.text(prefix + " ").append(component(key, args));
    }

    // ==================== 消息：纯文本输出（控制台日志） ====================

    /**
     * 获取原始消息文本（已剥离 MiniMessage 标签），用于控制台日志
     */
    public String raw(String key, Object... args) {
        String template = resolveMessage(key);
        if (template == null) {
            String fallback = messages != null ? messages.getMissingKeyFallback() : null;
            return (fallback != null) ? String.format(fallback, key) : "<missing: " + key + ">";
        }
        String formatted = args.length > 0 ? String.format(template, escapeArgs(args)) : template;
        return stripTags(formatted);
    }

    /**
     * 获取带前缀的纯文本（控制台日志用）
     */
    public String prefixedRaw(String key, Object... args) {
        String prefix = messages != null ? messages.getPrefix() : "[KittySnap]";
        return prefix + " " + raw(key, args);
    }

    /** 安全地剥离 MiniMessage 标签，解析失败时返回原文 */
    private String stripTags(String text) {
        if (text == null) return "";
        Component component = safeDeserialize(text);
        return plainText.serialize(component);
    }

    /** 安全解析 MiniMessage，失败时返回纯文本 Component */
    public Component safeDeserialize(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        try {
            return miniMessage.deserialize(text);
        } catch (Exception e) {
            return Component.text(text);
        }
    }

    /**
     * 转义 args 中所有 String 的 MiniMessage 标签（< → \<），
     * 防止用户输入内容被 MiniMessage 误解析为颜色标签。
     */
    private Object[] escapeArgs(Object... args) {
        if (args == null || args.length == 0) return args;
        Object[] escaped = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String s) {
                escaped[i] = s.replace("<", "\\<");
            } else {
                escaped[i] = args[i];
            }
        }
        return escaped;
    }

    // ==================== 日志便捷方法 ====================

    public void logInfo(String key, Object... args) {
        plugin.getLogger().info(raw(key, args));
    }

    public void logWarning(String key, Object... args) {
        plugin.getLogger().warning(raw(key, args));
    }

    public void logSevere(String key, Object... args) {
        plugin.getLogger().severe(raw(key, args));
    }

    public void logFine(String key, Object... args) {
        plugin.getLogger().fine(raw(key, args));
    }

    // ==================== 内部 ====================

    /**
     * 通过反射从 MessagesConfig 中读取字段值。
     * 将 kebab-case 键转为 camelCase getter 名（如 "ws-connecting" → "getWsConnecting"）。
     */
    private String resolveMessage(String key) {
        Method getter = messageGetters.get(key);
        if (getter == null) {
            // kebab-case → camelCase getter: "ws-connecting" → "getWsConnecting"
            String getterName = toGetterName(key);
            try {
                getter = MessagesConfig.class.getMethod(getterName);
                getter.setAccessible(true);
                messageGetters.put(key, getter);
            } catch (NoSuchMethodException e) {
                plugin.getLogger().warning(() -> messages != null
                        ? String.format(messages.getMissingMessageKey(), key)
                        : "messages.yml 中缺少消息键: " + key);
                return null;
            }
        }
        try {
            Object value = getter.invoke(messages);
            return value instanceof String s ? s : null;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, messages != null
                    ? String.format(messages.getReadMessageFailed(), key)
                    : "读取消息 " + key + " 失败", e);
            return null;
        }
    }

    /**
     * "ws-connecting" → "getWsConnecting"
     */
    private static String toGetterName(String kebabKey) {
        StringBuilder sb = new StringBuilder("get");
        boolean nextUpper = true;
        for (char c : kebabKey.toCharArray()) {
            if (c == '-') {
                nextUpper = true;
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ==================== 代理方法：KittySnapConfig 常用配置 ====================

    public String getWsUrl() {
        return config.getNapcat().getWsUrl();
    }

    public int getReconnectDelay() {
        return Math.max(1, config.getNapcat().getReconnectDelay());
    }

    public List<Long> getListenGroups() {
        return config.getGroups();
    }

    public boolean hasListenGroups() {
        return !config.getGroups().isEmpty();
    }

    public boolean addListenGroup(long groupId) {
        if (config.getGroups().contains(groupId)) return false;
        config.getGroups().add(groupId);
        saveConfig();
        return true;
    }

    public boolean removeListenGroup(long groupId) {
        boolean removed = config.getGroups().remove(groupId);
        if (removed) saveConfig();
        return removed;
    }

    public boolean isChatForwardEnabled() {
        return config.getChatForward().isEnabled();
    }

    public String getChatForwardFormat() {
        return config.getChatForward().getFormat();
    }

    public List<Long> getChatForwardTargetGroups() {
        return config.getChatForward().getTargetGroups();
    }

    public String getDbJdbcUrl() {
        return config.getDatabase().getJdbcUrl();
    }

    public String getDbDriverClass() {
        return config.getDatabase().getDriverClass();
    }

    public String getDbUsername() {
        return config.getDatabase().getUsername();
    }

    public String getDbPassword() {
        return config.getDatabase().getPassword();
    }

    public String getDbTableName() {
        return config.getDatabase().getTableName();
    }

    public String getDbPoolName() {
        return config.getDatabase().getPool().getPoolName();
    }

    public int getDbPoolMaxSize() {
        return Math.max(1, config.getDatabase().getPool().getMaxSize());
    }

    public int getDbPoolMinIdle() {
        return Math.max(0, config.getDatabase().getPool().getMinIdle());
    }

    public int getDbPoolConnectionTimeout() {
        return Math.max(1000, config.getDatabase().getPool().getConnectionTimeout());
    }

    public int getDbPoolIdleTimeout() {
        return Math.max(60000, config.getDatabase().getPool().getIdleTimeout());
    }

    public int getDbPoolMaxLifetime() {
        return Math.max(180000, config.getDatabase().getPool().getMaxLifetime());
    }

    public boolean isDbCachePrepStmts() {
        return config.getDatabase().getPool().isCachePrepStmts();
    }

    public int getDbPrepStmtCacheSize() {
        return Math.max(25, config.getDatabase().getPool().getPrepStmtCacheSize());
    }

    public int getDbPrepStmtCacheSqlLimit() {
        return Math.max(512, config.getDatabase().getPool().getPrepStmtCacheSqlLimit());
    }

    // ==================== Napcat 新配置 ====================

    public String getNapcatThreadName() {
        return config.getNapcat().getThreadName();
    }

    public int getNapcatCloseCode() {
        return Math.max(1000, config.getNapcat().getCloseCode());
    }

    public String getNapcatCloseReason() {
        return config.getNapcat().getCloseReason();
    }

    public String getNapcatManualReconnectReason() {
        return config.getNapcat().getManualReconnectReason();
    }

    public int getNapcatDebugTruncateLength() {
        return Math.max(50, config.getNapcat().getDebugTruncateLength());
    }

    /** Napcat 连接 Token（可能为空） */
    public String getNapcatToken() {
        return config.getNapcat().getToken();
    }

    public long getNapcatConnectTimeout() {
        return Math.max(5, config.getNapcat().getConnectTimeout());
    }

    public long getNapcatHeartbeatTimeout() {
        return Math.max(10, config.getNapcat().getHeartbeatTimeout());
    }

    // ==================== Misc ====================

    public int getNicknameTruncateLength() {
        return Math.max(32, config.getMisc().getNicknameTruncateLength());
    }

    // ==================== 日志格式快捷方法 ====================

    /**
     * 获取日志消息格式的文本，使用 MessagesConfig 的 logGroupMsgFormat 键。
     */
    public String logGroupMsgFormat(long groupId, long userId, String content) {
        return String.format(getMessages().getLogGroupMsgFormat(), groupId, userId, content);
    }
}
