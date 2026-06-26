package org.icarus.kittysnap.napcat.handler.handlers.image;

import io.wdsj.imagepreviewer.image.ImageLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.icarus.kittysnap.config.KittySnapConfig;
import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.napcat.onebot.OB11MessageImage;

/**
 * 图片处理
 * <p>
 * 尝试调用 ImagePreviewer
 * 动图暂时不触发预览，仅显示 {@code [动画表情]}
 */
public class ImageHandler {

    private static final int HOVER_MAX = 60;

    public static String handleImage(OB11MessageImage img, MessagesConfig config, KittySnapConfig snapConfig) {
        String url = img.getUrl();
        String summary = img.getSummary();
        boolean isGif = summary != null && !summary.isEmpty();


        var callback = ClickEvent.callback(audience -> { //死局了 想想怎么重构
            // check perm
            // 您的权限节点我知らない，亲请check一下
            //
            var task = ImageLoader.imageAsData(url);
        });

        // 这不酷吗孩子们
        Component test = Component.text("What the fuck").clickEvent(ClickEvent.callback(audience -> {
            if (!(audience instanceof Player player)) {
                System.out.println("我操");
                return;
            }

            player.kick(Component.text("谁他妈让你点了？"));
        }));

        if (url == null || url.isEmpty() && !snapConfig.getChatForward().parseImage) {
            if (isGif) {
                return config.getSegment().getImageGifText();
            }
            return config.getSegment().getImageText();
        }

        // ImagePreviewer 可用自动异步加载
        if (ImagePreviewerIntegration.isAvailable()) {
            ImagePreviewerIntegration.previewForPlayers(url, Bukkit.getOnlinePlayers());
            return config.getSegment().getImageText();
        }

        // 未加载则降级为可点击的命令
        String safeUrl = url.replace("\\", "\\\\").replace("'", "\\'");
        String hover = safeUrl.length() <= HOVER_MAX ? safeUrl : safeUrl.substring(0, HOVER_MAX) + "...";
        return "<click:run_command:'/imagepreviewer preview " + safeUrl + "'>"
                + "<hover:show_text:'" + config.getSegment().getImagePreviewHover() + "\n"
                + "<dark_gray>" + hover + "</dark_gray>'>"
                + config.getSegment().getImageText()
                + "</hover></click>";
    }
}