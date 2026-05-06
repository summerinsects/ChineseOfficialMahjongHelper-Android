package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import net.tziakcha.chineseofficialmahjonghelper.R;

import java.util.ArrayList;

public class MarqueeNoticeView extends RecyclerView {

    private final ArrayList<CharSequence> mMarqueeList = new ArrayList<>();

    public MarqueeNoticeView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MarqueeNoticeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MarqueeNoticeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LinearLayoutManager llm = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        setLayoutManager(llm);
        setAdapter(new MarqueeRecyclerViewAdapter());
    }

    public void setNoticeList(ArrayList<String> list) {
        for (String str : list) {
            mMarqueeList.add(Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY));
        }
    }

    public void startScroll() {
        LinearLayoutManager llm = (LinearLayoutManager)getLayoutManager();
        if (llm == null) {
            return;
        }

        LinearSmoothScroller lss = new LinearSmoothScroller(getContext()) {
            @Override
            protected int getHorizontalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics metrics) {
                return 500 / getContext().getResources().getDimension(R.dimen.dp16);
            }
        };
        lss.setTargetPosition(Integer.MAX_VALUE);
        llm.startSmoothScroll(lss);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return false;
    }

    private final static class MarqueeViewHolder extends RecyclerView.ViewHolder {
        public MarqueeViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private final class MarqueeRecyclerViewAdapter extends RecyclerView.Adapter<MarqueeViewHolder> {

        @NonNull
        @Override
        public MarqueeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    parent.getContext().getResources().getDimension(R.dimen.dp14));
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setSingleLine();
            textView.setEllipsize(null);
            ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            DisplayMetrics metrics = parent.getContext().getResources().getDisplayMetrics();
            mlp.leftMargin = metrics.widthPixels / 2;
            textView.setLayoutParams(mlp);
            return new MarqueeViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull MarqueeViewHolder holder, int position) {
            if (!mMarqueeList.isEmpty()) {
                TextView textView = (TextView)holder.itemView;
                textView.setText(mMarqueeList.get(position % mMarqueeList.size()));
            }
        }

        @Override
        public int getItemCount() {
            return mMarqueeList.isEmpty() ? 0 : Integer.MAX_VALUE;
        }
    }
}
