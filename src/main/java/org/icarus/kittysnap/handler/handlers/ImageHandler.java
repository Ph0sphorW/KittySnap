package org.icarus.kittysnap.handler.handlers;

import org.bukkit.Bukkit;
import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.onebotapi.OB11MessageImage;

/**
 * 图片处理
 * <p>
 * 会尝试调用多多的 ImagePreviewer
 * 动图暂时不触发预览，仅显示 {@code [动画表情]}
 */
public class ImageHandler {

    private static final int HOVER_MAX = 60;

    public static String handleImage(OB11MessageImage img, MessagesConfig m) {
        String url = img.getUrl();
        String summary = img.getSummary();
        boolean isGif = summary != null && !summary.isEmpty();

        // 动图不生成预览
        if (isGif) return m.getSegmentImageGifText();
        if (url == null || url.isEmpty()) return m.getSegmentImageText();

        // ImagePreviewer 可用自动异步加载
        if (ImagePreviewerIntegration.isAvailable()) {
            ImagePreviewerIntegration.previewForPlayers(url, Bukkit.getOnlinePlayers());
            return m.getSegmentImageText();
        }

        // 未加载则降级为可点击的命令
        String safeUrl = url.replace("\\", "\\\\").replace("'", "\\'");
        String hover = safeUrl.length() <= HOVER_MAX ? safeUrl : safeUrl.substring(0, HOVER_MAX) + "...";
        return "<click:run_command:'/imagepreviewer preview " + safeUrl + "'>"
                + "<hover:show_text:'" + m.getSegmentImagePreviewHover() + "\n<dark_gray>" + hover + "</dark_gray>'>"
                + m.getSegmentImageText()
                + "</hover></click>";
    }
}
