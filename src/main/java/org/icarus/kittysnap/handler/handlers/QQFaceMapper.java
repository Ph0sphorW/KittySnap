package org.icarus.kittysnap.handler.handlers;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * QQ 表情 ID → 名称映射（枚举，由 koishi QFace index.json 自动生成）
 */
public enum QQFaceMapper {

    // 0 → 惊讶
    SURPRISED(0, "惊讶"),
    // 1 → 撇嘴
    POUT(1, "撇嘴"),
    // 2 → 色
    SEXY(2, "色"),
    // 3 → 发呆
    DAZED(3, "发呆"),
    // 4 → 得意
    PROUD(4, "得意"),
    // 5 → 流泪
    CRYING(5, "流泪"),
    // 6 → 害羞
    SHY(6, "害羞"),
    // 7 → 闭嘴
    SHUT_UP(7, "闭嘴"),
    // 8 → 睡
    SLEEP(8, "睡"),
    // 9 → 大哭
    CRY_LOUDLY(9, "大哭"),
    // 10 → 尴尬
    AWKWARD(10, "尴尬"),
    // 11 → 发怒
    ANGRY(11, "发怒"),
    // 12 → 调皮
    NAUGHTY(12, "调皮"),
    // 13 → 呲牙
    GRIN(13, "呲牙"),
    // 14 → 微笑
    SMILE(14, "微笑"),
    // 15 → 难过
    SAD(15, "难过"),
    // 16 → 酷
    COOL(16, "酷"),
    // 18 → 抓狂
    FRANTIC(18, "抓狂"),
    // 19 → 吐
    SPIT(19, "吐"),
    // 20 → 偷笑
    SNICKER(20, "偷笑"),

    // 21 → 可爱
    CUTE(21, "可爱"),
    // 22 → 白眼
    ROLL_EYES(22, "白眼"),
    // 23 → 傲慢
    ARROGANT(23, "傲慢"),
    // 24 → 饥饿
    HUNGRY(24, "饥饿"),
    // 25 → 困
    SLEEPY(25, "困"),
    // 26 → 惊恐
    SCARED(26, "惊恐"),
    // 27 → 流汗
    SWEAT(27, "流汗"),
    // 28 → 憨笑
    SIMPLE_SMILE(28, "憨笑"),
    // 29 → 悠闲
    LEISURELY(29, "悠闲"),
    // 30 → 奋斗
    STRUGGLE(30, "奋斗"),
    // 31 → 咒骂
    CURSE(31, "咒骂"),
    // 32 → 疑问
    QUESTION(32, "疑问"),
    // 33 → 嘘
    SHUSH(33, "嘘"),
    // 34 → 晕
    DIZZY(34, "晕"),
    // 35 → 折磨
    TORMENT(35, "折磨"),
    // 36 → 衰
    UNLUCKY(36, "衰"),
    // 37 → 骷髅
    SKULL(37, "骷髅"),
    // 38 → 敲打
    HIT(38, "敲打"),
    // 39 → 再见
    GOODBYE(39, "再见"),
    // 41 → 发抖
    SHIVER(41, "发抖"),

    // 42 → 爱情
    LOVE(42, "爱情"),
    // 43 → 跳跳
    JUMP(43, "跳跳"),
    // 46 → 猪头
    PIG(46, "猪头"),
    // 49 → 拥抱
    HUG(49, "拥抱"),
    // 53 → 蛋糕
    CAKE(53, "蛋糕"),
    // 55 → 炸弹
    BOMB(55, "炸弹"),
    // 56 → 刀
    KNIFE(56, "刀"),
    // 59 → 便便
    POOP(59, "便便"),
    // 60 → 咖啡
    COFFEE(60, "咖啡"),
    // 63 → 玫瑰
    ROSE(63, "玫瑰"),
    // 64 → 凋谢
    WITHER(64, "凋谢"),
    // 66 → 爱心
    HEART(66, "爱心"),
    // 67 → 心碎
    BROKEN_HEART(67, "心碎"),
    // 74 → 太阳
    SUN(74, "太阳"),
    // 75 → 月亮
    MOON(75, "月亮"),
    // 76 → 赞
    LIKE(76, "赞"),
    // 77 → 踩
    DISLIKE(77, "踩"),
    // 78 → 握手
    HANDSHAKE(78, "握手"),
    // 79 → 胜利
    VICTORY(79, "胜利"),
    // 85 → 飞吻
    BLOW_KISS(85, "飞吻"),

    // 86 → 怄火
    VEXED(86, "怄火"),
    // 89 → 西瓜
    WATERMELON(89, "西瓜"),
    // 96 → 冷汗
    COLD_SWEAT(96, "冷汗"),
    // 97 → 擦汗
    WIPE_SWEAT(97, "擦汗"),
    // 98 → 抠鼻
    PICK_NOSE(98, "抠鼻"),
    // 99 → 鼓掌
    APPLAUD(99, "鼓掌"),
    // 100 → 糗大了
    MORTIFIED(100, "糗大了"),
    // 101 → 坏笑
    SLY_SMILE(101, "坏笑"),
    // 102 → 左哼哼
    LEFT_HUM(102, "左哼哼"),
    // 103 → 右哼哼
    RIGHT_HUM(103, "右哼哼"),
    // 104 → 哈欠
    YAWN(104, "哈欠"),
    // 105 → 鄙视
    DESPISE(105, "鄙视"),
    // 106 → 委屈
    WRONGED(106, "委屈"),
    // 107 → 快哭了
    ABOUT_TO_CRY(107, "快哭了"),
    // 108 → 阴险
    INSIDIOUS(108, "阴险"),
    // 109 → 左亲亲
    LEFT_KISS(109, "左亲亲"),
    // 110 → 吓
    SCARED2(110, "吓"),
    // 111 → 可怜
    PITIFUL(111, "可怜"),
    // 112 → 菜刀
    KITCHEN_KNIFE(112, "菜刀"),
    // 114 → 篮球
    BASKETBALL(114, "篮球"),

    // 116 → 示爱
    SHOW_LOVE(116, "示爱"),
    // 118 → 抱拳
    FIST(118, "抱拳"),
    // 119 → 勾引
    TEMPT(119, "勾引"),
    // 120 → 拳头
    PUNCH(120, "拳头"),
    // 121 → 差劲
    BAD(121, "差劲"),
    // 122 → 爱你
    LOVE_YOU(122, "爱你"),
    // 123 → NO
    NO(123, "NO"),
    // 124 → OK
    OK(124, "OK"),
    // 125 → 转圈
    SPIN(125, "转圈"),
    // 129 → 挥手
    WAVE(129, "挥手"),
    // 137 → 鞭炮
    FIRECRACKER(137, "鞭炮"),
    // 144 → 喝彩
    CHEER(144, "喝彩"),
    // 146 → 爆筋
    VEIN(146, "爆筋"),
    // 147 → 棒棒糖
    LOLLIPOP(147, "棒棒糖"),
    // 148 → 喝奶
    DRINK_MILK(148, "喝奶"),
    // 169 → 手枪
    GUN(169, "手枪"),
    // 171 → 茶
    TEA(171, "茶"),
    // 172 → 眨眼睛
    WINK(172, "眨眼睛"),
    // 173 → 泪奔
    TEARFUL(173, "泪奔"),
    // 174 → 无奈
    HELPLESS(174, "无奈"),

    // 175 → 卖萌
    CUTE_BEHAVIOR(175, "卖萌"),
    // 176 → 小纠结
    TROUBLED(176, "小纠结"),
    // 177 → 喷血
    SPIT_BLOOD(177, "喷血"),
    // 178 → 斜眼笑
    SLANT_SMILE(178, "斜眼笑"),
    // 179 → doge
    DOGE(179, "doge"),
    // 180 → 惊喜
    SURPRISE(180, "惊喜"),
    // 181 → 戳一戳
    POKE(181, "戳一戳"),
    // 182 → 笑哭
    LOL(182, "笑哭"),
    // 183 → 我最美
    IM_THE_BEST(183, "我最美"),
    // 185 → 羊驼
    ALPACA(185, "羊驼"),
    // 187 → 幽灵
    GHOST(187, "幽灵"),
    // 193 → 大笑
    LAUGH(193, "大笑"),
    // 194 → 不开心
    UNHAPPY(194, "不开心"),
    // 198 → 呃
    UH(198, "呃"),
    // 200 → 求求
    PLEAD(200, "求求"),
    // 201 → 点赞
    THUMBS_UP(201, "点赞"),
    // 202 → 无聊
    BORED(202, "无聊"),
    // 203 → 托脸
    FACE_PALM(203, "托脸"),
    // 204 → 吃
    EAT(204, "吃"),
    // 206 → 害怕
    AFRAID(206, "害怕"),

    // 210 → 飙泪
    TEARS_STREAM(210, "飙泪"),
    // 211 → 我不看
    NOT_LOOKING(211, "我不看"),
    // 212 → 托腮
    CHIN_REST(212, "托腮"),
    // 214 → 啵啵
    KISSY(214, "啵啵"),
    // 215 → 糊脸
    FACE_COVER(215, "糊脸"),
    // 216 → 拍头
    PAT_HEAD(216, "拍头"),
    // 217 → 扯一扯
    PULL(217, "扯一扯"),
    // 218 → 舔一舔
    LICK(218, "舔一舔"),
    // 219 → 蹭一蹭
    RUB(219, "蹭一蹭"),
    // 221 → 顶呱呱
    THUMBS_UP_STRONG(221, "顶呱呱"),
    // 222 → 抱抱
    HUG_HUG(222, "抱抱"),
    // 223 → 暴击
    CRITICAL_HIT(223, "暴击"),
    // 224 → 开枪
    SHOOT(224, "开枪"),
    // 225 → 撩一撩
    FLIRT(225, "撩一撩"),
    // 226 → 拍桌
    TABLE_SLAM(226, "拍桌"),
    // 227 → 拍手
    CLAP(227, "拍手"),
    // 229 → 干杯
    CHEERS(229, "干杯"),
    // 230 → 嘲讽
    MOCK(230, "嘲讽"),
    // 231 → 哼
    HUMPH(231, "哼"),
    // 232 → 佛系
    ZEN(232, "佛系"),

    // 233 → 掐一掐
    PINCH(233, "掐一掐"),
    // 235 → 颤抖
    TREMBLE(235, "颤抖"),
    // 237 → 偷看
    PEEK(237, "偷看"),
    // 238 → 扇脸
    SLAP_FACE(238, "扇脸"),
    // 239 → 原谅
    FORGIVE(239, "原谅"),
    // 240 → 喷脸
    SPRAY_FACE(240, "喷脸"),
    // 241 → 生日快乐
    HAPPY_BIRTHDAY(241, "生日快乐"),
    // 243 → 甩头
    HEAD_SHAKE(243, "甩头"),
    // 244 → 扔狗
    THROW_DOG(244, "扔狗"),
    // 262 → 脑阔疼
    HEADACHE(262, "脑阔疼"),
    // 263 → 沧桑
    VICISSITUDE(263, "沧桑"),
    // 264 → 捂脸
    COVER_FACE(264, "捂脸"),
    // 265 → 辣眼睛
    EYES_BURNING(265, "辣眼睛"),
    // 266 → 哦哟
    OH_YO(266, "哦哟"),
    // 267 → 头秃
    BALD(267, "头秃"),
    // 268 → 问号脸
    QUESTION_FACE(268, "问号脸"),
    // 269 → 暗中观察
    SNEAKING(269, "暗中观察"),
    // 270 → emm
    EMM(270, "emm"),
    // 271 → 吃瓜
    MELON_EATING(271, "吃瓜"),
    // 272 → 呵呵哒
    HEHE(272, "呵呵哒"),

    // 273 → 我酸了
    SOUR(273, "我酸了"),
    // 277 → 汪汪
    WOOF(277, "汪汪"),
    // 278 → 汗
    SWEAT_DROP(278, "汗"),
    // 281 → 无眼笑
    NO_EYE_SMILE(281, "无眼笑"),
    // 282 → 敬礼
    SALUTE(282, "敬礼"),
    // 283 → 狂笑
    LAUGHING_HARD(283, "狂笑"),
    // 284 → 面无表情
    STONEFACE(284, "面无表情"),
    // 285 → 摸鱼
    SLACKING(285, "摸鱼"),
    // 286 → 魔鬼笑
    DEVIL_SMILE(286, "魔鬼笑"),
    // 287 → 哦
    OH(287, "哦"),
    // 288 → 请
    PLEASE(288, "请"),
    // 289 → 睁眼
    EYES_OPEN(289, "睁眼"),
    // 290 → 敲开心
    SO_HAPPY(290, "敲开心"),
    // 292 → 让我康康
    LET_ME_SEE(292, "让我康康"),
    // 293 → 摸锦鲤
    TOUCH_KOI(293, "摸锦鲤"),
    // 294 → 期待
    EXPECTING(294, "期待"),
    // 295 → 拿到红包
    GET_RED_PACKET(295, "拿到红包"),
    // 297 → 拜谢
    BOW_THANKS(297, "拜谢"),
    // 298 → 元宝
    GOLD_INGOT(298, "元宝"),
    // 299 → 牛啊
    COW(299, "牛啊"),

    // 300 → 胖三斤
    FAT_THREE_JIN(300, "胖三斤"),
    // 301 → 好闪
    SO_SHINY(301, "好闪"),
    // 302 → 左拜年
    LEFT_NY_GREET(302, "左拜年"),
    // 303 → 右拜年
    RIGHT_NY_GREET(303, "右拜年"),
    // 305 → 右亲亲
    RIGHT_KISS(305, "右亲亲"),
    // 306 → 牛气冲天
    BULLISH(306, "牛气冲天"),
    // 307 → 喵喵
    MEOW(307, "喵喵"),
    // 311 → 打call
    FAN_CALL(311, "打call"),
    // 312 → 变形
    DEFORM(312, "变形"),
    // 314 → 仔细分析
    ANALYZE(314, "仔细分析"),
    // 317 → 菜汪
    VEGGIE_DOG(317, "菜汪"),
    // 318 → 崇拜
    ADORE(318, "崇拜"),
    // 319 → 比心
    HEART_HAND(319, "比心"),
    // 320 → 庆祝
    CELEBRATE(320, "庆祝"),
    // 322 → 拒绝
    REFUSE(322, "拒绝"),
    // 323 → 嫌弃
    DISDAIN(323, "嫌弃"),
    // 324 → 吃糖
    EAT_CANDY(324, "吃糖"),
    // 325 → 惊吓
    STARTLE(325, "惊吓"),
    // 326 → 生气
    FURIOUS(326, "生气"),
    // 332 → 举牌牌
    SIGN_HOLDING(332, "举牌牌"),

    // 333 → 烟花
    FIREWORKS(333, "烟花"),
    // 334 → 虎虎生威
    TIGER_POWER(334, "虎虎生威"),
    // 336 → 豹富
    LEOPARD_RICH(336, "豹富"),
    // 337 → 花朵脸
    FLOWER_FACE(337, "花朵脸"),
    // 338 → 我想开了
    ENLIGHTENED(338, "我想开了"),
    // 339 → 舔屏
    LICK_SCREEN(339, "舔屏"),
    // 341 → 打招呼
    GREETING(341, "打招呼"),
    // 342 → 酸Q
    SOUR_Q(342, "酸Q"),
    // 343 → 我方了
    IM_DONE(343, "我方了"),
    // 344 → 大怨种
    GRUDGE(344, "大怨种"),
    // 345 → 红包多多
    MANY_RED_PACKETS(345, "红包多多"),
    // 346 → 你真棒棒
    YOURE_GREAT(346, "你真棒棒"),
    // 347 → 大展宏兔
    RABBIT_AMBITION(347, "大展宏兔"),
    // 348 → 福萝卜
    FORTUNE_RADISH(348, "福萝卜"),
    // 349 → 坚强
    STRONG(349, "坚强"),
    // 350 → 贴贴
    SNUGGLE(350, "贴贴"),
    // 351 → 敲敲
    TAP_TAP(351, "敲敲"),
    // 352 → 咦
    EH(352, "咦"),
    // 353 → 拜托
    PLEASE_BEG(353, "拜托"),
    // 354 → 尊嘟假嘟
    REALLY(354, "尊嘟假嘟"),

    // 355 → 耶
    YEAH(355, "耶"),
    // 356 → 666
    E666(356, "666"),
    // 357 → 裂开
    CRACKED(357, "裂开"),
    // 358 → 骰子
    DICE(358, "骰子"),
    // 359 → 包剪锤
    ROCK_PAPER_SCISSORS(359, "包剪锤"),
    // 360 → 亲亲
    KISS_KISS(360, "亲亲"),
    // 361 → 狗狗笑哭
    DOG_LOL(361, "狗狗笑哭"),
    // 362 → 好兄弟
    GOOD_BRO(362, "好兄弟"),
    // 363 → 狗狗可怜
    DOG_PITIFUL(363, "狗狗可怜"),
    // 364 → 超级赞
    SUPER_LIKE(364, "超级赞"),
    // 365 → 狗狗生气
    DOG_ANGRY(365, "狗狗生气"),
    // 366 → 芒狗
    MANGO_DOG(366, "芒狗"),
    // 367 → 狗狗疑问
    DOG_QUESTION(367, "狗狗疑问"),
    // 368 → 奥特笑哭
    ULTRAMAN_LOL(368, "奥特笑哭"),
    // 369 → 彩虹
    RAINBOW(369, "彩虹"),
    // 370 → 祝贺
    CONGRATULATE(370, "祝贺"),
    // 371 → 冒泡
    BUBBLE(371, "冒泡"),
    // 372 → 气呼呼
    PUFFY_ANGRY(372, "气呼呼"),
    // 373 → 忙
    BUSY(373, "忙"),
    // 374 → 波波流泪
    BOBO_CRYING(374, "波波流泪"),

    // 375 → 超级鼓掌
    SUPER_APPLAUD(375, "超级鼓掌"),
    // 376 → 跺脚
    STOMP(376, "跺脚"),
    // 377 → 嗨
    HI(377, "嗨"),
    // 378 → 企鹅笑哭
    PENGUIN_LOL(378, "企鹅笑哭"),
    // 379 → 企鹅流泪
    PENGUIN_CRYING(379, "企鹅流泪"),
    // 380 → 真棒
    AWESOME(380, "真棒"),
    // 381 → 路过
    PASSING_BY(381, "路过"),
    // 382 → emo
    EMO(382, "emo"),
    // 383 → 企鹅爱心
    PENGUIN_HEART(383, "企鹅爱心"),
    // 384 → 晚安
    GOOD_NIGHT(384, "晚安"),
    // 385 → 太气了
    SO_ANGRY(385, "太气了"),
    // 386 → 呜呜呜
    WUWUWU(386, "呜呜呜"),
    // 387 → 太好笑
    SO_FUNNY(387, "太好笑"),
    // 388 → 太头疼
    SO_HEADACHE(388, "太头疼"),
    // 389 → 太赞了
    SO_AMAZING(389, "太赞了"),
    // 390 → 太头秃
    SO_BALD(390, "太头秃"),
    // 391 → 太沧桑
    SO_VICISSITUDE(391, "太沧桑"),
    // 392 → 龙年快乐
    DRAGON_YEAR_HAPPY(392, "龙年快乐"),
    // 393 → 新年中龙
    NEW_YEAR_MID_DRAGON(393, "新年中龙"),
    // 394 → 新年大龙
    NEW_YEAR_BIG_DRAGON(394, "新年大龙"),

    // 395 → 略略略
    LOLOLO(395, "略略略"),
    // 396 → 狼狗
    WOLF_DOG(396, "狼狗"),
    // 397 → 抛媚眼
    WINK_EYE(397, "抛媚眼"),
    // 398 → 超级ok
    SUPER_OK(398, "超级ok"),
    // 399 → tui
    TUI(399, "tui"),
    // 400 → 快乐
    HAPPY(400, "快乐"),
    // 401 → 超级转圈
    SUPER_SPIN(401, "超级转圈"),
    // 402 → 别说话
    DONT_SPEAK(402, "别说话"),
    // 403 → 出去玩
    GO_PLAY(403, "出去玩"),
    // 404 → 闪亮登场
    GLITTER_APPEAR(404, "闪亮登场"),
    // 405 → 好运来
    GOOD_LUCK_COME(405, "好运来"),
    // 406 → 姐是女王
    IM_QUEEN(406, "姐是女王"),
    // 407 → 我听听
    LET_ME_HEAR(407, "我听听"),
    // 408 → 臭美
    SMUG(408, "臭美"),
    // 409 → 送你花花
    SEND_FLOWERS(409, "送你花花"),
    // 410 → 么么哒
    MUAH(410, "么么哒"),
    // 411 → 一起嗨
    PARTY_TOGETHER(411, "一起嗨"),
    // 412 → 开心
    HAPPY_JOY(412, "开心"),
    // 413 → 摇起来
    SHAKE_IT(413, "摇起来"),
    // 415 → 划龙舟
    DRAGON_BOAT_PADDLE(415, "划龙舟"),

    // 416 → 中龙舟
    DRAGON_BOAT_MID(416, "中龙舟"),
    // 417 → 大龙舟
    DRAGON_BOAT_BIG(417, "大龙舟"),
    // 419 → 
    TRAIN1(419, "火车"),
    // 420 → 
    TRAIN2(420, "火车"),
    // 421 → 
    TRAIN3(421, "火车"),
    // 422 → 
    LOTUS(422, "我想开了"),
    // 423 → 
    HIGHSPEED(423, "高铁"),
    // 424 → 
    BUTTON(424, "续标识"),
    // 425 → 求放过
    BEG_MERCY(425, "求放过"),
    // 426 → 玩火
    PLAY_FIRE(426, "玩火"),
    // 427 → 偷感
    SNEAKY(427, "偷感"),
    // 428 → 熬夜
    STAY_UP(428, "熬夜"),
    // 429 → 蛇
    SNAKE1(429, "蛇"),
    // 430 → 蛇
    SNAKE2(430, "蛇"),
    // 431 → 蛇
    SNAKE3(431, "蛇"),
    // 432 → 龙
    DRAGON(432, "龙"),
    // 450 → 撇嘴
    POUT2(450, "撇嘴"),
    // 451 → 色
    SEXY2(451, "色"),
    // 452 → 微笑
    SMILE2(452, "微笑"),
    // 453 → 发呆
    DAZED2(453, "发呆"),
    // 454 → 得意
    PROUD2(454, "得意"),
    // 455 → 害羞
    SHY2(455, "害羞"),
    // 456 → 闭嘴
    SHUT_UP2(456, "闭嘴"),
    // 457 → 睡
    SLEEP2(457, "睡"),
    // 458 → 我吗
    ME(458, "我吗"),
    // 459 → 优雅
    ELEGANT(459, "优雅"),
    // 460 → 硬撑
    TOUGH_IT_OUT(460, "硬撑"),
    // 461 → 宕机
    CRASH(461, "宕机"),
    // 462 → 无语
    SPEECHLESS(462, "无语"),
    // 463 → 新年快乐
    HAPPY_NEW_YEAR(463, "新年快乐"),
    // 464 → 马上到
    ON_THE_WAY(464, "马上到"),
    // 465 → 拆红包
    OPEN_RED_PACKET(465, "拆红包"),
    // 466 → 羞羞哒
    SHY_SHY(466, "羞羞哒"),
    // 467 → 摇花手
    WAVE_HANDS(467, "摇花手"),
    // 468 → 失眠
    INSOMNIA(468, "失眠"),
    // 469 → 坚毅
    PERSEVERANCE(469, "坚毅"),
    // 470 → 马
    HORSE(470, "马");

    private static final Map<Long, String> NAME_MAP = Stream.of(values())
            .collect(Collectors.toUnmodifiableMap(f -> f.id, f -> f.name));

    private final long id;
    private final String name;

    QQFaceMapper(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static String getName(long id) {
        return NAME_MAP.getOrDefault(id, null);
    }
}
