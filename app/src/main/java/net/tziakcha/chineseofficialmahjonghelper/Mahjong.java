package net.tziakcha.chineseofficialmahjonghelper;

import java.util.Arrays;
import java.util.HashMap;

public final class Mahjong {
    public static final int TILE_SUIT_NONE = 0;  // 无效
    public static final int TILE_SUIT_CHARACTERS = 1;  // 万子（CHARACTERS）
    public static final int TILE_SUIT_BAMBOO = 2;  // 条子（BAMBOO）
    public static final int TILE_SUIT_DOTS = 3;  // 饼子（DOTS）
    public static final int TILE_SUIT_HONORS = 4;  // 字牌（HONORS）

    public static final int TILE_1m = 0x11;
    public static final int TILE_2m = 0x12;
    public static final int TILE_3m = 0x13;
    public static final int TILE_4m = 0x14;
    public static final int TILE_5m = 0x15;
    public static final int TILE_6m = 0x16;
    public static final int TILE_7m = 0x17;
    public static final int TILE_8m = 0x18;
    public static final int TILE_9m = 0x19;
    public static final int TILE_1s = 0x21;
    public static final int TILE_2s = 0x22;
    public static final int TILE_3s = 0x23;
    public static final int TILE_4s = 0x24;
    public static final int TILE_5s = 0x25;
    public static final int TILE_6s = 0x26;
    public static final int TILE_7s = 0x27;
    public static final int TILE_8s = 0x28;
    public static final int TILE_9s = 0x29;
    public static final int TILE_1p = 0x31;
    public static final int TILE_2p = 0x32;
    public static final int TILE_3p = 0x33;
    public static final int TILE_4p = 0x34;
    public static final int TILE_5p = 0x35;
    public static final int TILE_6p = 0x36;
    public static final int TILE_7p = 0x37;
    public static final int TILE_8p = 0x38;
    public static final int TILE_9p = 0x39;
    public static final int TILE_E = 0x41;
    public static final int TILE_S = 0x42;
    public static final int TILE_W = 0x43;
    public static final int TILE_N = 0x44;
    public static final int TILE_C = 0x45;
    public static final int TILE_F = 0x46;
    public static final int TILE_P = 0x47;
    public static final int TILE_TABLE_SIZE = 0x48;

    public static final int[] ALL_TILES = {
            TILE_1m, TILE_2m, TILE_3m, TILE_4m, TILE_5m, TILE_6m, TILE_7m, TILE_8m, TILE_9m,
            TILE_1s, TILE_2s, TILE_3s, TILE_4s, TILE_5s, TILE_6s, TILE_7s, TILE_8s, TILE_9s,
            TILE_1p, TILE_2p, TILE_3p, TILE_4p, TILE_5p, TILE_6p, TILE_7p, TILE_8p, TILE_9p,
            TILE_E, TILE_S, TILE_W, TILE_N, TILE_C, TILE_F, TILE_P
    };

    private static final int[] VALID_MAP = {
            0x3fe0000, 0x3fe03fe, 0xfe, 0, 0, 0, 0, 0
    };

    public static boolean checkTile(int tile) {
        return (tile & ~0x7f) == 0 && (VALID_MAP[tile >> 5] & (1 << (tile & 0x1f))) != 0;
    }

    public static int makeTile(int suit, int rank) {
        return ((suit & 0xF) << 4) | (rank & 0xF);
    }

    public static int tileSuit(int tile) {
        return (tile >> 4) & 0xF;
    }

    public static int tileRank(int tile) {
        return tile & 0xF;
    }

    public static int tileIdx(int tile) {
        return (tileSuit(tile) - 1) * 9 + tileRank(tile) - 1;
    }

    public static final int PACK_TYPE_NONE = 0;  // 无效
    public static final int PACK_TYPE_CHOW = 1;  // 顺子
    public static final int PACK_TYPE_PUNG = 2;  // 刻子
    public static final int PACK_TYPE_KONG = 3;  // 杠
    public static final int PACK_TYPE_PAIR = 4;  // 雀头

    public static int makePack(int offer, int type, int tile) {
        return (offer << 12) | (type << 8) | tile;
    }

    public static int packOffer(int pack) {
        return (pack >> 12) & 0x3;
    }

    public static int packType(int pack) {
        return (pack >> 8) & 0xF;
    }

    public static int packTile(int pack) {
        return pack & 0xFF;
    }

    public static boolean packIsMelded(int pack) {
        return (pack & 0x3000) != 0;
    }

    public static boolean packIsPKong(int pack) {
        return (pack & 0x4000) != 0;
    }

    public static int promotePungToKong(int pack) {
        return pack | 0x4300;
    }

    public static final int ERROR_WRONG_TILES_COUNT = -1;  // 错误的张数
    public static final int ERROR_TILE_MORE_THAN_4 = -2;  // 某张牌出现超过4枚
    public static final int ERROR_NOT_WIN = -3;  // 没和牌

    public static native int calculateFan(final int[] st, final int[] fp, int wt, int cond, int pw, int sw, int flw, int[] ft);

    public static class HandTiles {
        public int[] fp;  // 副露
        public int[] st;  // 立牌
        public int wt;  // 上牌
    }

    public static final int FAN_NONE = 0;  // 无效
    public static final int BIG_FOUR_WINDS = 1;  // 大四喜
    public static final int BIG_THREE_DRAGONS = 2;  // 大三元
    public static final int ALL_GREEN = 3;  // 绿一色
    public static final int NINE_GATES = 4;  // 九莲宝灯
    public static final int FOUR_KONGS = 5;  // 四杠
    public static final int SEVEN_SHIFTED_PAIRS = 6;  // 连七对
    public static final int THIRTEEN_ORPHANS = 7;  // 十三幺
    public static final int ALL_TERMINALS = 8;  // 清幺九
    public static final int LITTLE_FOUR_WINDS = 9;  // 小四喜
    public static final int LITTLE_THREE_DRAGONS = 10;  // 小三元
    public static final int ALL_HONORS = 11;  // 字一色
    public static final int FOUR_CONCEALED_PUNGS =  12;  // 四暗刻
    public static final int PURE_TERMINAL_CHOWS =   13;  // 一色双龙会
    public static final int QUADRUPLE_CHOW =        14;  // 一色四同顺
    public static final int FOUR_PURE_SHIFTED_PUNGS = 15;  // 一色四节高
    public static final int FOUR_PURE_SHIFTED_CHOWS_1 = 16;  // 一色四步高Ⅰ
    public static final int FOUR_PURE_SHIFTED_CHOWS_2 = 17;  // 一色四步高Ⅱ
    public static final int THREE_KONGS = 18;  // 三杠
    public static final int ALL_TERMINALS_AND_HONORS = 19;  // 混幺九
    public static final int SEVEN_PAIRS = 20;  // 七对
    public static final int GREATER_HONORS_AND_KNITTED_TILES = 21;  // 七星不靠
    public static final int ALL_EVEN_PUNGS = 22;  // 全双刻
    public static final int FULL_FLUSH = 23;  // 清一色
    public static final int PURE_TRIPLE_CHOW = 24;  // 一色三同顺
    public static final int PURE_SHIFTED_PUNGS = 25;  // 一色三节高
    public static final int UPPER_TILES = 26;  // 全大
    public static final int MIDDLE_TILES = 27;  // 全中
    public static final int LOWER_TILES = 28;  // 全小
    public static final int PURE_STRAIGHT = 29;  // 清龙
    public static final int THREE_SUITED_TERMINAL_CHOWS = 30;  // 三色双龙会
    public static final int PURE_SHIFTED_CHOWS_1 = 31;  // 一色三步高Ⅰ
    public static final int PURE_SHIFTED_CHOWS_2 = 32;  // 一色三步高Ⅱ
    public static final int ALL_FIVE = 33;  // 全带五
    public static final int TRIPLE_PUNG = 34;  // 三同刻
    public static final int THREE_CONCEALED_PUNGS = 35;  // 三暗刻
    public static final int LESSER_HONORS_AND_KNITTED_TILES = 36;  // 全不靠
    public static final int KNITTED_STRAIGHT = 37;  // 组合龙
    public static final int UPPER_FOUR = 38;  // 大于五
    public static final int LOWER_FOUR = 39;  // 小于五
    public static final int BIG_THREE_WINDS = 40;  // 三风刻
    public static final int MIXED_STRAIGHT = 41;  // 花龙
    public static final int REVERSIBLE_TILES = 42;  // 推不倒
    public static final int MIXED_TRIPLE_CHOW = 43;  // 三色三同顺
    public static final int MIXED_SHIFTED_PUNGS = 44;  // 三色三节高
    public static final int CHICKEN_HAND = 45;  // 无番和
    public static final int LAST_TILE_DRAW = 46;  // 妙手回春
    public static final int LAST_TILE_CLAIM = 47;  // 海底捞月
    public static final int OUT_WITH_REPLACEMENT_TILE = 48;  // 杠上开花
    public static final int ROBBING_THE_KONG = 49;  // 抢杠和
    public static final int ALL_PUNGS = 50;  // 碰碰和
    public static final int HALF_FLUSH = 51;  // 混一色
    public static final int MIXED_SHIFTED_CHOWS = 52;  // 三色三步高
    public static final int ALL_TYPES = 53;  // 五门齐
    public static final int MELDED_HAND = 54;  // 全求人
    public static final int TWO_CONCEALED_KONGS = 55;  // 双暗杠
    public static final int TWO_DRAGONS_PUNGS = 56;  // 双箭刻
    public static final int OUTSIDE_HAND = 57;  // 全带幺
    public static final int FULLY_CONCEALED_HAND = 58;  // 不求人
    public static final int TWO_MELDED_KONGS = 59;  // 双明杠
    public static final int LAST_TILE = 60;  // 和绝张
    public static final int DRAGON_PUNG = 61;  // 箭刻
    public static final int PREVALENT_WIND = 62;  // 圈风刻
    public static final int SEAT_WIND = 63;  // 门风刻
    public static final int CONCEALED_HAND = 64;  // 门前清
    public static final int ALL_CHOWS = 65;  // 平和
    public static final int TILE_HOG = 66;  // 四归一
    public static final int DOUBLE_PUNG = 67;  // 双同刻
    public static final int TWO_CONCEALED_PUNGS = 68;  // 双暗刻
    public static final int CONCEALED_KONG = 69;  // 暗杠
    public static final int ALL_SIMPLES = 70;  // 断幺
    public static final int PURE_DOUBLE_CHOW = 71;  // 一般高
    public static final int MIXED_DOUBLE_CHOW = 72;  // 喜相逢
    public static final int SHORT_STRAIGHT = 73;  // 连六
    public static final int TWO_TERMINAL_CHOWS = 74;  // 老少副
    public static final int PUNG_OF_TERMINALS_OR_HONORS = 75;  // 幺九刻
    public static final int MELDED_KONG = 76;  // 明杠
    public static final int ONE_VOIDED_SUIT = 77;  // 缺一门
    public static final int NO_HONORS = 78;  // 无字
    public static final int EDGE_WAIT = 79;  // 独听・边张
    public static final int CLOSED_WAIT = 80;  // 独听・嵌张
    public static final int SINGLE_WAIT = 81;  // 独听・单钓
    public static final int SELF_DRAWN = 82;  // 自摸
    public static final int FLOWER_TILES = 83;  // 花牌
    public static final int CONCEALED_KONG_AND_MELDED_KONG = 84;  // 明暗杠
    public static final int BLESSING_OF_HEAVEN = 85;  // 天和
    public static final int BLESSING_OF_EARTH = 86;  // 地和
    public static final int BLESSING_OF_HUMAN_1 = 87;  // 人和Ⅰ（点和）
    public static final int BLESSING_OF_HUMAN_2 = 88;  // 人和Ⅱ（自摸）

    public static final String[] FAN_NAME = {
            "无",
            "大四喜", "大三元", "绿一色", "九莲宝灯", "四杠", "连七对", "十三幺",
            "清幺九", "小四喜", "小三元", "字一色", "四暗刻", "一色双龙会",
            "一色四同顺", "一色四节高",
            "一色四步高①", "一色四步高②", "三杠", "混幺九",
            "七对", "七星不靠", "全双刻", "清一色", "一色三同顺", "一色三节高", "全大", "全中", "全小",
            "清龙", "三色双龙会", "一色三步高①", "一色三步高②", "全带五", "三同刻", "三暗刻",
            "全不靠", "组合龙", "大于五", "小于五", "三风刻",
            "花龙", "推不倒", "三色三同顺", "三色三节高", "无番和", "妙手回春", "海底捞月", "杠上开花", "抢杠和",
            "碰碰和", "混一色", "三色三步高", "五门齐", "全求人", "双暗杠", "双箭刻",
            "全带幺", "不求人", "双明杠", "和绝张",
            "箭刻", "圈风刻", "门风刻", "门前清", "平和", "四归一", "双同刻", "双暗刻", "暗杠", "断幺",
            "一般高", "喜相逢", "连六", "老少副", "幺九刻", "明杠", "缺一门", "无字", "独听・边张", "独听・嵌张", "独听・单钓", "自摸",
            "花牌",
            "明暗杠",
            "天和", "地和", "人和①", "人和②"
    };

    public static final int[] FAN_VALUE_TABLE = {
            0,
            88, 88, 88, 88, 88, 88, 88,
            64, 64, 64, 64, 64, 64,
            48, 48,
            32, 32, 32, 32,
            24, 24, 24, 24, 24, 24, 24, 24, 24,
            16, 16, 16, 16, 16, 16, 16,
            12, 12, 12, 12, 12,
            8, 8, 8, 8, 8, 8, 8, 8, 8,
            6, 6, 6, 6, 6, 6, 6,
            4, 4, 4, 4,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1,
            5,
            8, 8, 8, 8
    };

    public static final String[] WIND_TEXT = {"东", "南", "西", "北"};

    public static final int FORM_FLAG_REGULAR = 0x01;  // 基本和型
    public static final int FORM_FLAG_SEVEN_PAIRS = 0x02;  // 七对
    public static final int FORM_FLAG_THIRTEEN_ORPHANS = 0x04;  // 十三幺
    public static final int FORM_FLAG_HONORS_AND_KNITTED_TILES = 0x08;  // 全不靠
    public static final int FORM_FLAG_KNITTED_STRAIGHT = 0x10; // 组合龙
    public static final int FORM_FLAG_ALL = 0xFF;  // 全部和型

    public static class EnumResult {
        public int discard;  // 打这张牌
        public int form;  // 和牌形式
        public int shanten;  // 上听数
        public long useful;  // 有效牌标记表，每张牌占1个bit
    }

    public static native EnumResult[] enumDiscardTile(final int[] st, int wt);

    private static void insertInto(int[] a, int i, int p) {
        while (i > 0) {
            if (a[i - 1] > p) {
                a[i] = a[i - 1];
            } else {
                break;
            }
            --i;
        }
        a[i] = p;
    }
    public static boolean substantiallyEqual(HandTiles a, HandTiles b) {
        int fpaLen = a.fp != null ? a.fp.length : 0;
        int fpbLen = b.fp != null ? b.fp.length : 0;
        if (fpaLen != fpbLen) return false;

        int staLen = a.st != null ? a.st.length : 0;
        int stbLen = b.st != null ? b.st.length : 0;
        if (staLen != stbLen) return false;

        if (fpaLen != 0) {
            int[] fpa = new int[fpaLen];
            int[] fpb = new int[fpaLen];
            for (int i = 0; i < fpaLen; ++i) {
                // 副露只需要比较类型和牌，无需考虑供牌来源，所以统一当成1
                insertInto(fpa, i, makePack(1, packType(a.fp[i]), packTile(a.fp[i])));
                insertInto(fpb, i, makePack(1, packType(b.fp[i]), packTile(b.fp[i])));
            }
            if (!Arrays.equals(fpa, fpb)) {
                return false;
            }
        }

        if (staLen != 0) {
            int[] sta = new int[staLen + 1];
            int[] stb = new int[staLen + 1];
            for (int i = 0; i < staLen; ++i) {
                insertInto(sta, i, a.st[i]);
                insertInto(stb, i, b.st[i]);
            }
            // 把上牌也当成手牌判断
            insertInto(sta, staLen, a.wt);
            insertInto(stb, staLen, b.wt);
            return Arrays.equals(sta, stb);
        }

        return true;
    }

    private static final char[] NUMBERED_CHARS = {'m', 's', 'p'};
    private static final char[] HONOR_CHARS = {'E', 'S', 'W', 'N', 'C', 'F', 'P'};

    public static String handTilesToString(final int[] fp, final int[] st, int wt) {
        StringBuilder str = new StringBuilder();

        int maxCnt = 13;
        if (fp != null) {
            maxCnt -= fp.length * 3;
            for (int pack : fp) {
                int t = packTile(pack);
                if (checkTile(t)) {
                    int o = packOffer(pack);
                    int r = tileRank(t);
                    int s = tileSuit(t);
                    switch (packType(pack)) {
                        case PACK_TYPE_CHOW:
                            str.append('[');
                            if (s != TILE_SUIT_HONORS) {
                                str.append((char)(r - 1 + '0'));
                                str.append((char)(r + '0'));
                                str.append((char)(r + 1 + '0'));
                                str.append(NUMBERED_CHARS[s - 1]);
                                if (o != 1) str.append((char)(o + '0'));
                            }
                            str.append(']');
                            break;
                        case PACK_TYPE_PUNG:
                            str.append('[');
                            if (s != TILE_SUIT_HONORS) {
                                str.append((char)(r + '0'));
                                str.append((char)(r + '0'));
                                str.append((char)(r + '0'));
                                str.append(NUMBERED_CHARS[s - 1]);
                            } else {
                                str.append(HONOR_CHARS[r - 1]);
                                str.append(HONOR_CHARS[r - 1]);
                                str.append(HONOR_CHARS[r - 1]);
                                str.append(HONOR_CHARS[r - 1]);
                            }
                            if (o != 1) str.append((char)(o + '0'));
                            str.append(']');
                            break;
                        case PACK_TYPE_KONG:
                            str.append('[');
                            if (s != TILE_SUIT_HONORS) {
                                str.append((char)(r + '0'));
                                str.append((char)(r + '0'));
                                str.append((char)(r + '0'));
                                str.append((char)(r + '0'));
                                str.append(NUMBERED_CHARS[s - 1]);
                            } else {
                                str.append(HONOR_CHARS[r - 1]);
                                str.append(HONOR_CHARS[r - 1]);
                                str.append(HONOR_CHARS[r - 1]);
                                str.append(HONOR_CHARS[r - 1]);
                            }
                            if (o != 0) {
                                str.append((char)((packIsPKong(pack) ? o | 0x4 : o) + '0'));
                            }
                            str.append(']');
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        int suit = TILE_SUIT_NONE;
        maxCnt = Math.min(maxCnt, st.length);
        for (int i = 0; i < maxCnt; ++i) {
            int t = st[i];
            if (checkTile(t)) {
                int r = tileRank(t);
                int s = tileSuit(t);

                // 花色有变化，写入一个后缀，结束之前的花色
                if (suit != s) {
                    if (suit != TILE_SUIT_NONE && suit != TILE_SUIT_HONORS) {
                        str.append(NUMBERED_CHARS[suit - 1]);
                    }
                    suit = s;
                }

                if (s != TILE_SUIT_HONORS) {
                    // 数牌，先写入数字
                    str.append((char)(r + '0'));
                } else {
                    // 字牌，直接写相应字符
                    str.append(HONOR_CHARS[r - 1]);
                }
            }
        }

        // 补充未写入的花色后缀
        if (suit != TILE_SUIT_NONE && suit != TILE_SUIT_HONORS) {
            str.append(NUMBERED_CHARS[suit - 1]);
        }

        if (checkTile(wt)) {
            int r = tileRank(wt);
            int s = tileSuit(wt);
            if (s != TILE_SUIT_HONORS) {
                str.append((char)(r + '0'));
                str.append(NUMBERED_CHARS[s - 1]);
            } else {
                str.append(HONOR_CHARS[r - 1]);
            }
        }

        return str.toString();
    }

    public static final int PARSE_NO_ERROR = 0;                                 // 无错误
    public static final int PARSE_ERROR_ILLEGAL_CHARACTER = -1;                 // 非法字符
    public static final int PARSE_ERROR_SUFFIX = -2;                            // 后缀错误
    public static final int PARSE_ERROR_WRONG_TILES_COUNT_FOR_FIXED_PACK = -3;  // 副露包含错误的牌数目
    public static final int PARSE_ERROR_CANNOT_MAKE_FIXED_PACK = -4;            // 无法正确解析副露
    public static final int PARSE_ERROR_TOO_MANY_FIXED_PACKS = -5;              // 过多组副露（一副合法手牌最多4副露）
    public static final int PARSE_ERROR_TOO_MANY_TILES = -6;                    // 过多牌
    public static final int PARSE_ERROR_TILE_COUNT_GREATER_THAN_4 = -7;         // 某张牌出现超过4枚

    public static int parseHandTiles(final String str, HandTiles handTiles) {
        boolean bracket = false;
        int[] fixedPacks = new int[4];  // 副露
        int packCnt = 0;
        int[] standingTiles = new int[14];  // 立牌，包括最新上牌
        int tileCnt = 0, tileMax = 14;  // 立牌长度、最大长度
        int[] digitRank = new int[14];  // 数字串
        int digitCnt = 0, digitMax = 14;  // 数字串长度、最大长度
        int[] tempTile = new int[14];  // 临时牌
        int tempCnt = 0, tempMax = 14;  // 临时牌长度、最大长度
        int[] tile_table = new int[TILE_P + 1];  // 牌表

        for (int k = 0, len = str.length(); k < len; ++k) {
            int submitSuit = TILE_SUIT_NONE;  // 待提交的花色
            int submitHonor = 0;  // 待提交的字牌

            char ch = str.charAt(k);
            switch (ch) {
                case '1':case '2':case '3':
                case '4':case '5':case '6':
                case '7':case '8':case '9':
                    // 添加到数字串
                    // 如果在括号里，则不需要考虑临时牌长度
                    // 如果在括号外，临时牌已经满时，再添加已经没有意义了，反正这些牌也不会用到
                    if ((bracket || tempCnt < tempMax) && digitCnt < digitMax) {
                        digitRank[digitCnt++] = ch - '0';
                    }
                    break;
                case 'm': submitSuit = TILE_SUIT_CHARACTERS; break;
                case 's': submitSuit = TILE_SUIT_BAMBOO; break;
                case 'p': submitSuit = TILE_SUIT_DOTS; break;
                case 'E': submitHonor = TILE_E; break;
                case 'S': submitHonor = TILE_S; break;
                case 'W': submitHonor = TILE_W; break;
                case 'N': submitHonor = TILE_N; break;
                case 'C': submitHonor = TILE_C; break;
                case 'F': submitHonor = TILE_F; break;
                case 'P': submitHonor = TILE_P; break;
                case '[':
                    // 开始副露
                    if (bracket) return PARSE_ERROR_ILLEGAL_CHARACTER;
                    if (packCnt == 4) return PARSE_ERROR_TOO_MANY_FIXED_PACKS;
                    if (digitCnt != 0) return PARSE_ERROR_SUFFIX;

                    // 把临时牌提交到手牌，并打表
                    for (int i = 0; i < tempCnt && tileCnt < tileMax; ++i) {
                        int t = tempTile[i];
                        if (++tile_table[t] > 4) return PARSE_ERROR_TILE_COUNT_GREATER_THAN_4;
                        standingTiles[tileCnt++] = t;
                    }

                    // 手牌空间不足3张，说明这组副露多余的
                    if (tileCnt + 3 > tileMax) return PARSE_ERROR_TOO_MANY_TILES;

                    tempCnt = 0;
                    bracket = true;
                    tempMax = 5;  // 副露最多4张牌，为了能提示超出错误，多预留1
                    digitMax = 5;
                    break;
                case ']': {
                    // 结束副露
                    if (!bracket) return PARSE_ERROR_ILLEGAL_CHARACTER;
                    if (tempCnt == 0) return PARSE_ERROR_WRONG_TILES_COUNT_FOR_FIXED_PACK;

                    // 解析副露来源
                    int o = 0;
                    if (digitCnt == 1) {
                        o = digitRank[0];
                        digitCnt = 0;
                    }
                    // 存在多余的数字
                    if (digitCnt != 0) return PARSE_ERROR_ILLEGAL_CHARACTER;

                    switch (tempCnt) {
                        case 3: {
                            // 3张牌的情况
                            int t0 = tempTile[0], t1 = tempTile[1], t2 = tempTile[2];
                            if (o == 0) o = 1;

                            // 3张牌的副露来源只能是123
                            if (o > 3) return PARSE_ERROR_ILLEGAL_CHARACTER;

                            // 相同，构成碰
                            if (t0 == t1 && t0 == t2) {
                                fixedPacks[packCnt++] = makePack(o, PACK_TYPE_PUNG, t0);
                            } else {
                                // 不相同，尝试构成吃
                                if ((t0 & 0xc0) == 0) {  // 判断数牌
                                    if ((t0 + 1 == t1 && t1 + 1 == t2) || (t2 + 1 == t1 && t1 + 1 == t0)) {
                                        fixedPacks[packCnt++] = makePack(o, PACK_TYPE_CHOW, t1);
                                    } else if ((t0 + 1 == t2 && t2 + 1 == t1) || (t1 + 1 == t2 && t2 + 1 == t0)) {
                                        fixedPacks[packCnt++] = makePack(o, PACK_TYPE_CHOW, t2);
                                    } else if ((t1 + 1 == t0 && t0 + 1 == t2) || (t2 + 1 == t0 && t0 + 1 == t1)) {
                                        fixedPacks[packCnt++] = makePack(o, PACK_TYPE_CHOW, t0);
                                    } else {
                                        return PARSE_ERROR_CANNOT_MAKE_FIXED_PACK;
                                    }
                                } else {
                                    return PARSE_ERROR_CANNOT_MAKE_FIXED_PACK;
                                }
                            }
                            break;
                        }
                        case 4: {
                            // 4张牌只能是杠
                            int t = tempTile[0];
                            if (t == tempTile[1] && t == tempTile[2] && t == tempTile[3]) {
                                // 4张牌的副露来源可以是123或567
                                if (o > 7 || o == 4) return PARSE_ERROR_ILLEGAL_CHARACTER;

                                fixedPacks[packCnt++] = makePack(o, PACK_TYPE_KONG, t);
                            } else {
                                return PARSE_ERROR_CANNOT_MAKE_FIXED_PACK;
                            }
                            break;
                        }
                        default:
                            // 其他张数都是错误
                            return PARSE_ERROR_WRONG_TILES_COUNT_FOR_FIXED_PACK;
                    }

                    // 打表
                    for (int i = 0; i < tempCnt; ++i) {
                        if (++tile_table[tempTile[i]] > 4) return PARSE_ERROR_TILE_COUNT_GREATER_THAN_4;
                    }
                    tempCnt = 0;
                    bracket = false;
                    tileMax -= 3;  // NOTE: 这里可以直接-3，因为左括号的情况已经判断了够减
                    tempMax = tileMax;
                    digitMax = tileMax;
                    break;
                }
                default: return PARSE_ERROR_ILLEGAL_CHARACTER;
            }

            // 把数字串转成数牌提交到临时牌中
            if (submitSuit != TILE_SUIT_NONE) {
                if (digitCnt == 0) return PARSE_ERROR_SUFFIX;
                for (int i = 0; i < digitCnt && tempCnt < tempMax; ++i) {
                    tempTile[tempCnt++] = makeTile(submitSuit, digitRank[i]);
                }
                digitCnt = 0;
            }

            // 提交字牌
            if (submitHonor != 0) {
                if (digitCnt != 0) return PARSE_ERROR_SUFFIX;
                if (tempCnt < tempMax) {
                    tempTile[tempCnt++] = submitHonor;
                }
            }

            // 满了，提前退出
            if (!bracket && tempCnt == tempMax) {
                break;
            }
        }

        if (digitCnt != 0) return PARSE_ERROR_SUFFIX;

        // 处理余下的临时牌
        // 把临时牌提交到手牌，并打表
        for (int i = 0; i < tempCnt && tileCnt < tileMax; ++i) {
            int t = tempTile[i];
            if (++tile_table[t] > 4) return PARSE_ERROR_TILE_COUNT_GREATER_THAN_4;
            standingTiles[tileCnt++] = t;
        }

        // 无错误时再写回数据
        if (tileCnt == tileMax) {
            handTiles.st = Arrays.copyOfRange(standingTiles, 0, tileMax - 1);
            handTiles.wt = standingTiles[tileMax - 1];
        } else {
            handTiles.st = Arrays.copyOfRange(standingTiles, 0, tileCnt);
        }

        if (packCnt != 0) {
            handTiles.fp = Arrays.copyOf(fixedPacks, packCnt);
        }

        return PARSE_NO_ERROR;
    }

    public static String getParseResultString(int res) {
        switch (res) {
            case PARSE_NO_ERROR:
                return null;
            case PARSE_ERROR_ILLEGAL_CHARACTER:
                return "无法解析的字符";
            case PARSE_ERROR_SUFFIX:
                return "错误的后缀";
            case PARSE_ERROR_WRONG_TILES_COUNT_FOR_FIXED_PACK:
                return "错误的副露的牌数目";
            case PARSE_ERROR_CANNOT_MAKE_FIXED_PACK:
                return "无法正确解析副露";
            case PARSE_ERROR_TOO_MANY_FIXED_PACKS:
                return "副露组数过多";
            case PARSE_ERROR_TOO_MANY_TILES:
                return "手牌过多";
            case PARSE_ERROR_TILE_COUNT_GREATER_THAN_4:
                return "一种牌最多只有4张";
            default:
                return "未知错误";
        }
    }

    public static final HashMap<Integer, String> COMBINE_TWO_FAN_NAME = new HashMap<Integer, String>(){{
        put(BIG_FOUR_WINDS << 8 | FOUR_KONGS, "大四喜四杠");
        put(BIG_FOUR_WINDS << 8 | ALL_HONORS, "大四喜字一色");
        put(BIG_FOUR_WINDS << 8 | FOUR_CONCEALED_PUNGS, "大四喜四暗");
        put(BIG_FOUR_WINDS << 8 | THREE_KONGS, "大四喜三杠");
        put(BIG_FOUR_WINDS << 8 | ALL_TERMINALS_AND_HONORS, "大四喜混幺九");
        put(BIG_FOUR_WINDS << 8 | THREE_CONCEALED_PUNGS, "大四喜三暗");
        put(BIG_FOUR_WINDS << 8 | LAST_TILE_DRAW, "妙手大四喜");
        put(BIG_FOUR_WINDS << 8 | LAST_TILE_CLAIM, "海底大四喜");
        put(BIG_FOUR_WINDS << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开大四喜");
        put(BIG_FOUR_WINDS << 8 | MELDED_HAND, "大四喜全求人");
        put(BIG_FOUR_WINDS << 8 | TWO_CONCEALED_KONGS, "大四喜双暗杠");
        put(BIG_FOUR_WINDS << 8 | TWO_MELDED_KONGS, "大四喜双明杠");
        put(BIG_THREE_DRAGONS << 8 | FOUR_KONGS, "大三元四杠");
        put(BIG_THREE_DRAGONS << 8 | ALL_HONORS, "大三元字一色");
        put(BIG_THREE_DRAGONS << 8 | FOUR_CONCEALED_PUNGS, "大三元四暗");
        put(BIG_THREE_DRAGONS << 8 | THREE_KONGS, "大三元三杠");
        put(BIG_THREE_DRAGONS << 8 | ALL_TERMINALS_AND_HONORS, "大三元混幺九");
        put(BIG_THREE_DRAGONS << 8 | THREE_CONCEALED_PUNGS, "大三元三暗");
        put(BIG_THREE_DRAGONS << 8 | LAST_TILE_DRAW, "妙手大三元");
        put(BIG_THREE_DRAGONS << 8 | LAST_TILE_CLAIM, "海底大三元");
        put(BIG_THREE_DRAGONS << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开大三元");
        put(BIG_THREE_DRAGONS << 8 | ROBBING_THE_KONG, "大三元抢杠");
        put(BIG_THREE_DRAGONS << 8 | ALL_PUNGS, "大三元碰碰和");
        put(BIG_THREE_DRAGONS << 8 | HALF_FLUSH, "大三元混一色");
        put(BIG_THREE_DRAGONS << 8 | MELDED_HAND, "大三元全求人");
        put(BIG_THREE_DRAGONS << 8 | TWO_CONCEALED_KONGS, "大三元双暗杠");
        put(BIG_THREE_DRAGONS << 8 | OUTSIDE_HAND, "大三元全带幺");
        put(BIG_THREE_DRAGONS << 8 | TWO_MELDED_KONGS, "大三元双明杠");
        put(BIG_THREE_DRAGONS << 8 | LAST_TILE, "大三元绝张");
        put(ALL_GREEN << 8 | FOUR_KONGS, "绿一色四杠");
        put(ALL_GREEN << 8 | FOUR_CONCEALED_PUNGS, "绿一色四暗");
        put(ALL_GREEN << 8 | QUADRUPLE_CHOW, "绿一色四同顺");
        put(ALL_GREEN << 8 | THREE_KONGS, "绿一色三杠");
        put(ALL_GREEN << 8 | SEVEN_PAIRS, "绿一色七对");
        put(ALL_GREEN << 8 | FULL_FLUSH, "清绿一色");
        put(ALL_GREEN << 8 | PURE_TRIPLE_CHOW, "绿一色三同顺");
        put(ALL_GREEN << 8 | PURE_SHIFTED_PUNGS, "绿一色三节");
        put(ALL_GREEN << 8 | THREE_CONCEALED_PUNGS, "绿一色三暗");
        put(ALL_GREEN << 8 | LAST_TILE_DRAW, "妙手绿一色");
        put(ALL_GREEN << 8 | LAST_TILE_CLAIM, "海底绿一色");
        put(ALL_GREEN << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开绿一色");
        put(ALL_GREEN << 8 | ROBBING_THE_KONG, "绿一色抢杠");
        put(ALL_GREEN << 8 | ALL_PUNGS, "绿一色碰碰和");
        put(ALL_GREEN << 8 | MELDED_HAND, "绿一色全求人");
        put(ALL_GREEN << 8 | TWO_CONCEALED_KONGS, "绿一色双暗杠");
        put(ALL_GREEN << 8 | FULLY_CONCEALED_HAND, "绿一色不求人");
        put(ALL_GREEN << 8 | TWO_MELDED_KONGS, "绿一色双明杠");
        put(ALL_GREEN << 8 | LAST_TILE, "绿一色绝张");
        put(NINE_GATES << 8 | PURE_STRAIGHT, "九莲清龙");
        put(NINE_GATES << 8 | LAST_TILE_DRAW, "妙手九莲");
        put(NINE_GATES << 8 | LAST_TILE_CLAIM, "海底九莲");
        put(FOUR_KONGS << 8 | ALL_TERMINALS, "清幺九四杠");
        put(FOUR_KONGS << 8 | LITTLE_FOUR_WINDS, "小四喜四杠");
        put(FOUR_KONGS << 8 | LITTLE_THREE_DRAGONS, "小三元四杠");
        put(FOUR_KONGS << 8 | ALL_HONORS, "字一色四杠");
        put(FOUR_KONGS << 8 | FOUR_CONCEALED_PUNGS, "四杠四暗");
        put(FOUR_KONGS << 8 | FOUR_PURE_SHIFTED_PUNGS, "四杠四节");
        put(FOUR_KONGS << 8 | ALL_TERMINALS_AND_HONORS, "混幺九四杠");
        put(FOUR_KONGS << 8 | ALL_EVEN_PUNGS, "全双四杠");
        put(FOUR_KONGS << 8 | FULL_FLUSH, "清一色四杠");
        put(FOUR_KONGS << 8 | UPPER_TILES, "全大四杠");
        put(FOUR_KONGS << 8 | MIDDLE_TILES, "全中四杠");
        put(FOUR_KONGS << 8 | LOWER_TILES, "全小四杠");
        put(FOUR_KONGS << 8 | TRIPLE_PUNG, "四杠三同刻");
        put(FOUR_KONGS << 8 | THREE_CONCEALED_PUNGS, "四杠三暗");
        put(FOUR_KONGS << 8 | UPPER_FOUR, "大于五四杠");
        put(FOUR_KONGS << 8 | LOWER_FOUR, "小于五四杠");
        put(FOUR_KONGS << 8 | BIG_THREE_WINDS, "三风四杠");
        put(FOUR_KONGS << 8 | REVERSIBLE_TILES, "推不倒四杠");
        put(FOUR_KONGS << 8 | LAST_TILE_DRAW, "妙手四杠");
        put(FOUR_KONGS << 8 | LAST_TILE_CLAIM, "海底四杠");
        put(FOUR_KONGS << 8 | OUT_WITH_REPLACEMENT_TILE, "四杠杠开");
        put(FOUR_KONGS << 8 | HALF_FLUSH, "混一色四杠");
        put(FOUR_KONGS << 8 | ALL_TYPES, "五门四杠");
        put(FOUR_KONGS << 8 | MELDED_HAND, "四杠全求人");
        put(FOUR_KONGS << 8 | TWO_DRAGONS_PUNGS, "四杠双箭");
        put(SEVEN_SHIFTED_PAIRS << 8 | LAST_TILE_DRAW, "妙手连七对");
        put(SEVEN_SHIFTED_PAIRS << 8 | LAST_TILE_CLAIM, "海底连七对");
        put(THIRTEEN_ORPHANS << 8 | LAST_TILE_DRAW, "妙手十三幺");
        put(THIRTEEN_ORPHANS << 8 | LAST_TILE_CLAIM, "海底十三幺");
        put(THIRTEEN_ORPHANS << 8 | ROBBING_THE_KONG, "十三幺抢杠");
        put(THIRTEEN_ORPHANS << 8 | LAST_TILE, "十三幺绝张");
        put(ALL_TERMINALS << 8 | FOUR_CONCEALED_PUNGS, "清幺九四暗");
        put(ALL_TERMINALS << 8 | THREE_KONGS, "清幺九三杠");
        put(ALL_TERMINALS << 8 | SEVEN_PAIRS, "清幺九七对");
        put(ALL_TERMINALS << 8 | TRIPLE_PUNG, "清幺九三同刻");
        put(ALL_TERMINALS << 8 | THREE_CONCEALED_PUNGS, "清幺九三暗");
        put(ALL_TERMINALS << 8 | LAST_TILE_DRAW, "妙手清幺九");
        put(ALL_TERMINALS << 8 | LAST_TILE_CLAIM, "海底清幺九");
        put(ALL_TERMINALS << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开清幺九");
        put(ALL_TERMINALS << 8 | MELDED_HAND, "清幺九全求人");
        put(ALL_TERMINALS << 8 | TWO_CONCEALED_KONGS, "清幺九双暗杠");
        put(ALL_TERMINALS << 8 | TWO_MELDED_KONGS, "清幺九双明杠");
        put(LITTLE_FOUR_WINDS << 8 | ALL_HONORS, "小四喜字一色");
        put(LITTLE_FOUR_WINDS << 8 | FOUR_CONCEALED_PUNGS, "小四喜四暗");
        put(LITTLE_FOUR_WINDS << 8 | THREE_KONGS, "小四喜三杠");
        put(LITTLE_FOUR_WINDS << 8 | ALL_TERMINALS_AND_HONORS, "小四喜混幺九");
        put(LITTLE_FOUR_WINDS << 8 | LAST_TILE_DRAW, "妙手小四喜");
        put(LITTLE_FOUR_WINDS << 8 | LAST_TILE_CLAIM, "海底小四喜");
        put(LITTLE_FOUR_WINDS << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开小四喜");
        put(LITTLE_FOUR_WINDS << 8 | ROBBING_THE_KONG, "小四喜抢杠");
        put(LITTLE_FOUR_WINDS << 8 | ALL_PUNGS, "小四喜碰碰和");
        put(LITTLE_FOUR_WINDS << 8 | HALF_FLUSH, "混一色");
        put(LITTLE_THREE_DRAGONS << 8 | ALL_HONORS, "小三元字一色");
        put(LITTLE_THREE_DRAGONS << 8 | FOUR_CONCEALED_PUNGS, "小三元四暗");
        put(LITTLE_THREE_DRAGONS << 8 | THREE_KONGS, "小三元三杠");
        put(LITTLE_THREE_DRAGONS << 8 | ALL_TERMINALS_AND_HONORS, "小三元混幺九");
        put(LITTLE_THREE_DRAGONS << 8 | THREE_CONCEALED_PUNGS, "小三元三暗");
        put(LITTLE_THREE_DRAGONS << 8 | LAST_TILE_DRAW, "妙手小三元");
        put(LITTLE_THREE_DRAGONS << 8 | LAST_TILE_CLAIM, "海底小三元");
        put(LITTLE_THREE_DRAGONS << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开小三元");
        put(LITTLE_THREE_DRAGONS << 8 | ROBBING_THE_KONG, "小三元抢杠");
        put(LITTLE_THREE_DRAGONS << 8 | ALL_PUNGS, "小三元碰碰和");
        put(LITTLE_THREE_DRAGONS << 8 | HALF_FLUSH, "小三元混一色");
        put(LITTLE_THREE_DRAGONS << 8 | MELDED_HAND, "小三元全求人");
        put(LITTLE_THREE_DRAGONS << 8 | TWO_CONCEALED_KONGS, "小三元双暗杠");
        put(LITTLE_THREE_DRAGONS << 8 | OUTSIDE_HAND, "小三元全带幺");
        put(LITTLE_THREE_DRAGONS << 8 | FULLY_CONCEALED_HAND, "小三元不求人");
        put(LITTLE_THREE_DRAGONS << 8 | TWO_MELDED_KONGS, "小三元双明杠");
        put(LITTLE_THREE_DRAGONS << 8 | LAST_TILE, "小三元绝张");
        put(ALL_HONORS << 8 | FOUR_CONCEALED_PUNGS, "字一色四暗");
        put(ALL_HONORS << 8 | THREE_KONGS, "字一色三杠");
        put(ALL_HONORS << 8 | SEVEN_PAIRS, "字一色七对");
        put(ALL_HONORS << 8 | THREE_CONCEALED_PUNGS, "字一色三暗");
        put(ALL_HONORS << 8 | BIG_THREE_WINDS, "字一色三风");
        put(ALL_HONORS << 8 | LAST_TILE_DRAW, "妙手字一色");
        put(ALL_HONORS << 8 | LAST_TILE_CLAIM, "海底字一色");
        put(ALL_HONORS << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开字一色");
        put(ALL_HONORS << 8 | MELDED_HAND, "字一色全求人");
        put(ALL_HONORS << 8 | TWO_CONCEALED_KONGS, "字一色双暗杠");
        put(ALL_HONORS << 8 | TWO_DRAGONS_PUNGS, "字一色双箭刻");
        put(ALL_HONORS << 8 | TWO_MELDED_KONGS, "字一色双明杠");
        put(FOUR_CONCEALED_PUNGS << 8 | FOUR_PURE_SHIFTED_PUNGS, "四节四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | THREE_KONGS, "三杠四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | ALL_TERMINALS_AND_HONORS, "混幺九四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | ALL_EVEN_PUNGS, "全双四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | FULL_FLUSH, "清一色四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | UPPER_TILES, "全大四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | MIDDLE_TILES, "全中四暗刻");
        put(FOUR_CONCEALED_PUNGS << 8 | LOWER_TILES, "全小四暗刻");
        put(FOUR_CONCEALED_PUNGS << 8 | TRIPLE_PUNG, "三同刻四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | UPPER_FOUR, "大于五四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | LOWER_FOUR, "小于五四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | BIG_THREE_WINDS, "三风四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | REVERSIBLE_TILES, "推不倒四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | LAST_TILE_DRAW, "妙手四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | LAST_TILE_CLAIM, "海底四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | HALF_FLUSH, "混一色四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | ALL_TYPES, "五门四暗");
        put(FOUR_CONCEALED_PUNGS << 8 | TWO_CONCEALED_KONGS, "四暗双暗杠");
        put(FOUR_CONCEALED_PUNGS << 8 | TWO_DRAGONS_PUNGS, "四暗双箭");
        put(QUADRUPLE_CHOW << 8 | FULL_FLUSH, "清一色四同顺");
        put(QUADRUPLE_CHOW << 8 | HALF_FLUSH, "混一色四同顺");
        put(FOUR_PURE_SHIFTED_PUNGS << 8 | FULL_FLUSH, "清一色四节");
        put(FOUR_PURE_SHIFTED_PUNGS << 8 | HALF_FLUSH, "混一色四节");
        put(FOUR_PURE_SHIFTED_CHOWS_1 << 8 | FULL_FLUSH, "清一色四步①");
        put(FOUR_PURE_SHIFTED_CHOWS_1 << 8 | HALF_FLUSH, "混一色四步①");
        put(FOUR_PURE_SHIFTED_CHOWS_2 << 8 | FULL_FLUSH, "清一色四步②");
        put(FOUR_PURE_SHIFTED_CHOWS_2 << 8 | HALF_FLUSH, "混一色四步②");
        put(THREE_KONGS << 8 | ALL_TERMINALS_AND_HONORS, "混幺九三杠");
        put(THREE_KONGS << 8 | ALL_EVEN_PUNGS, "全双三杠");
        put(THREE_KONGS << 8 | FULL_FLUSH, "清一色三杠");
        put(THREE_KONGS << 8 | UPPER_TILES, "全大三杠");
        put(THREE_KONGS << 8 | MIDDLE_TILES, "全中三杠");
        put(THREE_KONGS << 8 | LOWER_TILES, "全小三杠");
        put(THREE_KONGS << 8 | TRIPLE_PUNG, "三杠三同刻");
        put(THREE_KONGS << 8 | THREE_CONCEALED_PUNGS, "三杠三暗");
        put(THREE_KONGS << 8 | UPPER_FOUR, "大于五三杠");
        put(THREE_KONGS << 8 | LOWER_FOUR, "小于五三杠");
        put(THREE_KONGS << 8 | BIG_THREE_WINDS, "三风三杠");
        put(THREE_KONGS << 8 | REVERSIBLE_TILES, "推不倒三杠");
        put(THREE_KONGS << 8 | LAST_TILE_DRAW, "妙手三杠");
        put(THREE_KONGS << 8 | LAST_TILE_CLAIM, "海底三杠");
        put(THREE_KONGS << 8 | OUT_WITH_REPLACEMENT_TILE, "三杠杠开");
        put(THREE_KONGS << 8 | ROBBING_THE_KONG, "三杠抢杠");
        put(THREE_KONGS << 8 | ALL_PUNGS, "三杠碰碰和");
        put(THREE_KONGS << 8 | HALF_FLUSH, "混一色三杠");
        put(THREE_KONGS << 8 | ALL_TYPES, "五门三杠");
        put(THREE_KONGS << 8 | MELDED_HAND, "三杠全求人");
        put(THREE_KONGS << 8 | TWO_DRAGONS_PUNGS, "三杠双箭");
        put(THREE_KONGS << 8 | OUTSIDE_HAND, "三杠全带幺");
        put(THREE_KONGS << 8 | LAST_TILE, "三杠绝张");
        put(ALL_TERMINALS_AND_HONORS << 8 | SEVEN_PAIRS, "混幺九七对");
        put(ALL_TERMINALS_AND_HONORS << 8 | TRIPLE_PUNG, "混幺九三同刻");
        put(ALL_TERMINALS_AND_HONORS << 8 | THREE_CONCEALED_PUNGS, "混幺九三暗");
        put(ALL_TERMINALS_AND_HONORS << 8 | BIG_THREE_WINDS, "混幺九三风");
        put(ALL_TERMINALS_AND_HONORS << 8 | LAST_TILE_DRAW, "妙手混幺九");
        put(ALL_TERMINALS_AND_HONORS << 8 | LAST_TILE_CLAIM, "海底混幺九");
        put(ALL_TERMINALS_AND_HONORS << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开混幺九");
        put(ALL_TERMINALS_AND_HONORS << 8 | HALF_FLUSH, "混幺九混一色");
        put(ALL_TERMINALS_AND_HONORS << 8 | ALL_TYPES, "五门混幺九");
        put(ALL_TERMINALS_AND_HONORS << 8 | MELDED_HAND, "混幺九全求人");
        put(ALL_TERMINALS_AND_HONORS << 8 | TWO_CONCEALED_KONGS, "混幺九双暗杠");
        put(ALL_TERMINALS_AND_HONORS << 8 | TWO_DRAGONS_PUNGS, "混幺九双箭刻");
        put(ALL_TERMINALS_AND_HONORS << 8 | TWO_MELDED_KONGS, "混幺九双明杠");
        put(SEVEN_PAIRS << 8 | FULL_FLUSH, "清一色七对");
        put(SEVEN_PAIRS << 8 | UPPER_TILES, "全大七对");
        put(SEVEN_PAIRS << 8 | MIDDLE_TILES, "全中七对");
        put(SEVEN_PAIRS << 8 | LOWER_TILES, "全小七对");
        put(SEVEN_PAIRS << 8 | UPPER_FOUR, "大于五七对");
        put(SEVEN_PAIRS << 8 | LOWER_FOUR, "小于五七对");
        put(SEVEN_PAIRS << 8 | REVERSIBLE_TILES, "推不倒七对");
        put(SEVEN_PAIRS << 8 | LAST_TILE_DRAW, "妙手七对");
        put(SEVEN_PAIRS << 8 | LAST_TILE_CLAIM, "海底七对");
        put(SEVEN_PAIRS << 8 | HALF_FLUSH, "混一色七对");
        put(SEVEN_PAIRS << 8 | ALL_TYPES, "五门七对");
        put(GREATER_HONORS_AND_KNITTED_TILES << 8 | LAST_TILE_DRAW, "妙手七星");
        put(GREATER_HONORS_AND_KNITTED_TILES << 8 | LAST_TILE_CLAIM, "海底七星");
        put(GREATER_HONORS_AND_KNITTED_TILES << 8 | ROBBING_THE_KONG, "七星抢杠");
        put(GREATER_HONORS_AND_KNITTED_TILES << 8 | LAST_TILE, "七星绝张");
        put(ALL_EVEN_PUNGS << 8 | MIDDLE_TILES, "全中全双刻");
        put(ALL_EVEN_PUNGS << 8 | TRIPLE_PUNG, "全双三同刻");
        put(ALL_EVEN_PUNGS << 8 | THREE_CONCEALED_PUNGS, "全双刻三暗");
        put(ALL_EVEN_PUNGS << 8 | REVERSIBLE_TILES, "全双刻推不倒");
        put(ALL_EVEN_PUNGS << 8 | LAST_TILE_DRAW, "妙手全双刻");
        put(ALL_EVEN_PUNGS << 8 | LAST_TILE_CLAIM, "海底全双刻");
        put(ALL_EVEN_PUNGS << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开全双刻");
        put(ALL_EVEN_PUNGS << 8 | MELDED_HAND, "全双刻全求人");
        put(ALL_EVEN_PUNGS << 8 | TWO_CONCEALED_KONGS, "全双刻双暗杠");
        put(ALL_EVEN_PUNGS << 8 | TWO_MELDED_KONGS, "全双刻双明杠");
        put(FULL_FLUSH << 8 | PURE_TRIPLE_CHOW, "清一色三同顺");
        put(FULL_FLUSH << 8 | PURE_SHIFTED_PUNGS, "清一色三节高");
        put(FULL_FLUSH << 8 | PURE_SHIFTED_CHOWS_1, "清一色三步①");
        put(FULL_FLUSH << 8 | PURE_SHIFTED_CHOWS_2, "清一色三步②");
        put(FULL_FLUSH << 8 | THREE_CONCEALED_PUNGS, "清一色三暗");
        put(FULL_FLUSH << 8 | UPPER_FOUR, "清一色大于五");
        put(FULL_FLUSH << 8 | LOWER_FOUR, "清一色小于五");
        put(FULL_FLUSH << 8 | REVERSIBLE_TILES, "清一色推不倒");
        put(FULL_FLUSH << 8 | LAST_TILE_DRAW, "妙手清一色");
        put(FULL_FLUSH << 8 | LAST_TILE_CLAIM, "海底清一色");
        put(FULL_FLUSH << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开清一色");
        put(FULL_FLUSH << 8 | ROBBING_THE_KONG, "清一色抢杠");
        put(FULL_FLUSH << 8 | ALL_PUNGS, "清一色碰碰和");
        put(FULL_FLUSH << 8 | MELDED_HAND, "清一色全求人");
        put(FULL_FLUSH << 8 | TWO_CONCEALED_KONGS, "清一色双暗杠");
        put(FULL_FLUSH << 8 | FULLY_CONCEALED_HAND, "清一色不求人");
        put(FULL_FLUSH << 8 | TWO_MELDED_KONGS, "清一色双明杠");
        put(FULL_FLUSH << 8 | LAST_TILE, "清一色绝张");
        put(PURE_TRIPLE_CHOW << 8 | HALF_FLUSH, "混一色三同顺");
        put(PURE_SHIFTED_PUNGS << 8 | HALF_FLUSH, "混一色三节");
        put(UPPER_TILES << 8 | TRIPLE_PUNG, "全大三同刻");
        put(UPPER_TILES << 8 | THREE_CONCEALED_PUNGS, "全大三暗");
        put(UPPER_TILES << 8 | LAST_TILE_DRAW, "妙手全大");
        put(UPPER_TILES << 8 | LAST_TILE_CLAIM, "海底全大");
        put(UPPER_TILES << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开全大");
        put(UPPER_TILES << 8 | ROBBING_THE_KONG, "全大抢杠");
        put(UPPER_TILES << 8 | ALL_PUNGS, "全大碰碰和");
        put(UPPER_TILES << 8 | MELDED_HAND, "全大全求人");
        put(UPPER_TILES << 8 | TWO_CONCEALED_KONGS, "全大双暗杠");
        put(UPPER_TILES << 8 | OUTSIDE_HAND, "全大全带幺");
        put(UPPER_TILES << 8 | FULLY_CONCEALED_HAND, "全大不求人");
        put(UPPER_TILES << 8 | TWO_MELDED_KONGS, "全大双明杠");
        put(UPPER_TILES << 8 | LAST_TILE, "全大绝张");
        put(MIDDLE_TILES << 8 | ALL_FIVE, "全中全带五");
        put(MIDDLE_TILES << 8 | TRIPLE_PUNG, "全中三同刻");
        put(MIDDLE_TILES << 8 | THREE_CONCEALED_PUNGS, "全中三暗");
        put(MIDDLE_TILES << 8 | REVERSIBLE_TILES, "全中推不倒");
        put(MIDDLE_TILES << 8 | LAST_TILE_DRAW, "妙手全中");
        put(MIDDLE_TILES << 8 | LAST_TILE_CLAIM, "海底全中");
        put(MIDDLE_TILES << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开全中");
        put(MIDDLE_TILES << 8 | ROBBING_THE_KONG, "全中抢杠");
        put(MIDDLE_TILES << 8 | ALL_PUNGS, "全中碰碰和");
        put(MIDDLE_TILES << 8 | MELDED_HAND, "全中全求人");
        put(MIDDLE_TILES << 8 | TWO_CONCEALED_KONGS, "全中双暗杠");
        put(MIDDLE_TILES << 8 | FULLY_CONCEALED_HAND, "全中不求人");
        put(MIDDLE_TILES << 8 | TWO_MELDED_KONGS, "全中双明杠");
        put(MIDDLE_TILES << 8 | LAST_TILE, "全中绝张");
        put(LOWER_TILES << 8 | TRIPLE_PUNG, "全小三同刻");
        put(LOWER_TILES << 8 | THREE_CONCEALED_PUNGS, "全小三暗刻");
        put(LOWER_TILES << 8 | LAST_TILE_DRAW, "妙手全小");
        put(LOWER_TILES << 8 | LAST_TILE_CLAIM, "海底全小");
        put(LOWER_TILES << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开全小");
        put(LOWER_TILES << 8 | ROBBING_THE_KONG, "全小抢杠");
        put(LOWER_TILES << 8 | ALL_PUNGS, "全小碰碰和");
        put(LOWER_TILES << 8 | MELDED_HAND, "全小全求人");
        put(LOWER_TILES << 8 | TWO_CONCEALED_KONGS, "全小双暗杠");
        put(LOWER_TILES << 8 | OUTSIDE_HAND, "全小全带幺");
        put(LOWER_TILES << 8 | FULLY_CONCEALED_HAND, "全小不求人");
        put(LOWER_TILES << 8 | TWO_MELDED_KONGS, "全小双明杠");
        put(LOWER_TILES << 8 | LAST_TILE, "全小绝张");
        put(PURE_STRAIGHT << 8 | LAST_TILE_DRAW, "妙手清龙");
        put(PURE_STRAIGHT << 8 | LAST_TILE_CLAIM, "海底清龙");
        put(PURE_STRAIGHT << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开清龙");
        put(PURE_STRAIGHT << 8 | ROBBING_THE_KONG, "清龙抢杠");
        put(PURE_STRAIGHT << 8 | HALF_FLUSH, "混一色清龙");
        put(PURE_STRAIGHT << 8 | MELDED_HAND, "清龙全求人");
        put(PURE_STRAIGHT << 8 | FULLY_CONCEALED_HAND, "清龙不求人");
        put(PURE_STRAIGHT << 8 | LAST_TILE, "清龙绝张");
        put(PURE_SHIFTED_CHOWS_1 << 8 | HALF_FLUSH, "混一色三步①");
        put(PURE_SHIFTED_CHOWS_2 << 8 | HALF_FLUSH, "混一色三步②");
        put(TRIPLE_PUNG << 8 | THREE_CONCEALED_PUNGS, "三同刻三暗");
        put(TRIPLE_PUNG << 8 | UPPER_FOUR, "大于五三同刻");
        put(TRIPLE_PUNG << 8 | LOWER_FOUR, "小于五三同刻");
        put(TRIPLE_PUNG << 8 | LAST_TILE_DRAW, "妙手三同刻");
        put(TRIPLE_PUNG << 8 | LAST_TILE_CLAIM, "海底三同刻");
        put(TRIPLE_PUNG << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开三同刻");
        put(TRIPLE_PUNG << 8 | ROBBING_THE_KONG, "三同刻抢杠");
        put(TRIPLE_PUNG << 8 | ALL_PUNGS, "三同刻碰碰和");
        put(TRIPLE_PUNG << 8 | ALL_TYPES, "五门三同刻");
        put(TRIPLE_PUNG << 8 | MELDED_HAND, "三同刻全求人");
        put(TRIPLE_PUNG << 8 | TWO_CONCEALED_KONGS, "三同刻双暗杠");
        put(TRIPLE_PUNG << 8 | OUTSIDE_HAND, "三同刻全带幺");
        put(TRIPLE_PUNG << 8 | TWO_MELDED_KONGS, "三同刻双明杠");
        put(TRIPLE_PUNG << 8 | LAST_TILE, "三同刻绝张");
        put(THREE_CONCEALED_PUNGS << 8 | UPPER_FOUR, "大于五三暗");
        put(THREE_CONCEALED_PUNGS << 8 | LOWER_FOUR, "小于五三暗");
        put(THREE_CONCEALED_PUNGS << 8 | BIG_THREE_WINDS, "三风三暗");
        put(THREE_CONCEALED_PUNGS << 8 | REVERSIBLE_TILES, "推不倒三暗");
        put(THREE_CONCEALED_PUNGS << 8 | LAST_TILE_DRAW, "妙手三暗");
        put(THREE_CONCEALED_PUNGS << 8 | LAST_TILE_CLAIM, "海底三暗");
        put(THREE_CONCEALED_PUNGS << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开三暗");
        put(THREE_CONCEALED_PUNGS << 8 | ROBBING_THE_KONG, "三暗抢杠");
        put(THREE_CONCEALED_PUNGS << 8 | ALL_PUNGS, "碰碰和三暗");
        put(THREE_CONCEALED_PUNGS << 8 | HALF_FLUSH, "混一色三暗");
        put(THREE_CONCEALED_PUNGS << 8 | ALL_TYPES, "五门三暗");
        put(THREE_CONCEALED_PUNGS << 8 | TWO_CONCEALED_KONGS, "三暗双暗杠");
        put(THREE_CONCEALED_PUNGS << 8 | TWO_DRAGONS_PUNGS, "三暗双箭");
        put(THREE_CONCEALED_PUNGS << 8 | OUTSIDE_HAND, "三暗全带幺");
        put(THREE_CONCEALED_PUNGS << 8 | LAST_TILE, "三暗绝张");
        put(LESSER_HONORS_AND_KNITTED_TILES << 8 | KNITTED_STRAIGHT, "全不靠组合龙");
        put(LESSER_HONORS_AND_KNITTED_TILES << 8 | LAST_TILE_DRAW, "妙手全不靠");
        put(LESSER_HONORS_AND_KNITTED_TILES << 8 | LAST_TILE_CLAIM, "海底全不靠");
        put(LESSER_HONORS_AND_KNITTED_TILES << 8 | ROBBING_THE_KONG, "全不靠抢杠");
        put(LESSER_HONORS_AND_KNITTED_TILES << 8 | LAST_TILE, "全不靠绝张");
        put(KNITTED_STRAIGHT << 8 | LAST_TILE_DRAW, "妙手组合龙");
        put(KNITTED_STRAIGHT << 8 | LAST_TILE_CLAIM, "海底组合龙");
        put(KNITTED_STRAIGHT << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开组合龙");
        put(KNITTED_STRAIGHT << 8 | ROBBING_THE_KONG, "组合龙抢杠");
        put(KNITTED_STRAIGHT << 8 | ALL_TYPES, "五门组合龙");
        put(KNITTED_STRAIGHT << 8 | FULLY_CONCEALED_HAND, "组合龙不求人");
        put(KNITTED_STRAIGHT << 8 | LAST_TILE, "组合龙绝张");
        put(UPPER_FOUR << 8 | REVERSIBLE_TILES, "大于五推不倒");
        put(UPPER_FOUR << 8 | LAST_TILE_DRAW, "妙手大于五");
        put(UPPER_FOUR << 8 | LAST_TILE_CLAIM, "海底大于五");
        put(UPPER_FOUR << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开大于五");
        put(UPPER_FOUR << 8 | ROBBING_THE_KONG, "大于五抢杠");
        put(UPPER_FOUR << 8 | ALL_PUNGS, "大于五碰碰和");
        put(UPPER_FOUR << 8 | MELDED_HAND, "大于五全求人");
        put(UPPER_FOUR << 8 | TWO_CONCEALED_KONGS, "大于五双暗杠");
        put(UPPER_FOUR << 8 | FULLY_CONCEALED_HAND, "大于五不求人");
        put(UPPER_FOUR << 8 | TWO_MELDED_KONGS, "大于五双明杠");
        put(UPPER_FOUR << 8 | LAST_TILE, "大于五绝张");
        put(LOWER_FOUR << 8 | REVERSIBLE_TILES, "小于五推不倒");
        put(LOWER_FOUR << 8 | LAST_TILE_DRAW, "妙手小于五");
        put(LOWER_FOUR << 8 | LAST_TILE_CLAIM, "海底小于五");
        put(LOWER_FOUR << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开小于五");
        put(LOWER_FOUR << 8 | ROBBING_THE_KONG, "小于五抢杠");
        put(LOWER_FOUR << 8 | ALL_PUNGS, "小于五碰碰和");
        put(LOWER_FOUR << 8 | MELDED_HAND, "小于五全求人");
        put(LOWER_FOUR << 8 | TWO_CONCEALED_KONGS, "小于五双暗杠");
        put(LOWER_FOUR << 8 | FULLY_CONCEALED_HAND, "小于五不求人");
        put(LOWER_FOUR << 8 | TWO_MELDED_KONGS, "小于五双明杠");
        put(LOWER_FOUR << 8 | LAST_TILE, "小于五绝张");
        put(BIG_THREE_WINDS << 8 | LAST_TILE_DRAW, "妙手三风刻");
        put(BIG_THREE_WINDS << 8 | LAST_TILE_CLAIM, "海底三风刻");
        put(BIG_THREE_WINDS << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开三风刻");
        put(BIG_THREE_WINDS << 8 | ROBBING_THE_KONG, "三风刻抢杠");
        put(BIG_THREE_WINDS << 8 | ALL_PUNGS, "三风刻碰碰和");
        put(BIG_THREE_WINDS << 8 | HALF_FLUSH, "三风刻混一色");
        put(BIG_THREE_WINDS << 8 | MELDED_HAND, "三风刻全求人");
        put(BIG_THREE_WINDS << 8 | TWO_CONCEALED_KONGS, "三风刻双暗杠");
        put(BIG_THREE_WINDS << 8 | OUTSIDE_HAND, "三风刻全带幺");
        put(BIG_THREE_WINDS << 8 | TWO_MELDED_KONGS, "三风刻双明杠");
        put(BIG_THREE_WINDS << 8 | LAST_TILE, "三风刻绝张");
        put(MIXED_STRAIGHT << 8 | LAST_TILE_DRAW, "妙手花龙");
        put(MIXED_STRAIGHT << 8 | LAST_TILE_CLAIM, "海底花龙");
        put(MIXED_STRAIGHT << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开花龙");
        put(MIXED_STRAIGHT << 8 | ROBBING_THE_KONG, "花龙抢杠");
        put(MIXED_STRAIGHT << 8 | ALL_TYPES, "五门花龙");
        put(MIXED_STRAIGHT << 8 | MELDED_HAND, "花龙全求人");
        put(MIXED_STRAIGHT << 8 | FULLY_CONCEALED_HAND, "花龙不求人");
        put(MIXED_STRAIGHT << 8 | LAST_TILE, "花龙绝张");
        put(REVERSIBLE_TILES << 8 | LAST_TILE_DRAW, "妙手推不倒");
        put(REVERSIBLE_TILES << 8 | LAST_TILE_CLAIM, "海底推不倒");
        put(REVERSIBLE_TILES << 8 | OUT_WITH_REPLACEMENT_TILE, "杠开推不倒");
        put(REVERSIBLE_TILES << 8 | ROBBING_THE_KONG, "推不倒抢杠");
        put(REVERSIBLE_TILES << 8 | ALL_PUNGS, "推不倒碰碰和");
        put(REVERSIBLE_TILES << 8 | HALF_FLUSH, "混一色推不倒");
        put(REVERSIBLE_TILES << 8 | MELDED_HAND, "推不倒全求人");
        put(REVERSIBLE_TILES << 8 | TWO_CONCEALED_KONGS, "推不倒双暗杠");
        put(REVERSIBLE_TILES << 8 | FULLY_CONCEALED_HAND, "推不倒不求人");
        put(REVERSIBLE_TILES << 8 | TWO_MELDED_KONGS, "推不倒双明杠");
        put(REVERSIBLE_TILES << 8 | LAST_TILE, "推不倒绝张");
        put(MIXED_TRIPLE_CHOW << 8 | ALL_TYPES, "五门三同顺");
        put(MIXED_SHIFTED_PUNGS << 8 | ALL_TYPES, "五门三节");
        put(LAST_TILE_DRAW << 8 | OUT_WITH_REPLACEMENT_TILE, "妙手杠开");
        put(LAST_TILE_DRAW << 8 | ALL_PUNGS, "妙手碰碰和");
        put(LAST_TILE_DRAW << 8 | HALF_FLUSH, "妙手混一色");
        put(LAST_TILE_DRAW << 8 | ALL_TYPES, "妙手五门齐");
        put(LAST_TILE_DRAW << 8 | TWO_CONCEALED_KONGS, "妙手双暗杠");
        put(LAST_TILE_DRAW << 8 | TWO_DRAGONS_PUNGS, "妙手双箭刻");
        put(LAST_TILE_DRAW << 8 | OUTSIDE_HAND, "妙手全带幺");
        put(LAST_TILE_DRAW << 8 | FULLY_CONCEALED_HAND, "妙手不求人");
        put(LAST_TILE_DRAW << 8 | TWO_MELDED_KONGS, "妙手双明杠");
        put(LAST_TILE_DRAW << 8 | LAST_TILE, "妙手绝张");
        put(LAST_TILE_CLAIM << 8 | ALL_PUNGS, "海底碰碰和");
        put(LAST_TILE_CLAIM << 8 | HALF_FLUSH, "海底混一色");
        put(LAST_TILE_CLAIM << 8 | ALL_TYPES, "海底五门齐");
        put(LAST_TILE_CLAIM << 8 | MELDED_HAND, "海底全求人");
        put(LAST_TILE_CLAIM << 8 | TWO_CONCEALED_KONGS, "海底双暗杠");
        put(LAST_TILE_CLAIM << 8 | TWO_DRAGONS_PUNGS, "海底双箭刻");
        put(LAST_TILE_CLAIM << 8 | OUTSIDE_HAND, "海底全带幺");
        put(LAST_TILE_CLAIM << 8 | TWO_MELDED_KONGS, "海底双明杠");
        put(LAST_TILE_CLAIM << 8 | LAST_TILE, "海底绝张");
        put(OUT_WITH_REPLACEMENT_TILE << 8 | ALL_PUNGS, "杠开碰碰和");
        put(OUT_WITH_REPLACEMENT_TILE << 8 | HALF_FLUSH, "杠开混一色");
        put(OUT_WITH_REPLACEMENT_TILE << 8 | ALL_TYPES, "杠开五门齐");
        put(OUT_WITH_REPLACEMENT_TILE << 8 | TWO_CONCEALED_KONGS, "杠开双暗杠");
        put(OUT_WITH_REPLACEMENT_TILE << 8 | TWO_DRAGONS_PUNGS, "杠开双箭刻");
        put(OUT_WITH_REPLACEMENT_TILE << 8 | OUTSIDE_HAND, "杠开全带幺");
        put(OUT_WITH_REPLACEMENT_TILE << 8 | FULLY_CONCEALED_HAND, "杠开不求人");
        put(OUT_WITH_REPLACEMENT_TILE << 8 | TWO_MELDED_KONGS, "杠开双明杠");
        put(OUT_WITH_REPLACEMENT_TILE << 8 | LAST_TILE, "杠开绝张");
        put(ROBBING_THE_KONG << 8 | HALF_FLUSH, "混一色抢杠");
        put(ROBBING_THE_KONG << 8 | ALL_TYPES, "五门齐抢杠");
        put(ROBBING_THE_KONG << 8 | TWO_CONCEALED_KONGS, "双暗杠抢杠");
        put(ROBBING_THE_KONG << 8 | TWO_DRAGONS_PUNGS, "双箭刻抢杠");
        put(ROBBING_THE_KONG << 8 | OUTSIDE_HAND, "全带幺抢杠");
        put(ROBBING_THE_KONG << 8 | TWO_MELDED_KONGS, "双明杠抢杠");
        put(ALL_PUNGS << 8 | HALF_FLUSH, "混一色碰碰和");
        put(ALL_PUNGS << 8 | ALL_TYPES, "五门碰碰和");
        put(ALL_PUNGS << 8 | MELDED_HAND, "全求人碰碰和");
        put(ALL_PUNGS << 8 | TWO_CONCEALED_KONGS, "双暗杠碰碰和");
        put(ALL_PUNGS << 8 | TWO_DRAGONS_PUNGS, "双箭刻碰碰和");
        put(ALL_PUNGS << 8 | TWO_MELDED_KONGS, "双明杠碰碰和");
        put(HALF_FLUSH << 8 | MELDED_HAND, "混一色全求人");
        put(HALF_FLUSH << 8 | TWO_CONCEALED_KONGS, "混一色双暗杠");
        put(HALF_FLUSH << 8 | TWO_DRAGONS_PUNGS, "混一色双箭刻");
        put(HALF_FLUSH << 8 | OUTSIDE_HAND, "混一色全带幺");
        put(HALF_FLUSH << 8 | FULLY_CONCEALED_HAND, "混一色不求人");
        put(HALF_FLUSH << 8 | TWO_MELDED_KONGS, "混一色双明杠");
        put(HALF_FLUSH << 8 | LAST_TILE, "混一色绝张");
        put(MIXED_SHIFTED_CHOWS << 8 | ALL_TYPES, "五门三步");
        put(ALL_TYPES << 8 | MELDED_HAND, "五门全求人");
        put(ALL_TYPES << 8 | TWO_CONCEALED_KONGS, "五门双暗杠");
        put(ALL_TYPES << 8 | OUTSIDE_HAND, "五门全带幺");
        put(ALL_TYPES << 8 | FULLY_CONCEALED_HAND, "五门不求人");
        put(ALL_TYPES << 8 | TWO_MELDED_KONGS, "五门双明杠");
        put(ALL_TYPES << 8 | LAST_TILE, "五门绝张");
        put(MELDED_HAND << 8 | TWO_DRAGONS_PUNGS, "全求人双箭刻");
        put(MELDED_HAND << 8 | OUTSIDE_HAND, "全求人全带幺");
        put(MELDED_HAND << 8 | TWO_MELDED_KONGS, "全求人双明杠");
        put(TWO_CONCEALED_KONGS << 8 | TWO_DRAGONS_PUNGS, "双暗杠双箭刻");
        put(TWO_CONCEALED_KONGS << 8 | OUTSIDE_HAND, "双暗杠全带幺");
        put(TWO_CONCEALED_KONGS << 8 | FULLY_CONCEALED_HAND, "双暗杠不求人");
        put(TWO_CONCEALED_KONGS << 8 | LAST_TILE, "双暗杠绝张");
        put(TWO_DRAGONS_PUNGS << 8 | OUTSIDE_HAND, "双箭刻全带幺");
        put(TWO_DRAGONS_PUNGS << 8 | FULLY_CONCEALED_HAND, "双箭刻不求人");
        put(TWO_DRAGONS_PUNGS << 8 | TWO_MELDED_KONGS, "双箭刻双明杠");
        put(TWO_DRAGONS_PUNGS << 8 | LAST_TILE, "双箭刻绝张");
        put(OUTSIDE_HAND << 8 | FULLY_CONCEALED_HAND, "全带幺不求人");
        put(OUTSIDE_HAND << 8 | TWO_MELDED_KONGS, "全带幺双明杠");
        put(OUTSIDE_HAND << 8 | LAST_TILE, "全带幺绝张");
        put(FULLY_CONCEALED_HAND << 8 | LAST_TILE, "不求人绝张");
        put(TWO_MELDED_KONGS << 8 | LAST_TILE, "双明杠绝张");
    }};

}
