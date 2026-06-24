package org.icarus.kittysnap.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Getter
public final class KittySnapConfig {

    public KittySnapConfig() {}

    // -------------------- Napcat 连接 --------------------

    public Napcat napcat = new Napcat();

    @Configuration
    @Getter
    public static final class Napcat {
        @Comment({"Napcat 反向 WebSocket 地址", "格式: ws://IP:端口", "Napcat WebUI 中设置的监听 IP 和端口"})
        public String wsUrl = "ws://127.0.0.1:6700";

        @Comment({"连接认证 Token", "留空表示不需要认证"})
        public String token = "";

        @Comment("连接超时时间（秒）")
        public int connectTimeout = 10;

        @Comment("心跳超时时间（秒），超过此时间未收到心跳包则强制重连")
        public int heartbeatTimeout = 30;

        @Comment("自动重连延迟（秒），最小 1 秒")
        public int reconnectDelay = 5;

        @Comment("WebSocket 工作线程名称")
        public String threadName = "Napcat-WebSocket";

        @Comment("正常关闭时的 WebSocket 状态码")
        public int closeCode = 1000;

        @Comment("正常关闭时的提示原因")
        public String closeReason = "Plugin shutting down";

        @Comment("手动重连时的关闭原因")
        public String manualReconnectReason = "Manual reconnect";

        @Comment("调试消息截断长度（超过此长度加 ... 省略）")
        public int debugTruncateLength = 200;
    }

    // -------------------- 监听群聊 --------------------

    @Comment({"监听群号列表", "格式:", "  - 123456789", "  - 987654321"})
    public List<Long> groups = new ArrayList<>();

    // -------------------- 聊天转发 --------------------

    public ChatForward chatForward = new ChatForward();

    @Configuration
    @Getter
    public static final class ChatForward {
        @Comment("游戏内聊天 → QQ群 转发总开关")
        public boolean enabled = true;

        @Comment({"消息格式。占位符:", "  %s = 玩家显示名称", "  %s = 消息内容"})
        public String format = "[MC] <%s> %s";

        @Comment({"转发目标群列表", "留空则转发到上方 groups 中的所有群"})
        public List<Long> targetGroups = new ArrayList<>();
    }

    // -------------------- 数据库 --------------------

    public Database database = new Database();

    @Configuration
    @Getter
    public static final class Database {
        @Comment({"JDBC 连接 URL", "H2 示例：jdbc:h2:file:./plugins/KittySnap/data/messages;..."})
        public String jdbcUrl = "jdbc:h2:file:./plugins/KittySnap/data/messages;DB_CLOSE_DELAY=-1;MODE=MySQL";

        @Comment("JDBC 驱动类")
        public String driverClass = "org.h2.Driver";

        @Comment("数据库用户名")
        public String username = "sa";

        @Comment("数据库密码")
        public String password = "";

        @Comment("数据表名")
        public String tableName = "group_messages";

        public Pool pool = new Pool();

        @Configuration
        @Getter
        public static final class Pool {
            @Comment("连接池名称（日志标识）")
            public String poolName = "KittySnap-Hikari";

            @Comment("最大连接数")
            public int maxSize = 10;

            @Comment("最小空闲连接")
            public int minIdle = 2;

            @Comment("连接超时（毫秒）")
            public int connectionTimeout = 5000;

            @Comment("空闲超时（毫秒）")
            public int idleTimeout = 300000;

            @Comment("最大存活时间（毫秒）")
            public int maxLifetime = 600000;

            @Comment("是否启用预处理语句缓存")
            public boolean cachePrepStmts = true;

            @Comment("预处理语句缓存大小")
            public int prepStmtCacheSize = 250;

            @Comment("预处理语句 SQL 长度限制")
            public int prepStmtCacheSqlLimit = 2048;
        }
    }

    // -------------------- 杂项 --------------------

    public Misc misc = new Misc();

    @Configuration
    @Getter
    public static final class Misc {
        @Comment("数据库昵称截断长度")
        public int nicknameTruncateLength = 128;
    }
}
