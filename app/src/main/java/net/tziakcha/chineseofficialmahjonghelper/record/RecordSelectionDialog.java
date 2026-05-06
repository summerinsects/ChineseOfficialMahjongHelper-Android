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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class RecordSelectionDialog extends AlertDialog {
    public interface OnSubmitListener {
        void onSubmit(RecordSelectionDialog dialog, int count, boolean[] selections);
    }

    private final ArrayList<RecordItemInfo> mRecordItems;
    private final boolean[] mSelections;
    private final OnSubmitListener mOnSubmitListener;
    private int mSelectionCount = 0;
    private final String mTitle;
    private final String mConfirm;
    private CheckBox mAllCheck;
    private TextView mCountText;
    private RecyclerView mRecyclerView;
    private final SelectionRecyclerViewAdapter mSelectionRecyclerViewAdapter = new SelectionRecyclerViewAdapter();

    protected RecordSelectionDialog(@NonNull Context context, final ArrayList<RecordItemInfo> recordItems,
            String title, String confirm, OnSubmitListener listener) {
        super(context);
        mOnSubmitListener = listener;
        mRecordItems = recordItems;
        mSelections = new boolean[recordItems.size()];
        Arrays.fill(mSelections, false);
        mTitle = title;
        mConfirm = confirm;
    }

    @Override
    public void show() {
        super.show();

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_selection_layout, null);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText(mTitle);
        contentView.findViewById(R.id.ab_r_btn).setVisibility(View.GONE);
        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view -> onBackPressed());

        RecyclerView rv = contentView.findViewById(R.id.rll_rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(mSelectionRecyclerViewAdapter);
        mRecyclerView = rv;

        mAllCheck = contentView.findViewById(R.id.rll_cb_all);
        mAllCheck.setOnCheckedChangeListener(getToggleAllCallback());
        Utils.adaptCompoundButton(mAllCheck, getContext().getResources().getDimensionPixelSize(R.dimen.dp25));

        mCountText = contentView.findViewById(R.id.rll_txt_cnt);
        contentView.findViewById(R.id.rll_btn_cancel).setOnClickListener(view -> onBackPressed());

        Button button = contentView.findViewById(R.id.rll_btn_confirm);
        button.setText(mConfirm);
        button.setOnClickListener(view ->
                mOnSubmitListener.onSubmit(this, mSelectionCount, mSelections));

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

    private final class SelectionRecyclerViewHolder extends RecyclerView.ViewHolder {
        private int mIndex = -1;
        private final View mRoot;
        private final TextView mTitleText;
        private final TextView mTimeText;
        private final TextView[] mPlayerTexts = new TextView[4];
        private final CheckBox mCheckBox;

        public SelectionRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            mRoot = itemView.findViewById(R.id.rsi_ll);
            mTitleText = itemView.findViewById(R.id.rsi_txt_title);
            mTimeText = itemView.findViewById(R.id.rsi_txt_time);
            mPlayerTexts[0] = itemView.findViewById(R.id.rsi_txt_pl0);
            mPlayerTexts[1] = itemView.findViewById(R.id.rsi_txt_pl1);
            mPlayerTexts[2] = itemView.findViewById(R.id.rsi_txt_pl2);
            mPlayerTexts[3] = itemView.findViewById(R.id.rsi_txt_pl3);
            mCheckBox = itemView.findViewById(R.id.rsi_cb);
            mCheckBox.setOnCheckedChangeListener((view, checked) -> {
                if (mIndex != -1) {
                    setSelection(mIndex, checked);
                }
            });
            Utils.adaptCompoundButton(mCheckBox,
                    itemView.getContext().getResources().getDimensionPixelSize(R.dimen.dp25));

            itemView.setOnClickListener(view -> mCheckBox.performClick());
        }

        public void setup(int idx) {
            mIndex = idx;
            mRoot.setBackgroundColor(
                    (idx & 1) != 0
                            ? Color.argb(0x10, 0xc0, 0xc0, 0xc0)
                            : Color.argb(0x10, 0x10, 0x10, 0x10));

            RecordItemInfo item = mRecordItems.get(idx);
            mTitleText.setText(item.title);
            mTimeText.setText(item.time);

            final int match = item.match;
            final int[] seats = item.seats;
            for (int i = 0; i < 4; ++i) {
                mPlayerTexts[i].setText(item.players[i]);
                mPlayerTexts[i].setTextColor(
                        (match & (1 << seats[i])) == 0 ? Common.COLOR_GRAY : Common.COLOR_RED);
            }

            mCheckBox.setChecked(mSelections[idx]);
        }

        private void setChecked(boolean checked) {
            mCheckBox.setChecked(checked);
        }

    }

    private final class SelectionRecyclerViewAdapter extends RecyclerView.Adapter<SelectionRecyclerViewHolder> {

        @NonNull
        @Override
        public SelectionRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_selection_item_layout,
                    parent, false);
            return new SelectionRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SelectionRecyclerViewHolder holder, int position) {
            holder.setup(position);
        }

        @Override
        public int getItemCount() {
            return mRecordItems.size();
        }
    }

    private void setSelection(int index, boolean checked) {
        StringBuilder str = null;
        if (mSelections[index]) {  // 原本是选中
            if (!checked) {  // 取消选中
                mSelections[index] = false;
                --mSelectionCount;
                str = new StringBuilder();

                // 全选取消
                mAllCheck.setOnCheckedChangeListener(null);
                mAllCheck.setChecked(false);
                mAllCheck.setOnCheckedChangeListener(getToggleAllCallback());
            }
        } else {  // 原本是未选中
            if (checked) {  // 切换成选中
                mSelections[index] = true;
                ++mSelectionCount;
                str = new StringBuilder();

                // 已经事实上全选了
                if (mSelectionCount == mSelections.length) {
                    mAllCheck.setOnCheckedChangeListener(null);
                    mAllCheck.setChecked(true);
                    mAllCheck.setOnCheckedChangeListener(getToggleAllCallback());
                }
            }
        }

        if (str != null) {
            str.append(mSelectionCount).append('/').append(mSelections.length);
            mCountText.setText(str.toString());
        }
    }

    private CheckBox.OnCheckedChangeListener getToggleAllCallback() {
        return (view, checked) -> onToggleAllCheck(checked);
    }

    @SuppressLint("SetTextI18n")
    private void onToggleAllCheck(boolean checked) {
        if (checked) {  // 切换成全选
            // 已经是全选了
            if (mSelectionCount == mSelections.length) {
                return;
            }

            Arrays.fill(mSelections, true);
            mSelectionCount = mSelections.length;

            // 把所有可见的都打勾
            for (int i = 0, length = mSelections.length; i < length; ++i) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(i);
                if (viewHolder != null) {
                    ((SelectionRecyclerViewHolder)viewHolder).setChecked(true);
                }
            }

            mCountText.setText(mSelectionCount + "/" + mSelectionCount);
        } else {  // 切换成全不选
            // 已经是全不选了
            if (mSelectionCount == 0) {
                return;
            }

            Arrays.fill(mSelections, false);
            mSelectionCount = 0;

            // 把所有可见的都勾都去掉
            for (int i = 0, length = mSelections.length; i < length; ++i) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(i);
                if (viewHolder != null) {
                    ((SelectionRecyclerViewHolder) viewHolder).setChecked(false);
                }
            }

            mCountText.setText("0/" + mSelections.length);
        }

    }

}
