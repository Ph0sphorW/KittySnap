package org.icarus.kittysnap.handler.handlers;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 力竭了。这应该是老 QQ 的数据，QQNT 我还没来得及提
 */
public enum QQFaceMapper {

    SURPRISED(0, "惊讶"), POUT(1, "撇嘴"), SEXY(2, "色"), DAZED(3, "发呆"),
    PROUD(4, "得意"), CRYING(5, "流泪"), SHY(6, "害羞"), SHUT_UP(7, "闭嘴"),
    SLEEP(8, "睡"), SOB(9, "大哭"), AWKWARD(10, "尴尬"), ANGRY(11, "发怒"),
    NAUGHTY(12, "调皮"), GRIN(13, "呲牙"), SMILE(14, "微笑"), SAD(15, "难过"),
    COOL(16, "酷"), PICK_NOSE(17, "抠鼻"), WEEP(18, "哭"), FRANTIC(19, "抓狂"),
    SPIT(20, "吐"), SNICKER(21, "偷笑"), CUTE(22, "可爱"), ROLL_EYES(23, "白眼"),
    ARROGANT(24, "傲慢"), HUNGRY(25, "饥饿"), SLEEPY(26, "困"), SCARED(27, "惊恐"),
    SWEAT(28, "流汗"), SIMPER(29, "憨笑"), LEISURELY(30, "悠闲"), STRUGGLE(31, "奋斗"),
    CURSE(32, "咒骂"), QUESTION(33, "疑问"), SHUSH(34, "嘘"), DIZZY(35, "晕"),
    TORMENT(36, "折磨"), DECAY(37, "衰"), SKULL(38, "骷髅"), HIT(39, "敲打"),
    GOODBYE(40, "再见"), SHIVER(41, "发抖"), LOVE(42, "爱情"), JUMP(43, "跳跳"),
    PIG(44, "猪头"), HUG(45, "拥抱"), MOON(46, "月亮"), LIKE(47, "赞"),
    DISLIKE(48, "踩"), HANDSHAKE(49, "握手"), VICTORY(50, "胜利"), FIST(51, "抱拳"),
    TEMPT(52, "勾引"), PUNCH(53, "拳头"), BAD(54, "差劲"), LOVE_U(55, "爱你"),
    NO(56, "NO"), OK(57, "OK"), SPIN(58, "转圈"), KOWTOW(59, "磕头"),
    TURN(60, "回头"), JUMP_ROPE(61, "跳绳"), WAVE(62, "挥手"), EXCITED(63, "激动"),
    STREET_DANCE(64, "街舞"), KISS(65, "献吻"), LEFT_TAIJI(66, "左太极"), RIGHT_TAIJI(67, "右太极"),
    DOUBLE_HAPPY(68, "双喜"), FIRECRACKER(69, "鞭炮"), LANTERN(70, "灯笼"), GET_RICH(71, "发财"),
    K_SONG(72, "K歌"), SHOPPING(73, "购物"), MAIL(74, "邮件"), HANDSOME(75, "帅"),
    SUGAR_DADDY(76, "求包养"), STRIVE(77, "奋斗"), OVERTIME(78, "加班"), SUDDEN_WEALTH(79, "暴富"),
    GENTLE(80, "温柔"), OK_HAND(81, "行"), NO_HAND(82, "不行"), FURIOUS(83, "生气"),
    FEARFUL(84, "害怕"), SMUG(85, "得瑟"), BROKEN_HEART(86, "心碎"),
    LIGHTNING(96, "闪电"), COUPLE(97, "情侣"), PHONE(98, "手机"), COFFEE(99, "咖啡"),
    CAKE(100, "蛋糕"), MONEY(101, "钱"), POOP(102, "便便"),
    MOON2(103, "月亮"), SUN(104, "太阳"), GIFT(105, "礼物"), HUG2(106, "拥抱"),
    STRONG(107, "强"), WEAK(108, "弱"), HANDSHAKE2(109, "握手"), VICTORY2(110, "胜利"),
    FIST2(111, "抱拳"), TEMPT2(112, "勾引"), PUNCH2(113, "拳头"), BAD2(114, "差劲"),
    LOVE_U2(115, "爱你"), NO2(116, "NO"), OK2(117, "OK"), LOVE2(118, "爱情"),
    BLOW_KISS(119, "飞吻"), JUMP2(120, "跳跳"), SHIVER2(121, "发抖"), VEXED(122, "怄火"),
    SPIN2(123, "转圈"), KOWTOW2(124, "磕头"), TURN2(125, "回头"), JUMP_ROPE2(126, "跳绳"),
    WAVE2(127, "挥手"), EXCITED2(128, "激动"), STREET_DANCE2(129, "街舞"), KISS2(130, "献吻"),
    LEFT_TAIJI2(131, "左太极"), RIGHT_TAIJI2(132, "右太极"),
    DOUBLE_HAPPY2(136, "双喜"), FIRECRACKER2(137, "鞭炮"), LANTERN2(138, "灯笼"),
    GET_RICH2(139, "发财"), K_SONG2(140, "K歌"), SHOPPING2(141, "购物"),
    MAIL2(142, "邮件"), HANDSOME2(143, "帅"), SUGAR_DADDY2(144, "求包养"),
    STRIVE2(145, "奋斗"), OVERTIME2(146, "加班"), SUDDEN_WEALTH2(147, "暴富"),
    GENTLE2(148, "温柔"), OK_HAND2(149, "行"), NO_HAND2(150, "不行"),
    FURIOUS2(151, "生气"), FEARFUL2(152, "害怕"), SMUG2(153, "得瑟"),
    BROKEN_HEART2(154, "心碎"),
    CHEER(155, "喝彩"), PRAY(156, "祈祷"), VEIN(157, "爆筋"),
    LOLLIPOP(158, "棒棒糖"), WATERMELON(159, "西瓜"), TEARFUL(160, "泪奔"),
    SHOW_OFF(161, "献丑"), OFF_TOPIC(162, "跑题"), BOW(163, "低头"),
    APPLAUD(164, "鼓掌"), AWKWARD2(165, "尴尬"), SPIT_BLOOD(166, "吐血"),
    VOMIT(167, "呕吐"), HIT2(168, "敲打"), SWEAT2(169, "汗"),
    SOB2(170, "大哭"), HELPLESS(171, "无奈"), PICK_NOSE2(172, "抠鼻"),
    APPLAUD2(173, "鼓掌"), MORTIFIED(174, "糗大了"), SLY(175, "坏笑"),
    LEFT_HUM(176, "左哼哼"), RIGHT_HUM(177, "右哼哼"), YAWN(178, "哈欠"),
    DESPISE(179, "鄙视"), WRONGED(180, "委屈"), ABOUT_TO_CRY(181, "快哭了"),
    INSIDIOUS(182, "阴险"), KISSY(183, "亲亲"), SCARED2(184, "吓"),
    PITIFUL(185, "可怜"), KITCHEN_KNIFE(186, "菜刀"), BEER(187, "啤酒"),
    BASKETBALL(188, "篮球"), TABLE_TENNIS(189, "乒乓"), COFFEE2(190, "咖啡"),
    RICE(191, "饭"), PIG2(192, "猪头"), ROSE(193, "玫瑰"),
    WITHER(194, "凋谢"), LIPS(195, "嘴唇"), HEART(196, "爱心"),
    BROKEN_HEART3(197, "心碎"), CAKE2(198, "蛋糕"), LIGHTNING2(199, "闪电"),
    BOMB(200, "炸弹"), KNIFE(201, "刀"), FOOTBALL(202, "足球"),
    LADYBUG(203, "瓢虫"), POOP2(204, "便便"), MOON3(205, "月亮"),
    SUN2(206, "太阳"), GIFT2(207, "礼物"), HUG3(208, "拥抱"),
    STRONG2(209, "强"), WEAK2(210, "弱"), HANDSHAKE3(211, "握手"),
    VICTORY3(212, "胜利"), FIST3(213, "抱拳"), TEMPT3(214, "勾引"),
    PUNCH3(215, "拳头"), BAD3(216, "差劲"), LOVE_U3(217, "爱你"),
    NO3(218, "NO"), OK3(219, "OK"), COUPLE2(220, "情侣"),
    BLOW_KISS2(221, "飞吻");

    private static final HashMap<Long, String> NAME_MAP = (HashMap<Long, String>) Stream.of(values())
            .collect(Collectors.toUnmodifiableMap(f -> f.id, f -> f.name));

    private final long id;
    private final String name;

    QQFaceMapper(long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 根据 ID 获取表情名 <p>
     * 未记录 ID 返回 "[未知表情]"
     */
    public static String getName(long id) {
            return '[' + NAME_MAP.getOrDefault(id, "未知表情") + ']';
    }
}
