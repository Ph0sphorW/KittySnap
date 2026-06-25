package org.icarus.kittysnap.napcat.handler;

public class Escaper {
    public static String escape(String s) {
        return s != null ? s.replace("<", "\\<") : "";
    }

    /** 转义字符串使其可安全嵌入 MiniMessage hover:show_text:'...' 而不破坏标签 */
    public static String hoverSafe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("<", "\\<")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
