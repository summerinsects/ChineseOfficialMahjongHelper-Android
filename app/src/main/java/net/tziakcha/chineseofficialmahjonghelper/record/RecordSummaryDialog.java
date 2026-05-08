package net.tziakcha.chineseofficialmahjonghelper.record;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class RecordSummaryDialog extends AlertDialog {
    public interface OnSubmitListener {
        void onSubmit(int[] selections);
    }

    private final ArrayList<RecordItemInfo> mRecordItems;
    private final int[] mSelections;
    private final OnSubmitListener mOnSubmitListener;
    private int mSelectionCount;
    private TextView mCountText;
    private RecyclerView mRecyclerView;
    private final SummaryRecyclerViewAdapter mSelectionRecyclerViewAdapter = new SummaryRecyclerViewAdapter();

    protected RecordSummaryDialog(@NonNull Context context, final ArrayList<RecordItemInfo> recordItems,
            OnSubmitListener listener) {
        super(context);
        mOnSubmitListener = listener;
        mRecordItems = recordItems;
        mSelections = new int[recordItems.size()];
        Arrays.fill(mSelections, -1);
        mSelectionCount = 0;
    }

    @Override
    public void show() {
        super.show();

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_summary_layout, null);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("个人汇总");
        contentView.findViewById(R.id.ab_r_btn).setVisibility(View.GONE);
        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view -> onBackPressed());

        RecyclerView rv = contentView.findViewById(R.id.rul_rv);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(mSelectionRecyclerViewAdapter);
        mRecyclerView = rv;

        contentView.findViewById(R.id.rul_btn_clear).setOnClickListener(view -> onClearButton());

        mCountText = contentView.findViewById(R.id.rul_txt_cnt);
        contentView.findViewById(R.id.rul_btn_cancel).setOnClickListener(view -> onBackPressed());
        contentView.findViewById(R.id.rul_btn_confirm).setOnClickListener(view -> {
            if (mSelectionCount > 0) {
                mOnSubmitListener.onSubmit(mSelections);
            } else {
                Utils.showToastLong(getContext(), "请选择需要汇总的选手");
            }
        });

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);

            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0.0f;
            window.setAttributes(lp);

            // NOTE: 部分手机状态栏会黑变，需要通过以下代码改变
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
    }

    private final class SummaryRecyclerViewHolder extends RecyclerView.ViewHolder {
        private int mIndex = -1;
        private final TextView mTitleText;
        private final TextView mTimeText;
        private final RadioButton[] mPlayerRadios = new RadioButton[4];

        public SummaryRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.rti_txt_title);
            mTimeText = itemView.findViewById(R.id.rti_txt_time);
            mPlayerRadios[0] = itemView.findViewById(R.id.rti_rb_pl0);
            mPlayerRadios[1] = itemView.findViewById(R.id.rti_rb_pl1);
            mPlayerRadios[2] = itemView.findViewById(R.id.rti_rb_pl2);
            mPlayerRadios[3] = itemView.findViewById(R.id.rti_rb_pl3);
            itemView.findViewById(R.id.rti_btn_clear).setOnClickListener(view -> {
                if (mIndex != -1) {
                    setSelection(mIndex, -1);
                }
                clearSelection();
            });

            final int dp20 = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.dp20);
            for (int i = 0; i < 4; ++i) {
                final int idx = i;
                mPlayerRadios[i].setOnCheckedChangeListener((view, checked) -> {
                    if (checked) {
                        if (mIndex != -1) {
                            setSelection(mIndex, idx);
                        }
                        for (int k = 0; k < 4; ++k) {
                            if (k != idx) {
                                mPlayerRadios[k].setChecked(false);
                            }
                        }
                    }
                });
                Utils.adaptCompoundButton(mPlayerRadios[i], dp20);
            }
        }

        public void setup(int idx) {
            mIndex = idx;
            itemView.setBackgroundColor(
                    (idx & 1) != 0
                            ? Color.argb(0x10, 0xc0, 0xc0, 0xc0)
                            : Color.argb(0x10, 0x10, 0x10, 0x10));

            RecordItemInfo item = mRecordItems.get(idx);
            mTitleText.setText(item.title);
            mTimeText.setText(item.time);

            RecordInfo record = item.record;
            final int match = item.match;
            for (int i = 0; i < 4; ++i) {
                mPlayerRadios[i].setText(record.names[i]);
                mPlayerRadios[i].setChecked(mSelections[idx] == i);
                mPlayerRadios[i].setTextColor(
                        (match & (1 << i)) == 0 ? Common.COLOR_GRAY : Common.COLOR_RED);
            }
        }

        public void clearSelection() {
            for (int i = 0; i < 4; ++i) {
                mPlayerRadios[i].setChecked(false);
            }
        }
    }

    private final class SummaryRecyclerViewAdapter extends RecyclerView.Adapter<SummaryRecyclerViewHolder> {

        @NonNull
        @Override
        public SummaryRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_summary_item_layout,
                    parent, false);
            return new SummaryRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SummaryRecyclerViewHolder holder, int position) {
            holder.setup(position);
        }

        @Override
        public int getItemCount() {
            return mRecordItems.size();
        }
    }

    private void setSelection(int index, int which) {
        StringBuilder str = null;
        if (mSelections[index] == -1) {
            if (which != -1) {
                mSelections[index] = which;
                ++mSelectionCount;
                str = new StringBuilder();
            }
        } else {
            if (which == -1) {
                mSelections[index] = -1;
                --mSelectionCount;
                str = new StringBuilder();
            }
        }

        if (str != null) {
            str.append(mSelectionCount).append('/').append(mSelections.length);
            mCountText.setText(str.toString());
        }
    }

    @SuppressLint("SetTextI18n")
    private void onClearButton() {
        if (mSelectionCount != 0) {
            Arrays.fill(mSelections, -1);
            mSelectionCount = 0;

            for (int i = 0, length = mSelections.length; i < length; ++i) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(i);
                if (viewHolder != null) {
                    ((SummaryRecyclerViewHolder) viewHolder).clearSelection();
                }
            }

            mCountText.setText("0/" + mSelections.length);
        }
    }

}
