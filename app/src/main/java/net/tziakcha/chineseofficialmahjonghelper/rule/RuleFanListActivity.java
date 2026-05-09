package net.tziakcha.chineseofficialmahjonghelper.rule;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonWebFullScreenDialog;

public class RuleFanListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.common_recycler_layout, null);
        setContentView(contentView);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("国标麻将番种表");

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                getOnBackPressedDispatcher().onBackPressed());

        contentView.findViewById(R.id.ab_r_btn).setVisibility(View.GONE);

        RecyclerView rv = contentView.findViewById(R.id.crl_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new FanRecyclerViewAdapter());
    }

    private static final String[] LIST_ITEM_TITLE = {
            "1番", "2番", "4番", "6番", "8番", "12番", "16番", "24番", "32番", "48番", "64番", "88番"
    };

    // 表格各行内容包含的番种索引
    private static final int[][] FAN_ITEM_INDEX = {
            {
                    Mahjong.PURE_DOUBLE_CHOW,  // 一般高
                    Mahjong.MIXED_DOUBLE_CHOW,  // 喜相逢
                    Mahjong.SHORT_STRAIGHT,  // 连六
                    Mahjong.TWO_TERMINAL_CHOWS,  // 老少副
                    Mahjong.PUNG_OF_TERMINALS_OR_HONORS,  // 幺九刻
                    Mahjong.MELDED_KONG,  // 明杠
                    Mahjong.ONE_VOIDED_SUIT,  // 缺一门
                    Mahjong.NO_HONORS,  // 无字
                    Mahjong.EDGE_WAIT,  // 独听・边张
                    Mahjong.CLOSED_WAIT,  // 独听・嵌张
                    Mahjong.SINGLE_WAIT,  // 独听・单钓
                    Mahjong.SELF_DRAWN,  // 自摸
                    Mahjong.FLOWER_TILES,  // 花牌
            },
            {
                    Mahjong.DRAGON_PUNG,  // 箭刻
                    Mahjong.PREVALENT_WIND,  // 圈风刻
                    Mahjong.SEAT_WIND,  // 门风刻
                    Mahjong.CONCEALED_HAND,  // 门前清
                    Mahjong.ALL_CHOWS,  // 平和
                    Mahjong.TILE_HOG,  // 四归一
                    Mahjong.DOUBLE_PUNG,  // 双同刻
                    Mahjong.TWO_CONCEALED_PUNGS,  // 双暗刻
                    Mahjong.CONCEALED_KONG,  // 暗杠
                    Mahjong.ALL_SIMPLES,  // 断幺
            },
            {
                    Mahjong.OUTSIDE_HAND,  // 全带幺
                    Mahjong.FULLY_CONCEALED_HAND,  // 不求人
                    Mahjong.TWO_MELDED_KONGS,  // 双明杠
                    Mahjong.LAST_TILE,  // 和绝张
            },
            {
                    Mahjong.ALL_PUNGS,  // 碰碰和
                    Mahjong.HALF_FLUSH,  // 混一色
                    Mahjong.MIXED_SHIFTED_CHOWS,  // 三色三步高
                    Mahjong.ALL_TYPES,  // 五门齐
                    Mahjong.MELDED_HAND,  // 全求人
                    Mahjong.TWO_CONCEALED_KONGS,  // 双暗杠
                    Mahjong.TWO_DRAGONS_PUNGS,  // 双箭刻
            },
            {
                    Mahjong.MIXED_STRAIGHT,  // 花龙
                    Mahjong.REVERSIBLE_TILES,  // 推不倒
                    Mahjong.MIXED_TRIPLE_CHOW,  // 三色三同顺
                    Mahjong.MIXED_SHIFTED_PUNGS,  // 三色三节高
                    Mahjong.CHICKEN_HAND,  // 无番和
                    Mahjong.LAST_TILE_DRAW,  // 妙手回春
                    Mahjong.LAST_TILE_CLAIM,  // 海底捞月
                    Mahjong.OUT_WITH_REPLACEMENT_TILE,  // 杠上开花
                    Mahjong.ROBBING_THE_KONG,  // 抢杠和
            },
            {
                    Mahjong.LESSER_HONORS_AND_KNITTED_TILES,  // 全不靠
                    Mahjong.KNITTED_STRAIGHT,  // 组合龙
                    Mahjong.UPPER_FOUR,  // 大于五
                    Mahjong.LOWER_FOUR,  // 小于五
                    Mahjong.BIG_THREE_WINDS,  // 三风刻
            },
            {
                    Mahjong.PURE_STRAIGHT,  // 清龙
                    Mahjong.THREE_SUITED_TERMINAL_CHOWS,  // 三色双龙会
                    Mahjong.PURE_SHIFTED_CHOWS_1,  // 一色三步高Ⅰ
                    Mahjong.PURE_SHIFTED_CHOWS_2,  // 一色三步高Ⅱ
                    Mahjong.ALL_FIVE,  // 全带五
                    Mahjong.TRIPLE_PUNG,  // 三同刻
                    Mahjong.THREE_CONCEALED_PUNGS,  // 三暗刻
            },
            {
                    Mahjong.SEVEN_PAIRS,  // 七对
                    Mahjong.GREATER_HONORS_AND_KNITTED_TILES,  // 七星不靠
                    Mahjong.ALL_EVEN_PUNGS,  // 全双刻
                    Mahjong.FULL_FLUSH,  // 清一色
                    Mahjong.PURE_TRIPLE_CHOW,  // 一色三同顺
                    Mahjong.PURE_SHIFTED_PUNGS,  // 一色三节高
                    Mahjong.UPPER_TILES,  // 全大
                    Mahjong.MIDDLE_TILES,  // 全中
                    Mahjong.LOWER_TILES,  // 全小
            },
            {
                    Mahjong.FOUR_PURE_SHIFTED_CHOWS_1,  // 一色四步高Ⅰ
                    Mahjong.FOUR_PURE_SHIFTED_CHOWS_2,  // 一色四步高Ⅱ
                    Mahjong.THREE_KONGS,  // 三杠
                    Mahjong.ALL_TERMINALS_AND_HONORS,  // 混幺九
            },
            {
                    Mahjong.QUADRUPLE_CHOW,  // 一色四同顺
                    Mahjong.FOUR_PURE_SHIFTED_PUNGS,  // 一色四节高
            },
            {
                    Mahjong.ALL_TERMINALS,  // 清幺九
                    Mahjong.LITTLE_FOUR_WINDS,  // 小四喜
                    Mahjong.LITTLE_THREE_DRAGONS,  // 小三元
                    Mahjong.ALL_HONORS,  // 字一色
                    Mahjong.FOUR_CONCEALED_PUNGS,  // 四暗刻
                    Mahjong.PURE_TERMINAL_CHOWS,  // 一色双龙会
            },
            {
                    Mahjong.BIG_FOUR_WINDS,  // 大四喜
                    Mahjong.BIG_THREE_DRAGONS,  // 大三元
                    Mahjong.ALL_GREEN,  // 绿一色
                    Mahjong.NINE_GATES,  // 九莲宝灯
                    Mahjong.FOUR_KONGS,  // 四杠
                    Mahjong.SEVEN_SHIFTED_PAIRS,  // 连七对
                    Mahjong.THIRTEEN_ORPHANS,  // 十三幺
            },
    };

    private void onFanClick(int itemIndex, int which) {
        if (itemIndex != -1) {
            int fan = FAN_ITEM_INDEX[itemIndex][which];
            new CommonWebFullScreenDialog(this, Mahjong.FAN_NAME[fan],
                    "file:///android_asset/www/rule/fan/" + fan + ".html").show();
        }
    }

    private static float sButtonGapF;
    private static float sButtonWidthF;
    private static float sButtonHeightF;
    private static int sButtonGapI;
    private static int sButtonWidthI;
    private static int sButtonHeightI;

    private final class FanRecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitleText;
        private final View[] mWrappers = new View[13];
        private final TextView[] mFanTexts = new TextView[13];
        private int mIndex = -1;

        public FanRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            init(itemView.getContext());
        }

        private void init(Context context) {
            if (sButtonWidthI == 0) {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                sButtonGapF = context.getResources().getDimension(R.dimen.dp5);
                sButtonHeightF = context.getResources().getDimension(R.dimen.dp28);
                sButtonWidthF = (metrics.widthPixels - 5 * sButtonGapF) * 0.25f;
                sButtonGapI = (int)sButtonGapF;
                sButtonWidthI = (int)sButtonWidthF;
                sButtonHeightI = (int)sButtonHeightF;
            }

            LinearLayout linearLayout = (LinearLayout)itemView;
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            final int dp16 = context.getResources().getDimensionPixelSize(R.dimen.dp16);
            final int dp6 = context.getResources().getDimensionPixelSize(R.dimen.dp6);

            // 几番
            TextView textView0 = new TextView(context);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            llp.leftMargin = (int)sButtonGapF;
            llp.rightMargin = (int)sButtonGapF;
            textView0.setLayoutParams(llp);
            textView0.setGravity(Gravity.CENTER_VERTICAL);
            textView0.setTextColor(ContextCompat.getColor(context, R.color.text_1));
            textView0.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp16);
            linearLayout.addView(textView0);
            mTitleText = textView0;

            // 下面所有按钮的根View
            RelativeLayout root = new RelativeLayout(context);
            root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(root);

            for (int i = 0; i < 13; ++i) {
                View wrapper = View.inflate(context, R.layout.fan_button_wrapper_layout, null);
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(sButtonWidthI, sButtonHeightI);
                rlp.topMargin = (int)(sButtonGapF + (sButtonHeightF + sButtonGapF) * (i >> 2));
                rlp.leftMargin = (int)(sButtonGapF + (sButtonWidthF + sButtonGapF) * (i & 3));
                wrapper.setLayoutParams(rlp);
                root.addView(wrapper);
                mWrappers[i] = wrapper;

                final int which = i;
                wrapper.findViewById(R.id.fbw_btn).setOnClickListener(
                        view -> onFanClick(mIndex, which));

                TextView textView1 = wrapper.findViewById(R.id.fbw_txt);
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView1,
                        dp6, dp16, 2, TypedValue.COMPLEX_UNIT_PX);
                mFanTexts[i] = textView1;
            }
        }

        public void setup(int idx) {
            mIndex = idx;

            mTitleText.setText(LIST_ITEM_TITLE[idx]);
            ((LinearLayout.LayoutParams)mTitleText.getLayoutParams()).topMargin
                    = idx != 0 ? sButtonGapI : 0;

            final int[] itemIndex = FAN_ITEM_INDEX[idx];
            final int cnt = itemIndex.length;  // 一共有这么多

            // 在这之前的显示
            for (int i = 0; i < cnt; ++i) {
                int n = itemIndex[i];
                mFanTexts[i].setText(Mahjong.FAN_NAME[n]);
                mWrappers[i].setVisibility(View.VISIBLE);
            }

            // 在这之后的隐藏
            for (int i = cnt; i < 13; ++i) {
                mWrappers[i].setVisibility(View.GONE);
            }
        }
    }

    private final class FanRecyclerViewAdapter extends RecyclerView.Adapter<FanRecyclerViewHolder> {

        @NonNull
        @Override
        public FanRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FanRecyclerViewHolder(new LinearLayout(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull FanRecyclerViewHolder holder, int position) {
            holder.setup(position);
        }

        @Override
        public int getItemCount() {
            return 12;
        }
    }

}
