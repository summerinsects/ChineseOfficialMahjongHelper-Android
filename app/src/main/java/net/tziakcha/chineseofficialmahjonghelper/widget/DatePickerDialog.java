package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;

import net.tziakcha.chineseofficialmahjonghelper.CalendarUtils;
import net.tziakcha.chineseofficialmahjonghelper.R;

import java.util.ArrayList;

public class DatePickerDialog extends AlertDialog {

    public interface OnSubmitListener {
        void onSubmit(CalendarUtils.GregorianDate date);
    }

    private static final int PAGE_DAY = 0;
    private static final int PAGE_MONTH = 1;
    private static final int PAGE_YEAR = 2;

    private final CalendarUtils.GregorianDate mPicked;
    private final CalendarUtils.GregorianDate mToday;
    private int mPage = PAGE_DAY;  // 0日期 1月份 2年份
    private int mDayOffset = 0;
    private int mDecadeStart = 0;
    private final OnSubmitListener mOnSubmitListener;
    private TextView mGDateText;
    private TextView mCDateText;
    private Button mToggleButton;
    private LinearLayout mDateLayout;
    private LinearLayout mMonthLayout;
    private LinearLayout mYearLayout;
    private final CheckBox[] mDayChecks = new CheckBox[42];
    private final TextView[] mLargeTexts = new TextView[42];
    private final TextView[] mSmallTexts = new TextView[42];
    private final CalendarUtils.ChineseDate[] mChineseDates = new CalendarUtils.ChineseDate[42];
    private final CheckBox[] mMonthChecks = new CheckBox[12];
    private final CheckBox[] mYearChecks = new CheckBox[10];

    public DatePickerDialog(@NonNull Context context, CalendarUtils.GregorianDate hint, OnSubmitListener listener) {
        super(context);
        mOnSubmitListener = listener;
        mPicked = new CalendarUtils.GregorianDate();
        mPicked.assign(hint);
        mToday = new CalendarUtils.GregorianDate();
    }

    @Override
    public void show() {
        super.show();

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.date_picker_layout, null);

        Button button = contentView.findViewById(R.id.dfl_btn_neg);
        button.setText("取消");
        button.setOnClickListener(view -> dismiss());

        button = contentView.findViewById(R.id.dfl_btn_pos);
        button.setText("确定");
        button.setOnClickListener(view -> {
            mOnSubmitListener.onSubmit(mPicked);
            dismiss();
        });

        mGDateText = contentView.findViewById(R.id.dpl_txt_gd);
        mCDateText = contentView.findViewById(R.id.dpl_txt_cd);

        contentView.findViewById(R.id.dpl_ib_l).setOnClickListener(view -> onLeftButton());

        contentView.findViewById(R.id.dpl_ib_r).setOnClickListener(view -> onRightButton());

        button = contentView.findViewById(R.id.dpl_btn_tg);
        button.setOnClickListener(view -> onToggleButton());
        mToggleButton = button;

        contentView.findViewById(R.id.dpl_btn_today).setOnClickListener(view -> onTodayButton());

        final int dp10 = getContext().getResources().getDimensionPixelSize(R.dimen.dp10);
        final int dp5 = getContext().getResources().getDimensionPixelSize(R.dimen.dp5);

        mDateLayout = contentView.findViewById(R.id.dpl_ll_date);
        final int[] dateIDs = {
                R.id.dpl_ll_date0, R.id.dpl_ll_date1, R.id.dpl_ll_date2,
                R.id.dpl_ll_date3, R.id.dpl_ll_date4, R.id.dpl_ll_date5
        };
        for (int k = 0; k < 6; ++k) {
            LinearLayout line = mDateLayout.findViewById(dateIDs[k]);
            for (int i = 0; i < 7; ++i) {
                int idx = k * 7 + i;
                RelativeLayout rl = (RelativeLayout)line.getChildAt(i);
                CheckBox checkBox = rl.findViewById(R.id.dpi_cb);
                checkBox.setOnCheckedChangeListener(getDayCheckedChangeListener(idx));
                mDayChecks[idx] = checkBox;
                mLargeTexts[idx] = rl.findViewById(R.id.dpi_txt_g);
                mSmallTexts[idx] = rl.findViewById(R.id.dpi_txt_c);
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mSmallTexts[idx],
                        dp5, dp10, 1, TypedValue.COMPLEX_UNIT_PX);

                mChineseDates[idx] = new CalendarUtils.ChineseDate();
            }
        }

        mMonthLayout = contentView.findViewById(R.id.dpl_ll_month);
        final int[] monthIDs = {
            R.id.dpl_ll_month0, R.id.dpl_ll_month1, R.id.dpl_ll_month2
        };
        for (int k = 0; k < 3; ++k) {
            LinearLayout line = contentView.findViewById(monthIDs[k]);
            for (int i = 0; i < 4; ++i) {
                int idx = k * 4 + i;
                CheckBox checkBox = (CheckBox)line.getChildAt(i);
                checkBox.setOnCheckedChangeListener(getMonthCheckedChangeListener(idx));
                mMonthChecks[idx] = checkBox;
            }
        }

        mYearLayout = contentView.findViewById(R.id.dpl_ll_year);
        final int[] yearIDs = {
                R.id.dpl_ll_year0, R.id.dpl_ll_year1, R.id.dpl_ll_year2
        };
        for (int k = 0; k < 3; ++k) {
            LinearLayout line = contentView.findViewById(yearIDs[k]);
            for (int i = 0; i < 4; ++i) {
                int idx = k * 4 + i;
                if (idx < 10) {
                    CheckBox checkBox = (CheckBox)line.getChildAt(i);
                    checkBox.setOnCheckedChangeListener(getYearCheckedChangeListener(idx));
                    mYearChecks[idx] = checkBox;
                }
            }
        }

        setupForDay();

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 监听返回键
        setOnKeyListener((dialogInterface, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                dismiss();
                return true;  // 返回true表示事件已处理
            }
            return false;  // 返回false表示事件未处理，将传递到下一个监听器
        });
    }

    private static final class Festival {
        public int priority;  // 优先级
        public String name;  // 节日名
        public int cal;  // 0农历，2公历
        public int month;
        public int day;
        public int since;  // 从多少年起
        public int week;  // 星期几（公历有一些节日，如母亲节、父亲节是固定在星期的）
        public int offset;  // 偏移，如上面母亲节、父亲节，在第2第3个星期日

        public Festival(int priority, String name, int cal, int month, int day) {
            this.priority = priority;
            this.name = name;
            this.cal = cal;
            this.month = month;
            this.day = day;
        }

        public Festival(int priority, String name, int cal, int month, int day, int since) {
            this.priority = priority;
            this.name = name;
            this.cal = cal;
            this.month = month;
            this.day = day;
            this.since = since;
        }

        public Festival(int priority, String name, int cal, int month, int day, int since, int week, int offset) {
            this.priority = priority;
            this.name = name;
            this.cal = cal;
            this.month = month;
            this.day = day;
            this.since = since;
            this.week = week;
            this.offset = offset;
        }
    }

    private static final int WINTER_99_PRIORITY = 1;
    private static final int DOG_DAYS_PRIORITY = 1;

    private static final Festival[] CommonFestivals = {
            new Festival(3, "元旦", 0, 1, 1),
            new Festival(3, "春节", 0, 1, 1, 1912),
            new Festival(3, "元宵节", 0, 1, 15),
            new Festival(2, "龙抬头", 0, 2, 2),
            new Festival(2, "上巳节", 0, 3, 3),
            new Festival(2, "佛诞", 0, 4, 8),
            new Festival(3, "端午节", 0, 5, 5),
            new Festival(2, "七夕", 0, 7, 7),
            new Festival(2, "中元节", 0, 7, 15),
            new Festival(3, "中秋节", 0, 8, 15),
            new Festival(2, "重阳节", 0, 9, 9),
            new Festival(2, "寒衣节", 0, 10, 1),
            new Festival(2, "下元节", 0, 10, 15),
            new Festival(2, "腊八节", 0, 12, 8),
            new Festival(2, "北方小年", 0, 12, 23),
            new Festival(2, "南方小年", 0, 12, 24),
            new Festival(3, "除夕", 0, 12, 30),
            new Festival(3, "元旦", 2, 1, 1, 1912),
            new Festival(2, "情人节", 2, 2, 14),
            new Festival(3, "妇女节", 2, 3, 8, 1976),
            new Festival(2, "植树节", 2, 3, 12, 1979),
            new Festival(2, "愚人节", 2, 4, 1),
            new Festival(3, "劳动节", 2, 5, 1, 1950),
            new Festival(3, "青年节", 2, 5, 4, 1950),
            new Festival(2, "母亲节", 2, 5, 0, 1913, 0, 2),
            new Festival(3, "儿童节", 2, 6, 1, 1950),
            new Festival(2, "父亲节", 2, 6, 0, 1910, 0, 3),
            new Festival(3, "建党节", 2, 7, 1, 1938),
            new Festival(3, "建军节", 2, 8, 1, 1933),
            new Festival(2, "抗战胜利", 2, 9, 3, 1945),
            new Festival(3, "教师节", 2, 9, 9, 1985),
            new Festival(2, "烈士纪念日", 2, 9, 30, 2014),
            new Festival(3, "国庆节", 2, 10, 1, 1949),
            new Festival(2, "感恩节", 2, 11, 0, 0, 4, 4),
            new Festival(3, "国家公祭日", 2, 12, 13, 2014),
            new Festival(2, "平安夜", 2, 12, 24),
            new Festival(2, "圣诞节", 2, 12, 25)
    };


    // 春社：立春后五戊
    // 设首戊为D号，五戊为：D+50-28=D+22（平年）；D+50-29=D+21（闰年），有落入4月的可能
    private static void getSpringSocial(ArrayList<Festival> festivals, int y, int m, boolean leap) {
        int d = CalendarUtils.Gregorian_GetSolarTerm(y, 2);
        int s = CalendarUtils.Gregorian_GetStemBranch(y, 2, d) % 10;  // 立春日天干
        d += (s <= 4 ? 4 : 14) - s;
        d += (leap ? 21 : 22);
        if ((m == 3 && d <= 31) || (m == 4 && d > 31)) {
            festivals.add(new Festival(2, "春社", 2, m, d <= 31 ? d : d - 31));
        }
    }

    // 秋社：立秋后第五个戊日
    // 设首戊为D号，五戊为：D+50-31=D+19，有落入10月的可能
    private static void getAutumnSocial(ArrayList<Festival> festivals, int y, int m) {
        int d = CalendarUtils.Gregorian_GetSolarTerm(y, 14);
        int s = CalendarUtils.Gregorian_GetStemBranch(y, 8, d) % 10;  // 立秋日天干
        d += (s <= 4 ? 4 : 14) - s;
        d += 19;
        if ((m == 9 && d <= 31) || (m == 10 && d > 31)) {
            festivals.add(new Festival(2, "秋社", 2, m, d <= 31 ? d : d - 31));
        }
    }

    private static void getFestivals(int y, int m, int mm, int dd, boolean l, ArrayList<Festival> festivals) {
        for (final Festival f : CommonFestivals) {
            int fm = f.month, d;
            switch (f.cal) {
                case 0:
                    d = f.day;
                    // 农历节日
                    // 1. 闰月中不可能有节日，过滤掉已经过了的节日
                    // 2. 公历一个月最多31天，当1号是A月B日时，31号可能是(A+1)月(B+1)日
                    // 3. 当1号是A月最后一天时，即2号是(A+1)月初一时，31号可能是(A+2)月初一
                    if ((!l && fm == mm && d >= dd) || ((fm == mm + 1 || (fm == 1 && mm == 12)) && (d < dd + 2)) || (dd > 28 && fm == (mm + 1) % 12 + 1 && d < 2)) {
                        festivals.add(f);
                    }
                    break;
                case 2:
                    if (y >= f.since && m == fm) {
                        festivals.add(f);
                    }
                    break;
            }
        }

        int[] solarTerms = {0, 0};
        for (int i = 0; i < 2; ++i) {
            int idx = m * 2 - 2 + i;
            solarTerms[i] = CalendarUtils.Gregorian_GetSolarTerm(y, idx);
            festivals.add(new Festival(2, SolarTermsText[idx], 2, m, solarTerms[i]));
        }

        int d, s;
        switch (m) {
            case 1:
                // 1月有（二九）、三九、四九、五九
                // 设冬至为W号，三九为：W+9*2-31=W-13
                d = CalendarUtils.Gregorian_GetSolarTerm(y - 1, 23) - 13;
                if (d > 9) {
                    festivals.add(new Festival(WINTER_99_PRIORITY, "二九", 2, 1, d - 9));
                }
                festivals.add(new Festival(WINTER_99_PRIORITY, "三九", 2, 1, d));
                festivals.add(new Festival(WINTER_99_PRIORITY, "四九", 2, 1, d + 9));
                festivals.add(new Festival(WINTER_99_PRIORITY, "五九", 2, 1, d + 18));
                break;
            case 2:
                // 2月有六九、七九、八九
                // 设冬至为W号，六九为：W+9*5-31-31=W-17
                d = CalendarUtils.Gregorian_GetSolarTerm(y - 1, 23) - 17;
                festivals.add(new Festival(WINTER_99_PRIORITY, "六九", 2, 2, d));
                festivals.add(new Festival(WINTER_99_PRIORITY, "七九", 2, 2, d + 9));
                festivals.add(new Festival(WINTER_99_PRIORITY, "八九", 2, 2, d + 18));
            break;
            case 3:
                // 3月有九九
                // 设冬至为W号，九九为：W+9*8-31-31-28=w-18（平年）；W+9*8-31-31-29=W-19（闰年）
                l = CalendarUtils.Gregorian_IsLeapYear(y);
                d = CalendarUtils.Gregorian_GetSolarTerm(y - 1, 23) - (l ? 19 : 18);
                festivals.add(new Festival(WINTER_99_PRIORITY, "九九", 2, 3, d));

                // 春社
                getSpringSocial(festivals, y, 3, l);
                break;
            case 4:
                // 寒食节，清明前一天
                festivals.add(new Festival(2, "寒食节", 2, 4, solarTerms[0] - 1));

                // 春社
                getSpringSocial(festivals, y, 4, CalendarUtils.Gregorian_IsLeapYear(y));
                break;
            case 6:
                // 芒种见丙入梅
                // 由于紫金山天文台目前只能查到2022年~2024年，这三年未出现的芒种当天是丙日的情况，只能根据2024出梅的算法猜测亦如此
                d = solarTerms[0];
                s = CalendarUtils.Gregorian_GetStemBranch(y, 6, d) % 10;  // 芒种日天干
                festivals.add(new Festival(1, "入梅", 2, 6, d + (s > 2 ? 12 : 2) - s));
                break;
            case 7:
                // 7月有初伏、中伏
                // 初伏：夏至后的第三个庚日；中伏：夏至后的第四个庚日
                // 夏至最晚在6月22日，极端情况前一日（21日）为庚日，那么初伏将在7月21日，中伏在31日，不会落入8月
                // 对于夏至当天是庚日的情况，紫金山天文台的算法是也算一次
                // 例如2023年夏至是6月21日，干支为庚戌，7月11日为初伏，21日为中伏
                d = CalendarUtils.Gregorian_GetSolarTerm(y, 11);  // 夏至日期
                s = CalendarUtils.Gregorian_GetStemBranch(y, 6, d) % 10;  // 夏至日天干
                if (s <= 6) {
                    d -= 4 + s;
                } else {
                    d += 6 - s;
                }
                festivals.add(new Festival(DOG_DAYS_PRIORITY, "初伏", 2, 7, d));
                festivals.add(new Festival(DOG_DAYS_PRIORITY, "中伏", 2, 7, d + 10));

                // 小暑见未出梅
                // 对于小暑当天是未日的情况，紫金山天文台的算法是当天即出梅
                // 例如2024年小暑是7月6日，干支为辛未
                d = solarTerms[0];
                s = CalendarUtils.Gregorian_GetStemBranch(y, 7, d) % 12;  // 小暑日地支
                festivals.add(new Festival(1, "出梅", 2, 7, d + (s <= 7 ? 7 : 19) - s));
                break;
            case 8:
                // 8月有末伏
                // 末伏：立秋后第一个庚日
                // 对于立秋当天是庚日的情况，网络上主流的算法是直接算末伏，这里保持跟网上一致
                // 由于紫金山天文台目前只能查到2022年~2024年，这三年未出现的立秋当天是庚日的情况，只能根据2023初伏的算法猜测亦如此
                d = solarTerms[0];  // 立秋日期
                s = CalendarUtils.Gregorian_GetStemBranch(y, 8, d) % 10;  // 立秋日天干
                festivals.add(new Festival(DOG_DAYS_PRIORITY, "末伏", 2, 8,  d + (s <= 6 ? 6 : 16) - s));
                break;
            case 9:
            case 10:
                // 秋社
                getAutumnSocial(festivals, y, m);
                break;
            case 12:
                // 冬至日当天就是一九第1天，冬至如果出现在22号之后，二九就会落在次年1月
                d = CalendarUtils.Gregorian_GetSolarTerm(y, 23) + 9;
                if (d <= 31) {
                    festivals.add(new Festival(WINTER_99_PRIORITY, "二九", 2, 12, d));
                }
                break;
            default:
                break;
        }
    }

    private static Festival pickFestival(final CalendarUtils.GregorianDate g,
            final CalendarUtils.ChineseDate c, int week, ArrayList<Festival> festivals) {
        Festival res = null;
        for (Festival f : festivals) {
            boolean pick = false;
            int since = f.since;
            switch (f.cal) {
                case 0:
                    // 农历节日不会出现在闰月
                    if ((since == 0 || c.year >= since) && !c.leap && c.month == f.month) {
                        int d1 = f.day, d2 = c.day;
                        // 类似除夕那种在三十的节日，如果当月没有三十，则改为廿九
                        if (d1 == d2 || (d1 == 30 && d2 == 29 && !c.major)) {
                            pick = true;
                        }
                    }
                    break;
                case 2:
                    if ((since == 0 || g.year >= since) && g.month == f.month) {
                        int d1 = f.day, d2 = g.day;
                        if (d1 == d2) {
                            pick = true;
                        } else if (d1 == 0) {
                            // 固定在星期几的节日
                            if (week == f.week) {
                                if (f.offset >= 0) {
                                    int t = f.offset * 7 - d2;
                                    if (t >= 0 && t <= 6) {
                                        pick = true;
                                    }
                                }
                            }
                        }
                    }
                    break;
                default:
                    break;
            }

            if (pick && (res == null || res.priority < f.priority || res.since < f.since)) {
                res = f;
            }
        }

        return res;
    }

    private static final String[] WeekText = {
        "日", "一", "二", "三", "四", "五", "六"
    };

    private static final String[] SolarTermsText = {
        "小寒", "大寒",
        "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
        "立夏", "小满", "芒种", "夏至", "小暑", "大暑",
        "立秋", "处暑", "白露", "秋分", "寒露", "霜降",
        "立冬", "小雪", "大雪", "冬至"
    };

    private static final String[] CelestialStem = {
        "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };

    private static final String[] TerrestrialBranch = {
        "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };

    private static final String[] ChineseZodiac = {
        "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };

    private static final String[] Chinese_MonthText = {
        "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"
    };

    private static final String[] Chinese_DayText = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    private static void adjustDate(CalendarUtils.GregorianDate date) {
        int lastDay = CalendarUtils.Gregorian_DaysInMonth(date.year, date.month);
        date.day = Math.min(date.day, lastDay);
    }

    private void refreshTitle(CalendarUtils.ChineseDate chineseDate) {
        if (chineseDate == null) {
            chineseDate = CalendarUtils.Gregorian2Chinese(mPicked);
        }

        StringBuilder str = new StringBuilder();
        str.append(mPicked.year).append("年");
        str.append(mPicked.month).append("月");
        str.append(mPicked.day).append("日 星期");
        str.append(WeekText[CalendarUtils.Gregorian_WeekDay(mPicked.year, mPicked.month, mPicked.day)]);
        mGDateText.setText(str.toString());

        str.setLength(0);
        int cz = (chineseDate.year + 8) % 12;
        str.append(CelestialStem[(chineseDate.year + 6) % 10]);
        str.append(TerrestrialBranch[cz]);
        str.append("（");
        str.append(ChineseZodiac[cz]);
        str.append("）年");
        if (chineseDate.leap) str.append("闰");
        str.append(Chinese_MonthText[chineseDate.month - 1]);
        str.append(Chinese_DayText[chineseDate.day - 1]);
        mCDateText.setText(str.toString());
    }

    private static final int COLOR_NORMAL = Color.rgb(0x77, 0x77, 0x77);
    private static final int COLOR_TODAY = Color.rgb(0x1e, 0x66, 0xb3);
    private static final int COLOR_FESTIVAL = Color.rgb(0xfe, 0x57, 0x6e);

    private static void setupLabelSmall(TextView textView,
            CalendarUtils.GregorianDate gregorianDate, CalendarUtils.ChineseDate chineseDate,
            int week, final ArrayList<Festival> festivals) {

        Festival festival = pickFestival(gregorianDate, chineseDate, week, festivals);

        // 优先顺序：优先度>1的节日、二十四节气、优先度<=的节日
        if (festival == null) {
            if (chineseDate.day > 1) {  // 不是初一
                textView.setText(Chinese_DayText[chineseDate.day - 1]);
                textView.setTextColor(COLOR_NORMAL);
            } else {
                // 将初一显示为（闰）某月大/小的形式
                StringBuilder str = new StringBuilder();
                if (chineseDate.leap) {
                    str.append("闰");
                }
                str.append(Chinese_MonthText[chineseDate.month - 1]);
                str.append(chineseDate.major ? "大" : "小");

                textView.setText(str.toString());
                textView.setTextColor(COLOR_NORMAL);
            }
        } else {
            textView.setText(festival.name);
            textView.setTextColor(COLOR_FESTIVAL);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setupForDay() {
        adjustDate(mPicked);

        mPage = PAGE_DAY;
        mDateLayout.setVisibility(View.VISIBLE);
        mMonthLayout.setVisibility(View.GONE);
        mYearLayout.setVisibility(View.GONE);
        mToggleButton.setText(mPicked.year + "年" + mPicked.month + "月");

        // 本月的1号是星期几，那么前面的天数属于上个月
        int weekday = CalendarUtils.Gregorian_WeekDay(mPicked.year, mPicked.month, 1);
        for (int i = 0; i < weekday; ++i) {
            ((RelativeLayout)mDayChecks[i].getParent()).setVisibility(View.INVISIBLE);
        }
        mDayOffset = weekday;

        // 公历
        CalendarUtils.GregorianDate gregorianDate = new CalendarUtils.GregorianDate(
                mPicked.year, mPicked.month, 1);
        int gregorianLastDay = CalendarUtils.Gregorian_DaysInMonth(mPicked.year, mPicked.month);

        // 农历
        CalendarUtils.ChineseDate chineseDate = CalendarUtils.Gregorian2Chinese(gregorianDate);
        int chineseLastDay = chineseDate.major ? 30 : 29;

        // 节日
        ArrayList<Festival> festivals = new ArrayList<>();
        getFestivals(gregorianDate.year, gregorianDate.month,
                chineseDate.month, chineseDate.day, chineseDate.leap, festivals);

        // 本月的天数
        for (int i = 0; i < gregorianLastDay; ++i) {
            int idx = weekday + i;
            ((RelativeLayout)mDayChecks[idx].getParent()).setVisibility(View.VISIBLE);

            mDayChecks[idx].setOnCheckedChangeListener(null);
            mDayChecks[idx].setChecked(false);
            mDayChecks[idx].setOnCheckedChangeListener(getDayCheckedChangeListener(idx));

            mLargeTexts[idx].setText(String.valueOf(i + 1));
            mLargeTexts[idx].setTextColor(COLOR_NORMAL);

            setupLabelSmall(mSmallTexts[idx], gregorianDate, chineseDate, (weekday + i) % 7, festivals);

            mChineseDates[idx].assign(chineseDate);

            // 日期增加
            ++gregorianDate.day;
            ++chineseDate.day;
            if (chineseDate.day > chineseLastDay) {  // 超过农历月最后一天，农历到下一个月
                chineseDate = CalendarUtils.Gregorian2Chinese(gregorianDate);
                chineseLastDay = chineseDate.major ? 30 : 29;
            }
        }

        // 超出的属于下个月
        for (int i = weekday + gregorianLastDay; i < 42; ++i) {
            ((RelativeLayout)mDayChecks[i].getParent()).setVisibility(View.INVISIBLE);
        }

        // 高亮今天
        if (mPicked.year == mToday.year && mPicked.month == mToday.month) {
            int idx = weekday + mToday.day - 1;
            mLargeTexts[idx].setTextColor(COLOR_TODAY);

            if (mSmallTexts[idx].getCurrentTextColor() == COLOR_NORMAL) {
                mSmallTexts[idx].setTextColor(COLOR_TODAY);
            }
        }
        mDayChecks[mDayOffset + mPicked.day - 1].setChecked(true);

        refreshTitle(mChineseDates[mDayOffset + mPicked.day - 1]);
    }

    @SuppressLint("SetTextI18n")
    private void setupForMonth() {
        adjustDate(mPicked);

        mPage = PAGE_MONTH;
        mDateLayout.setVisibility(View.GONE);
        mMonthLayout.setVisibility(View.VISIBLE);
        mYearLayout.setVisibility(View.GONE);
        mToggleButton.setText(mPicked.year + "年");

        mMonthChecks[mToday.month - 1].setTextColor(
                mPicked.year != mToday.year ? COLOR_NORMAL : COLOR_TODAY);

        for (int i = 0; i < 12; ++i) {
            mMonthChecks[i].setOnCheckedChangeListener(null);
            mMonthChecks[i].setChecked(mPicked.month - 1 == i);
            mMonthChecks[i].setOnCheckedChangeListener(getMonthCheckedChangeListener(i));
        }

        refreshTitle(null);
    }

    @SuppressLint("SetTextI18n")
    private void setupForYear() {
        mPage = PAGE_YEAR;
        mDateLayout.setVisibility(View.GONE);
        mMonthLayout.setVisibility(View.GONE);
        mYearLayout.setVisibility(View.VISIBLE);
        mToggleButton.setText(mDecadeStart + " ~ " + (mDecadeStart + 9));

        for (int i = 0; i < 10; ++i) {
            int yy = mDecadeStart + i;
            mYearChecks[i].setText(String.valueOf(yy));

            mYearChecks[i].setTextColor(yy == mToday.year ? COLOR_TODAY : COLOR_NORMAL);

            mYearChecks[i].setOnCheckedChangeListener(null);
            mYearChecks[i].setChecked(yy == mPicked.year);
            mYearChecks[i].setOnCheckedChangeListener(getYearCheckedChangeListener(i));
        }
    }

    private CheckBox.OnCheckedChangeListener getDayCheckedChangeListener(int index) {
        return (view, checked) -> onDayChecked(checked, index);
    }

    private void onDayChecked(boolean checked, int index) {
        if (checked) {
            int day = index + 1 - mDayOffset;
            if (day != mPicked.day) {
                int prev = mDayOffset + mPicked.day - 1;
                mDayChecks[prev].setOnCheckedChangeListener(null);
                mDayChecks[prev].setChecked(false);
                mDayChecks[prev].setOnCheckedChangeListener(getDayCheckedChangeListener(prev));

                mPicked.day = day;
                refreshTitle(mChineseDates[mDayOffset + mPicked.day - 1]);
            }
        } else {
            mDayChecks[index].setOnCheckedChangeListener(null);
            mDayChecks[index].setChecked(true);
            mDayChecks[index].setOnCheckedChangeListener(getDayCheckedChangeListener(index));
        }
    }

    private CheckBox.OnCheckedChangeListener getMonthCheckedChangeListener(int index) {
        return (view, checked) -> onMonthChecked(checked, index);
    }

    private void onMonthChecked(boolean checked, int index) {
        if (checked) {
            int prev = mPicked.month - 1;
            mMonthChecks[prev].setOnCheckedChangeListener(null);
            mMonthChecks[prev].setChecked(false);
            mMonthChecks[prev].setOnCheckedChangeListener(getMonthCheckedChangeListener(prev));
        } else {
            mMonthChecks[index].setOnCheckedChangeListener(null);
            mMonthChecks[index].setChecked(true);
            mMonthChecks[index].setOnCheckedChangeListener(getMonthCheckedChangeListener(index));
        }
        mPicked.month = index + 1;
        mPage = PAGE_DAY;
        setupForDay();
    }

    private CheckBox.OnCheckedChangeListener getYearCheckedChangeListener(int index) {
        return (view, checked) -> onYearChecked(checked, index);
    }

    private void onYearChecked(boolean checked, int index) {
        if (checked) {
            int prev = mPicked.year % 10;
            mYearChecks[prev].setOnCheckedChangeListener(null);
            mYearChecks[prev].setChecked(false);
            mYearChecks[prev].setOnCheckedChangeListener(getYearCheckedChangeListener(prev));
        } else {
            mYearChecks[index].setOnCheckedChangeListener(null);
            mYearChecks[index].setChecked(true);
            mYearChecks[index].setOnCheckedChangeListener(getYearCheckedChangeListener(index));
        }
        mPicked.year = mDecadeStart + index;
        mPage = PAGE_MONTH;
        setupForMonth();
    }

    private void onLeftButton() {
        switch (mPage) {
            case PAGE_DAY:
                if (mPicked.month > 1) {
                    --mPicked.month;
                    setupForDay();
                } else {
                    if (mPicked.year > CalendarUtils.CALENDAR_MIN_YEAR) {
                        --mPicked.year;
                        mPicked.month = 12;
                        setupForDay();
                    }
                }
                break;
            case PAGE_MONTH:
                if (mPicked.year > CalendarUtils.CALENDAR_MIN_YEAR) {
                    --mPicked.year;
                    setupForMonth();
                }
                break;
            case PAGE_YEAR:
                if (mDecadeStart - 10 >= CalendarUtils.CALENDAR_MIN_YEAR) {
                    mDecadeStart -= 10;
                    setupForYear();
                }
                break;
        }
    }

    private void onRightButton() {
        switch (mPage) {
            case PAGE_DAY:
                if (mPicked.month < 12) {
                    ++mPicked.month;
                    setupForDay();
                } else {
                    if (mPicked.year < CalendarUtils.CALENDAR_MAX_YEAR) {
                        ++mPicked.year;
                        mPicked.month = 1;
                        setupForDay();
                    }
                }
                break;
            case PAGE_MONTH:
                if (mPicked.year < CalendarUtils.CALENDAR_MAX_YEAR) {
                    ++mPicked.year;
                    setupForMonth();
                }
                break;
            case PAGE_YEAR:
                if (mDecadeStart + 10 < CalendarUtils.CALENDAR_MAX_YEAR) {
                    mDecadeStart += 10;
                    setupForYear();
                }
                break;
        }
    }

    private void onToggleButton() {
        switch (mPage) {
            case PAGE_DAY:
                setupForMonth();
                break;
            case PAGE_MONTH:
                mDecadeStart = mPicked.year / 10 * 10;
                setupForYear();
                break;
        }
    }

    private void onTodayButton() {
        if (mPage == PAGE_DAY && mPicked.year == mToday.year && mPicked.month == mToday.month) {
            // 本月内直接跳转
            if (mPicked.day != mToday.day) {
                int prev = mDayOffset + mPicked.day - 1;
                mDayChecks[prev].setOnCheckedChangeListener(null);
                mDayChecks[prev].setChecked(false);
                mDayChecks[prev].setOnCheckedChangeListener(getDayCheckedChangeListener(prev));

                mPicked.day = mToday.day;
                int index = mDayOffset + mToday.day - 1;
                mDayChecks[index].setOnCheckedChangeListener(null);
                mDayChecks[index].setChecked(true);
                mDayChecks[index].setOnCheckedChangeListener(getDayCheckedChangeListener(prev));

                refreshTitle(mChineseDates[mDayOffset + mPicked.day - 1]);
            }
        } else {
            mPicked.assign(mToday);
            mPage = PAGE_DAY;
            setupForDay();
        }
    }

}
