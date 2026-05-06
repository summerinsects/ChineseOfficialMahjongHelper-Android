package net.tziakcha.chineseofficialmahjonghelper.training;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.R;

import java.util.ArrayList;

public final class TrainingHistoryManager {
    static public final class Result {
        public int state;
    }

    private final ArrayList<Result> mResults = new ArrayList<>();
    private final float mPaddingF;
    private final int mCountInLine;
    private final int mSideLength;
    private final RecyclerView mRecyclerView;
    private final LinearLayoutManager mLinearLayoutManager;

    public TrainingHistoryManager(Context context, RecyclerView recyclerView) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float dp5 = context.getResources().getDimension(R.dimen.dp5);
        final float dp28 = context.getResources().getDimension(R.dimen.dp28);
        mCountInLine = (int)Math.ceil((metrics.widthPixels - dp5) / (dp28 + dp5));
        final float reciprocal = 1.0f / mCountInLine;
        mSideLength = (int)((metrics.widthPixels - (mCountInLine + 1) * dp5) * reciprocal);
        mPaddingF = ((metrics.widthPixels - dp5) - (mSideLength * mCountInLine)) * reciprocal;

        mRecyclerView = recyclerView;
        mLinearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(mHistoryRecyclerViewAdapter);
    }

    public void addResult() {
        int rem = mResults.size() % mCountInLine;

        mResults.add(new Result());

        RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(0);
        if (rem != 0) {
            if (viewHolder != null) {
                ((HistoryRecyclerViewHolder)viewHolder).addResult(rem);
            }
        } else {
            if (viewHolder != null) {
                ((RecyclerView.LayoutParams)viewHolder.itemView.getLayoutParams()).topMargin
                        = (int)mPaddingF;
            }
            mHistoryRecyclerViewAdapter.notifyItemInserted(0);
        }
        mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
    }

    public void setResult(boolean correct) {
        int size = mResults.size();
        int rem = size % mCountInLine;

        Result result = mResults.get(size - 1);
        result.state = correct ? 1 : -1;

        RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(0);
        if (viewHolder != null) {
            ((HistoryRecyclerViewHolder)viewHolder).setResult(
                    rem > 0 ? rem - 1 : mCountInLine - 1, correct);
        }
    }

    public void reset() {
        int count = mHistoryRecyclerViewAdapter.getItemCount();

        mResults.clear();
        mHistoryRecyclerViewAdapter.notifyItemRangeRemoved(0, count);
    }

    private final class HistoryRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout[] mResultLayouts = new RelativeLayout[mCountInLine];
        private final TextView[] mResultTexts = new TextView[mCountInLine];

        public HistoryRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            init(itemView.getContext());
        }

        private void init(Context context) {
            RecyclerView.LayoutParams rvl = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(rvl);

            final int dp16 = context.getResources().getDimensionPixelSize(R.dimen.dp16);
            for (int i = 0; i < mCountInLine; ++i) {
                RelativeLayout rl = new RelativeLayout(context);
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(mSideLength, mSideLength);
                rlp.leftMargin = mSideLength * i + (int)(mPaddingF * i);
                rl.setLayoutParams(rlp);
                ((RelativeLayout)itemView).addView(rl);
                rl.setVisibility(View.GONE);
                mResultLayouts[i] = rl;

                rl.setBackgroundColor(Common.COLOR_BLUE);

                TextView tv = new TextView(context);
                RelativeLayout.LayoutParams rlp1 = new RelativeLayout.LayoutParams(mSideLength, mSideLength);
                tv.setLayoutParams(rlp1);
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp16);
                tv.setTextColor(Color.WHITE);
                rl.addView(tv);
                mResultTexts[i] = tv;
            }
        }

        public void setup(int idx) {
            int size = mResults.size();
            int rem = size % mCountInLine;
            int quot = (size - rem) / mCountInLine;

            int offset, count;

            // 不是首行，则是满排的
            if (idx != 0) {
                count = mCountInLine;
                offset = (quot - idx) * mCountInLine;
                ((RecyclerView.LayoutParams)itemView.getLayoutParams()).topMargin = (int)mPaddingF;
            } else {
                count = rem != 0 ? rem : mCountInLine;
                offset = size - count;
                ((RecyclerView.LayoutParams)itemView.getLayoutParams()).topMargin = 0;
            }

            for (int i = 0; i < count; ++i) {
                int state = mResults.get(offset + i).state;
                if (state > 0) {
                    mResultLayouts[i].setBackgroundColor(Common.COLOR_GREEN);
                    mResultTexts[i].setText("\u2713");
                } else if (state < 0) {
                    mResultLayouts[i].setBackgroundColor(Common.COLOR_RED);
                    mResultTexts[i].setText("\u2715");
                } else {
                    mResultLayouts[i].setBackgroundColor(Common.COLOR_BLUE);
                    mResultTexts[i].setText("");
                }
                mResultLayouts[i].setVisibility(View.VISIBLE);
            }

            for (int i = count; i < mCountInLine; ++i) {
                mResultLayouts[i].setVisibility(View.GONE);
            }
        }

        public void addResult(int which) {
            mResultLayouts[which].setBackgroundColor(Common.COLOR_BLUE);
            mResultTexts[which].setText("");
            mResultLayouts[which].setVisibility(View.VISIBLE);
        }

        public void setResult(int which, boolean correct) {
            if (correct) {
                mResultLayouts[which].setBackgroundColor(Common.COLOR_GREEN);
                mResultTexts[which].setText("\u2713");
            } else {
                mResultLayouts[which].setBackgroundColor(Common.COLOR_RED);
                mResultTexts[which].setText("\u2715");
            }
            mResultLayouts[which].setVisibility(View.VISIBLE);
        }
    }

    private final class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewHolder> {

        @NonNull
        @Override
        public HistoryRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new HistoryRecyclerViewHolder(new RelativeLayout(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryRecyclerViewHolder holder, int position) {
            holder.setup(position);
        }

        @Override
        public int getItemCount() {
            int size = mResults.size();
            int rem = size % mCountInLine;
            int quot = (size - rem) / mCountInLine;
            return quot + (rem != 0 ? 1 : 0);
        }
    }

    private final HistoryRecyclerViewAdapter mHistoryRecyclerViewAdapter = new HistoryRecyclerViewAdapter();

}
