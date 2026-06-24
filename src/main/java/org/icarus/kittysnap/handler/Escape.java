package org.icarus.kittysnap.handler;

public class Escape {
    public static String esc(String s) {
        return s != null ? s.replace("<", "\\<") : "";
    }
}
