package org.icarus.kittysnap.handler.handlers;

import org.icarus.kittysnap.onebotapi.OB11MessageImage;

public class ImageHandler {
    public static String handleImage(OB11MessageImage img) {
        String s = img.getSummary();
        return (s != null && !s.isEmpty()) ? "[动画表情]" : "[图片]";
    }
}
