package net.tziakcha.chineseofficialmahjonghelper.record;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.R;

import java.util.ArrayList;

public class RecordSummaryResultDialog extends AlertDialog {
    public static final class SummaryInfo {
        public int competition_count;
        public int period_count;
        public int[] rank = new int[4];
        public int norm_score12;
        public int competition_score;
        public int claim_count;
        public int drawn_count;
        public int shoot_count;
        public int splash_count;
        public int tie_count;
        public int claim_fan;
        public int max_claim_fan;
        public int drawn_fan;
        public int max_drawn_fan;
        public int shoot_fan;
        public int max_shoot_fan;
        public int splash_fan;
        public int max_splash_fan;
    }

    private final SummaryInfo mSummaryInfo;

    public RecordSummaryResultDialog(@NonNull Context context,
            final ArrayList<RecordItemInfo> recordItems, final int[] selections) {
        super(context);

        SummaryInfo summary = new SummaryInfo();
        for (int i = 0, cnt = Math.min(recordItems.size(), selections.length); i < cnt; ++i) {
            int idx = selections[i];
            if (idx == -1) {
                continue;
            }

            RecordInfo record = recordItems.get(i).record;
            if (record.finish_time == 0) {
                continue;
            }

            ++summary.competition_count;
            int[] totalScores = {0, 0, 0, 0};

            for (int k = 0; k < 16; ++k) {
                RecordInfo.Detail detail = record.details[k];
                if (detail.timeout) {
                    break;
                }

                ++summary.period_count;

                int fan = detail.fan;
                int winFlag = detail.win_flag;
                int claimFlag = detail.claim_flag;
                if (fan == 0 || winFlag == 0 || claimFlag == 0) {
                    ++summary.tie_count;
                    continue;
                }

                int winIndex = -1, claimIndex = -1;
                int[] scores = RecordInfo.translateToScores(record.mode, fan,
                        winFlag, claimFlag, detail.penalty);
                for (int n = 0; n < 4; ++n) {
                    if ((winFlag & (1 << n)) != 0) {
                        winIndex = n;
                    }
                    if ((claimFlag & (1 << n)) != 0) {
                        claimIndex = n;
                    }
                    totalScores[n] += scores[n];
                }

                if (winIndex != claimIndex) {  // 点和
                    if (winIndex == idx) {  // 自己和牌
                        ++summary.claim_count;
                        summary.claim_fan += fan;
                        summary.max_claim_fan = Math.max(summary.max_claim_fan, fan);
                    } else if (claimIndex == idx) {  // 自己点炮
                        ++summary.shoot_count;
                        summary.shoot_fan += fan;
                        summary.max_shoot_fan = Math.max(summary.max_shoot_fan, fan);
                    }
                } else {  // 自摸
                    if (winIndex == idx) {  // 自己和牌
                        ++summary.drawn_count;
                        summary.drawn_fan += fan;
                        summary.max_drawn_fan = Math.max(summary.max_drawn_fan, fan);
                    } else {
                        ++summary.splash_count;
                        summary.splash_fan += fan;
                        summary.max_splash_fan = Math.max(summary.max_splash_fan, fan);
                    }
                }
            }

            summary.competition_score += totalScores[idx];

            int[] rank = RecordInfo.calcRankFromScore(totalScores);
            ++summary.rank[rank[idx]];

            int[] norm = RecordInfo.calcNorm12FromRank(rank);
            summary.norm_score12 += norm[idx];
        }

        mSummaryInfo = summary;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_summary_result_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("汇总结果");
        Button button = contentView.findViewById(R.id.dfl_btn_def);
        button.setText("确定");
        button.setOnClickListener(view -> dismiss());

        ((TextView)contentView.findViewById(R.id.rsr_txt_count0)).setText(
                "统计局数：" + mSummaryInfo.competition_count);
        ((TextView)contentView.findViewById(R.id.rsr_txt_count1)).setText(
                "有效盘数：" + mSummaryInfo.period_count);

        ((TextView)contentView.findViewById(R.id.rsr_txt_rank0)).setText(
                "一位：" + mSummaryInfo.rank[0] + " (" + formatPercentage(mSummaryInfo.rank[0], mSummaryInfo.competition_count) + ")");
        ((TextView)contentView.findViewById(R.id.rsr_txt_rank1)).setText(
                "二位：" + mSummaryInfo.rank[1] + " (" + formatPercentage(mSummaryInfo.rank[1], mSummaryInfo.competition_count) + ")");
        ((TextView)contentView.findViewById(R.id.rsr_txt_rank2)).setText(
                "三位：" + mSummaryInfo.rank[2] + " (" + formatPercentage(mSummaryInfo.rank[2], mSummaryInfo.competition_count) + ")");
        ((TextView)contentView.findViewById(R.id.rsr_txt_rank3)).setText(
                "四位：" + mSummaryInfo.rank[3] + " (" + formatPercentage(mSummaryInfo.rank[3], mSummaryInfo.competition_count) + ")");
        ((TextView)contentView.findViewById(R.id.rsr_txt_score0)).setText(
                "标准分：" + RecordInfo.normStringFrom12(mSummaryInfo.norm_score12));
        ((TextView)contentView.findViewById(R.id.rsr_txt_score1)).setText(
                "比赛分：" + mSummaryInfo.competition_score);
        ((TextView)contentView.findViewById(R.id.rsr_txt_score2)).setText(
                "平均标准分：" + formatMean(mSummaryInfo.norm_score12, mSummaryInfo.competition_count * 12));
        ((TextView)contentView.findViewById(R.id.rsr_txt_score3)).setText(
                "平均比赛分：" + formatMean(mSummaryInfo.competition_score, mSummaryInfo.competition_count));

        int win_count = mSummaryInfo.claim_count + mSummaryInfo.drawn_count;
        ((TextView)contentView.findViewById(R.id.rsr_txt_win0)).setText(
                "和牌率：" + formatPercentage(win_count, mSummaryInfo.period_count));
        ((TextView)contentView.findViewById(R.id.rsr_txt_win1)).setText(
                "荒庄率：" + formatPercentage(mSummaryInfo.tie_count, mSummaryInfo.period_count));
        ((TextView)contentView.findViewById(R.id.rsr_txt_win2)).setText(
                "点和率：" + formatPercentage(mSummaryInfo.claim_count, win_count));
        ((TextView)contentView.findViewById(R.id.rsr_txt_win3)).setText(
                "点炮率：" + formatPercentage(mSummaryInfo.shoot_count, mSummaryInfo.period_count));
        ((TextView)contentView.findViewById(R.id.rsr_txt_win4)).setText(
                "自摸率：" + formatPercentage(mSummaryInfo.drawn_count, win_count));
        ((TextView)contentView.findViewById(R.id.rsr_txt_win5)).setText(
                "被摸率：" + formatPercentage(mSummaryInfo.splash_count, mSummaryInfo.period_count));

        ((TextView)contentView.findViewById(R.id.rsr_txt_fan0)).setText(
                "平均和牌番：" + formatMean(mSummaryInfo.claim_fan + mSummaryInfo.drawn_fan, mSummaryInfo.claim_count + mSummaryInfo.drawn_count));
        ((TextView)contentView.findViewById(R.id.rsr_txt_fan1)).setText(
                "最大和牌番：" + Math.max(mSummaryInfo.max_claim_fan, mSummaryInfo.max_drawn_fan));
        ((TextView)contentView.findViewById(R.id.rsr_txt_fan2)).setText(
                "平均点和番：" + formatMean(mSummaryInfo.claim_fan, mSummaryInfo.claim_count));
        ((TextView)contentView.findViewById(R.id.rsr_txt_fan3)).setText(
                "最大点和番：" + mSummaryInfo.max_claim_fan);
        ((TextView)contentView.findViewById(R.id.rsr_txt_fan4)).setText(
                "平均自摸番：" + formatMean(mSummaryInfo.drawn_fan, mSummaryInfo.drawn_count));
        ((TextView)contentView.findViewById(R.id.rsr_txt_fan5)).setText(
                "最大自摸番：" + mSummaryInfo.max_drawn_fan);
        ((TextView)contentView.findViewById(R.id.rsr_txt_fan6)).setText(
                "平均点炮番：" + formatMean(mSummaryInfo.shoot_fan, mSummaryInfo.shoot_count));
        ((TextView)contentView.findViewById(R.id.rsr_txt_fan7)).setText(
                "最大点炮番：" + mSummaryInfo.max_shoot_fan);
        ((TextView)contentView.findViewById(R.id.rsr_txt_fan8)).setText(
                "平均被摸番：" + formatMean(mSummaryInfo.splash_fan, mSummaryInfo.splash_count));
        ((TextView)contentView.findViewById(R.id.rsr_txt_fan9)).setText(
                "最大被摸番：" + mSummaryInfo.max_splash_fan);

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

    @SuppressLint("DefaultLocale")
    static private String formatPercentage(int count, int total) {
        return total != 0 ? String.format("%.3f%%", count * 100.0f / total) : "0.000%";
    }

    @SuppressLint("DefaultLocale")
    static private String formatMean(int total, int count) {
        return count != 0 ? String.format("%.3f", (float)total / (float)count) : "0.000";
    }

}
