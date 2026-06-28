package org.icarus.kittysnap.napcat.handler.handlers.image;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.icarus.kittysnap.config.ConfigurationManager;
import org.icarus.kittysnap.napcat.onebot.OB11MessageImage;
import org.icarus.kittysnap.utils.HandleResult;

/**
 * 图片处理
 * <p>
 * 当 ImagePreviewer 可用时，返回带 ClickEvent.callback 的 Component，
 * 玩家点击 {@code [图片]} 后只为自己加载预览。
 * 否则降级为可点击命令。
 */
public class ImageHandler {

    private static final int HOVER_MAX = 60;

    public static HandleResult handleImage(OB11MessageImage img, ConfigurationManager cfg) {
        var messages = cfg.getMessages();
        var snapConfig = cfg.getConfig();
        String url = img.getUrl();
        String summary = img.getSummary();
        boolean isGif = summary != null && !summary.isEmpty();

        if (url == null || url.isEmpty() || !snapConfig.getChatForward().parseImage) {
            String fallback = isGif ? messages.getSegment().getImageGifText() : messages.getSegment().getImageText();
            return new HandleResult(fallback);
        }

        // ImagePreviewer 可用 → 返回带 callback 的 Component
        if (ImagePreviewerIntegration.isAvailable()) {
            Component clickable = cfg.safeDeserialize("<gray>" + messages.getSegment().getImageText() + "</gray>")
                    .hoverEvent(HoverEvent.showText(cfg.safeDeserialize(messages.getSegment().getImagePreviewHover())))
                    .clickEvent(ClickEvent.callback(audience -> {
                        if (!(audience instanceof Player player)) return;
                        ImagePreviewerIntegration.previewForPlayer(url, player);
                    }));
            return new HandleResult("", java.util.List.of(clickable));
        }

        // 未安装 ImagePreviewer 则降级为可点击的命令
        String safeUrl = url.replace("\\", "\\\\").replace("'", "\\'");
        String hover = safeUrl.length() <= HOVER_MAX ? safeUrl : safeUrl.substring(0, HOVER_MAX) + "...";
        String mmText = "<click:run_command:'/imagepreviewer preview " + safeUrl + "'>"
                + "<hover:show_text:'" + messages.getSegment().getImagePreviewHover() + "\n"
                + "<dark_gray>" + hover + "</dark_gray>'>"
                + messages.getSegment().getImageText()
                + "</hover></click>";
        return new HandleResult(mmText);
    }
}