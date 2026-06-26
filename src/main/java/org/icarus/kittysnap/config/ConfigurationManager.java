package org.icarus.kittysnap.config;

import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurations;
import de.exlll.configlib.YamlConfigurationProperties;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

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

    /**
     * 扁平化消息映射: "section.field" → 模板字符串
     */
    private final Map<String, String> messageMap = new HashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
        rebuildMessageMap();
    }

    /**
     * 递归扫描 MessagesConfig 嵌套结构，构建扁平键→值映射
     */
    private void rebuildMessageMap() {
        messageMap.clear();
        scanFields("", messages, messages.getClass());
    }

    private void scanFields(String prefix, Object instance, Class<?> clazz) {
        for (Field f : clazz.getFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
            String name = toKebab(f.getName());
            String fullKey = prefix.isEmpty() ? name : prefix + "." + name;
            try {
                Object val = f.get(instance);
                if (val instanceof String s) {
                    messageMap.put(fullKey, s);
                } else if (val != null && f.getType().isAnnotationPresent(de.exlll.configlib.Configuration.class)) {
                    scanFields(fullKey, val, f.getType());
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * camelCase → kebab-case
     */
    private static String toKebab(String camel) {
        StringBuilder sb = new StringBuilder();
        for (char c : camel.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!sb.isEmpty()) sb.append('-');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public void saveConfig() {
        YamlConfigurations.save(configPath, KittySnapConfig.class, config, properties);
    }

    // -------------------- 玩家消息 --------------------

    /**
     * 获取格式化为 Component 的消息
     */
    public Component component(String key, Object... args) {
        String template = resolveMessage(key);
        if (template == null) {
            String fallback = messages != null ? messages.getInternal().getMissingKeyFallback() : null;
            return Component.text(fallback != null ? String.format(fallback, key) : "<missing: " + key + ">");
        }
        String formatted = args.length > 0 ? String.format(template, escapeArgs(args)) : template;
        return safeDeserialize(formatted);
    }

    /**
     * 获取带前缀的 Component
     */
    public Component prefixed(String key, Object... args) {
        String rawPrefix = messages != null ? messages.getPrefix() : "[KittySnap]";
        Component prefixComp = safeDeserialize(rawPrefix);
        return prefixComp.append(Component.text(" ")).append(component(key, args));
    }

    // -------------------- 控制台日志 --------------------

    /**
     * 获取原始消息文本
     */
    public String raw(String key, Object... args) {
        String template = resolveMessage(key);
        if (template == null) {
            String fallback = messages != null ? messages.getInternal().getMissingKeyFallback() : null;
            return (fallback != null) ? String.format(fallback, key) : "<missing: " + key + ">";
        }
        String formatted = args.length > 0 ? String.format(template, escapeArgs(args)) : template;
        return stripTags(formatted);
    }

    /**
     * 安全地剥离 MiniMessage 标签
     */
    private String stripTags(String text) {
        if (text == null) return "";
        Component component = safeDeserialize(text);
        return plainText.serialize(component);
    }

    /**
     * 安全解析 MiniMessage
     */
    public Component safeDeserialize(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        try {
            return miniMessage.deserialize(text);
        } catch (Exception e) {
            return Component.text(text);
        }
    }

    /**
     * 转义 args 中所有 String 的 MiniMessage 标签，防止用户输入内容被 MiniMessage 误解析为颜色标签。 <p>
     * 例如，< 将被转义为 \<
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

    // -------------------- 日志方法 --------------------

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

    public void logWarning(String key, Throwable t, Object... args) {
        plugin.getLogger().log(Level.WARNING, raw(key, args), t);
    }

    public void logSevere(String key, Throwable t, Object... args) {
        plugin.getLogger().log(Level.SEVERE, raw(key, args), t);
    }

    // -------------------- 内部 --------------------

    /**
     * 从扁平消息映射中查找键
     */
    private String resolveMessage(String key) {
        String template = messageMap.get(key);
        if (template == null) {
            plugin.getLogger().warning(() -> {
                String fallback = messages != null ? messages.getInternal().getMissingMessageKey() : null;
                return fallback != null ? String.format(fallback, key) : "Missing message key: " + key;
            });
        }
        return template;
    }

    // -------------------- 代理方法：KittySnapConfig 常用配置 --------------------

    public String getWsUrl() {
        return config.getNapcat().getWsUrl();
    }

    public int getReconnectDelay() {
        return Math.max(1, config.getNapcat().getReconnectDelay());
    }

    public List<Long> getListenGroups() {
        return config.getGroups();
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

    // -------------------- Napcat 新配置 --------------------

    public int getNapcatDebugTruncateLength() {
        return Math.max(50, config.getNapcat().getDebugTruncateLength());
    }

    /**
     * Napcat 连接 Token（可能为空）
     */
    public String getNapcatToken() {
        return config.getNapcat().getToken();
    }

    public long getNapcatConnectTimeout() {
        return Math.max(5, config.getNapcat().getConnectTimeout());
    }

    // -------------------- Misc --------------------

    public int getNicknameTruncateLength() {
        return Math.max(32, config.getMisc().getNicknameTruncateLength());
    }

    // -------------------- 日志格式快捷方法 --------------------

    /**
     * 获取日志消息格式的文本，使用 MessagesConfig 的 logGroupMsgFormat 键。
     */
    public String logGroupMsgFormat(long groupId, long userId, String content) {
        return String.format(getMessages().getInternal().getLogGroupMsgFormat(), groupId, userId, content);
    }
}
