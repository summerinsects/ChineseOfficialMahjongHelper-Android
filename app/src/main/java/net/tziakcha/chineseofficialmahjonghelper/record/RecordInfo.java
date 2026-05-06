package net.tziakcha.chineseofficialmahjonghelper.record;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

public final class RecordInfo {
    public static final int MODE_STANDARD = 0;  // 标准
    public static final int MODE_SPLIT_SELF_DRAWN = 1;  // 自摸平摊
    public static final int MODE_SHOOT_UNDERTAKE = 2;  // 点炮承包
    public static final int MODE_INVOLVED_NO_BASE = 3;  // 牵连免底

    public static final String[] MODE_NAME_TEXT = {
            "标准", "自摸平摊", "点炮承包", "牵连免底"
    };

    public static final class Detail {
        public int win_flag = 0;
        public int claim_flag = 0;
        public int fan = 0;
        public int[] penalty = new int[4];
        public boolean timeout = false;
        public long fan_major = 0;
        public long fan_minor1 = 0;
        public int fan_minor2 = 0;

        public void assign(final Detail detail) {
            win_flag = detail.win_flag;
            claim_flag = detail.claim_flag;
            fan = detail.fan;
            System.arraycopy(detail.penalty, 0, penalty, 0, 4);
            timeout = detail.timeout;
            fan_major = detail.fan_major;
            fan_minor1 = detail.fan_minor1;
            fan_minor2 = detail.fan_minor2;
        }
    }

    public int version = 1;  // 版本号
    public String title;
    public String[] names = new String[4];
    public Detail[] details = new Detail[16];
    public int mode = MODE_STANDARD;
    public int period = 0;  // 当前第几盘
    public long start_time = 0;
    public long finish_time = 0;

    public RecordInfo() {
        for (int i = 0; i < 4; ++i) {
            names[i] = "";
        }
        for (int i = 0; i < 16; ++i) {
            details[i] = new Detail();
        }
    }

    public void assign(final RecordInfo record) {
        version = record.version;
        title = record.title;
        System.arraycopy(record.names, 0, names, 0, 4);
        for (int i = 0; i < 16; ++i) {
            details[i].assign(record.details[i]);
        }
        mode = record.mode;
        period = record.period;
        start_time = record.start_time;
        finish_time = record.finish_time;
    }

    public static int[] translateToScores(int mode, int fan, int winFlag, int claimFlag, int[] penalty) {
        int[] res = new int[4];
        if (fan >= 8 && winFlag != 0 && claimFlag != 0) {
            int winIndex = -1;
            int claimIndex = -1;
            for (int i = 0; i < 4; ++i) {
                if (((1 << i) & winFlag) != 0) {
                    winIndex = i;
                }
                if (((1 << i) & claimFlag) != 0) {
                    claimIndex = i;
                }
            }

            switch (mode) {
                default:
                    if (winIndex != claimIndex) {  // 点炮
                        for (int i = 0; i < 4; ++i) {
                            res[i] = (i == winIndex) ? (fan + 24) : (i == claimIndex ? (-8 - fan) : -8);
                        }
                    } else {  // 自摸
                        for (int i = 0; i < 4; ++i) {
                            res[i] = (i == winIndex) ? (fan + 8) * 3 : (-8 - fan);
                        }
                    }
                    break;
                case MODE_SPLIT_SELF_DRAWN:
                    if (winIndex != claimIndex) {  // 点炮
                        for (int i = 0; i < 4; ++i) {
                            res[i] = (i == winIndex) ? (fan + 24) : (i == claimIndex ? (-8 - fan) : -8);
                        }
                    } else {
                        // 自摸平摊，每人支付(fan/3)向上取整+8
                        int fan1 = fan / 3 + (fan % 3 != 0 ? 1 : 0);
                        for (int i = 0; i < 4; ++i) {
                            res[i] = (i == winIndex) ? (fan1 + 8) * 3 : (-8 - fan1);
                        }
                    }
                    break;
                case MODE_SHOOT_UNDERTAKE:
                    if (winIndex != claimIndex) {  // 点炮
                        // 点炮承包，点炮者支付fan*3+8
                        for (int i = 0; i < 4; ++i) {
                            res[i] = (i == winIndex) ? (fan * 3 + 24) : (i == claimIndex ? (-8 - fan * 3) : -8);
                        }
                    } else {  // 自摸
                        for (int i = 0; i < 4; ++i) {
                            res[i] = (i == winIndex) ? (fan + 8) * 3 : (-8 - fan);
                        }
                    }
                    break;
                case MODE_INVOLVED_NO_BASE:
                    if (winIndex != claimIndex) {  // 点炮
                        // 点炮者不给底分
                        for (int i = 0; i < 4; ++i) {
                            res[i] = (i == winIndex) ? (fan + 16) : (i == claimIndex ? -fan : -8);
                        }
                    } else {
                        // 自摸不给底分
                        for (int i = 0; i < 4; ++i) {
                            res[i] = (i == winIndex) ? fan * 3 : -fan;
                        }
                    }
                    break;
            }
        }

        // 加上处罚
        for (int i = 0; i < 4; ++i) {
            res[i] += penalty[i];
        }

        return res;
    }

    public static int[] calcRankFromScore(int[] scores) {
        int[] ranks = {0, 0, 0, 0};
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                if (i == j) continue;
                if (scores[i] < scores[j]) ++ranks[i];
                //if (scores[i] == scores[j] && i > j) ++ranks[i];  // 这一行的作用是取消并列
            }
        }
        return ranks;
    }

    private static final int[] NORM_TABLE = {48, 24, 12, 0};

    public static int[] calcNorm12FromRank(int[] ranks) {
        int[] norms = {0, 0, 0, 0};

        // 并列的数目
        int[] rankCnt = {0, 0, 0, 0};
        for (int i = 0; i < 4; ++i) {
            ++rankCnt[ranks[i]];
        }

        for (int i = 0; i < 4; ++i) {
            int rank = ranks[i];
            int tieCnt = rankCnt[rank];  // 并列的人数

            // 累加并列的标准分
            int ss0 = NORM_TABLE[rank];
            for (int n = 1; n < tieCnt; ++n) {
                ss0 += NORM_TABLE[rank + n];
            }
            ss0 /= tieCnt;
            norms[i] = ss0;
        }

        return norms;
    }

    private static final String[] FRACTION_TEXT = {
            "", "\u00B9\u2044\u2081\u2082", "\u2159", "\u00bc",
            "\u2153", "\u2075\u2044\u2081\u2082", "\u00bd", "\u2077\u2044\u2081\u2082",
            "\u2154", "\u00be", "\u215a", "\u00B9\u00B9\u2044\u2081\u2082"
    };

    public static String normStringFrom12(int norm) {
        int r = norm % 12;
        int q = (norm - r) / 12;
        return norm > 0 ? (q > 0 ? q : "") + FRACTION_TEXT[r] : "0";
    }

    private static void detailToJSON(JSONStringer stringer, final RecordInfo.Detail detail) throws JSONException {
        stringer.object();
        stringer.key("win_flag").value(detail.win_flag);
        stringer.key("win_flag").value(detail.win_flag);
        stringer.key("claim_flag").value(detail.claim_flag);
        stringer.key("fan").value(detail.fan);
        stringer.key("timeout").value(detail.timeout);
        stringer.key("fan_major").value(detail.fan_major);
        stringer.key("fan_minor1").value(detail.fan_minor1);
        stringer.key("fan_minor2").value(detail.fan_minor2);
        stringer.key("penalty").array();
        for (int k = 0; k < 4; ++k) {
            stringer.value(detail.penalty[k]);
        }
        stringer.endArray();
        stringer.endObject();
    }

    private static void recordToJSON(JSONStringer stringer, final RecordInfo record) throws JSONException {
        stringer.object();
        stringer.key("version").value(record.version);
        if (record.title != null && !record.title.isEmpty()) {
            stringer.key("title").value(record.title);
        }
        stringer.key("mode").value(record.mode);
        stringer.key("start_time").value(record.start_time);
        stringer.key("finish_time").value(record.finish_time);

        stringer.key("name").array();
        for (int i = 0; i < 4; ++i) {
            stringer.value(record.names[i]);
        }
        stringer.endArray();

        stringer.key("detail").array();
        for (int i = 0, cnt = record.period; i < cnt; ++i) {
            detailToJSON(stringer, record.details[i]);
        }
        stringer.endArray();
        stringer.endObject();
    }

    public static String recordToString(final RecordInfo record) {
        try {
            JSONStringer stringer = new JSONStringer();
            recordToJSON(stringer, record);
            return stringer.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void parseRecord(final JSONObject obj, RecordInfo record) throws JSONException {
        record.version = obj.getInt("version");
        if (obj.has("title")) {
            String str1 = obj.getString("title");
            if (!str1.isEmpty()) {
                record.title = str1;
            }
        }
        record.mode = obj.getInt("mode");
        record.start_time = obj.getLong("start_time");
        record.finish_time = obj.getLong("finish_time");

        JSONArray arr = obj.getJSONArray("name");
        if (arr.length() == 4) {
            for (int i = 0; i < 4; ++i) {
                record.names[i] = arr.getString(i);
            }
        }

        arr = obj.getJSONArray("detail");
        int len = arr.length();
        record.period = len;
        for (int i = 0; i < len; ++i) {
            JSONObject obj1 = arr.getJSONObject(i);
            RecordInfo.Detail detail = record.details[i];
            detail.win_flag = obj1.getInt("win_flag");
            detail.claim_flag = obj1.getInt("claim_flag");
            detail.fan = obj1.getInt("fan");
            detail.timeout = obj1.getBoolean("timeout");
            detail.fan_major = obj1.getLong("fan_major");
            detail.fan_minor1 = obj1.getLong("fan_minor1");
            detail.fan_minor2 = obj1.getInt("fan_minor2");
            JSONArray arr1 = obj1.getJSONArray("penalty");
            if (arr1.length() == 4) {
                detail.penalty[0] = arr1.getInt(0);
                detail.penalty[1] = arr1.getInt(1);
                detail.penalty[2] = arr1.getInt(2);
                detail.penalty[3] = arr1.getInt(3);
            }
        }
    }

    private static void parseRecordV0(final JSONObject obj, RecordInfo record) throws JSONException {
        if (obj.has("title")) {
            String str1 = obj.getString("title");
            if (!str1.isEmpty()) {
                record.title = str1;
            }
        }
        record.mode = obj.getInt("mode");
        record.start_time = obj.getLong("start_time") * 1000;
        record.finish_time = obj.getLong("end_time") * 1000;

        JSONArray arr = obj.getJSONArray("name");
        if (arr.length() == 4) {
            for (int i = 0; i < 4; ++i) {
                record.names[i] = arr.getString(i);
            }
        }

        arr = obj.getJSONArray("detail");
        int len = arr.length();
        record.period = len;
        for (int i = 0; i < len; ++i) {
            JSONObject obj1 = arr.getJSONObject(i);
            RecordInfo.Detail detail = record.details[i];
            detail.win_flag = obj1.getInt("win_flag");
            detail.claim_flag = obj1.getInt("claim_flag");
            detail.fan = obj1.getInt("fan");
            detail.timeout = obj1.getBoolean("timeout");
            detail.fan_minor1 = obj1.getLong("fan1_bits");
            detail.fan_minor2 = obj1.getInt("fan2_bits");
            JSONArray arr1 = obj1.getJSONArray("penalty_scores");
            if (arr1.length() == 4) {
                detail.penalty[0] = arr1.getInt(0);
                detail.penalty[1] = arr1.getInt(1);
                detail.penalty[2] = arr1.getInt(2);
                detail.penalty[3] = arr1.getInt(3);
            }

            // 新数据增加了一色三步、四步递增1/2的区分，把旧数据都当递增2的
            // 1. 一色三步高~和绝张，原封不动
            // 2. 一色四步高~三色双龙会，移动1位
            // 3. 大四喜~一色四节高，移动2位
            long bits = obj1.getLong("fan_bits");
            detail.fan_major = (bits & 0x1fffffffL)
                    | ((bits & 0x7ffe0000000L) << 1)
                    | ((bits & 0x3fff80000000000L) << 2);
        }

        // 转换完成后version改为1，使下次保存成新格式
        record.version = 1;
    }

    public static boolean parseRecordV0(final String str, RecordInfo record) {
        try {
            parseRecordV0(new JSONObject(str), record);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean parseRecord(final String str, RecordInfo record) {
        try {
            parseRecord(new JSONObject(str), record);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String fileNameFromStartTime(long start_time) {
        // 转成32进制，字符表：23456789ABCDEFGHJKLMNPQRSTUVWXYZ
        StringBuilder str = new StringBuilder();
        while (start_time != 0) {
            int idx = (int)(start_time & 31);
            if (idx < 8) {
                str.append((char)('2' + idx));
            } else if (idx < 16) {
                str.append((char)('A' + (idx - 8)));
            } else if (idx < 21) {
                str.append((char)('J' + (idx - 16)));
            } else {
                str.append((char)('P' + (idx - 21)));
            }
            start_time >>= 5;
        }
        if (str.length() != 0) {
            str.reverse();
        } else {
            str.append('0');
        }
        str.append(".json");
        return str.toString();
    }

}
