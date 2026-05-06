package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;

public class FlowLayout extends ViewGroup {
    private int mLineSpacing;
    private int mItemSpacing;
    private int mRowCount;

    public FlowLayout(@NonNull Context context) {
        this(context, (AttributeSet)null);
    }

    public FlowLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FlowLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getLineSpacing() {
        return this.mLineSpacing;
    }

    public void setLineSpacing(int lineSpacing) {
        this.mLineSpacing = lineSpacing;
    }

    public int getItemSpacing() {
        return this.mItemSpacing;
    }

    public void setItemSpacing(int itemSpacing) {
        this.mItemSpacing = itemSpacing;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        final int limitWidth = widthMode != Integer.MIN_VALUE && widthMode != MeasureSpec.EXACTLY ? Integer.MAX_VALUE : widthSize;

        final int paddingLeft = this.getPaddingLeft();
        final int paddingRight = this.getPaddingRight();
        final int paddingTop = this.getPaddingTop();
        final int paddingBottom = this.getPaddingBottom();

        int childLeft = paddingLeft;
        int childTop = paddingTop;
        int childBottom = childTop;
        int maxChildRight = 0, maxChildBottom = 0;
        int maxRight = limitWidth - paddingRight;

        int childCount = this.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            this.measureChild(child, widthMeasureSpec, heightMeasureSpec);
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            int leftMargin = 0, rightMargin = 0, topMargin = 0, bottomMargin = 0;
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)lp;
                leftMargin = mlp.leftMargin;
                rightMargin = mlp.rightMargin;
                topMargin = mlp.topMargin;
                bottomMargin = mlp.bottomMargin;
            }

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int childRight = childLeft + leftMargin + childWidth;
            if (childRight > maxRight) {
                childLeft = paddingLeft;
                childTop = childBottom + this.mLineSpacing;
            }

            childRight = childLeft + leftMargin + childWidth;
            childBottom = childTop + childHeight + topMargin + bottomMargin;
            if (childRight > maxChildRight) {
                maxChildRight = childRight;
            }
            if (childBottom > maxChildBottom) {
                maxChildBottom = childBottom;
            }

            childLeft += leftMargin + rightMargin + childWidth + this.mItemSpacing;
            if (i == childCount - 1) {
                maxChildRight += rightMargin;
            }
        }

        this.setMeasuredDimension(
                getMeasuredDimension(widthMode, widthSize, maxChildRight + paddingRight),
                getMeasuredDimension(heightMode, heightSize, maxChildBottom + paddingBottom));
    }

    private static int getMeasuredDimension(int mode, int size, int childrenEdge) {
        switch (mode) {
            case Integer.MIN_VALUE:
                return Math.min(childrenEdge, size);
            case MeasureSpec.EXACTLY:
                return size;
            default:
                return childrenEdge;
        }
    }

    @Override
    protected void onLayout(boolean sizeChanged, int left, int top, int right, int bottom) {
        if (this.getChildCount() == 0) {
            return;
        }

        this.mRowCount = 1;
        boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        int paddingStart = isRtl ? this.getPaddingRight() : this.getPaddingLeft();
        int paddingEnd = isRtl ? this.getPaddingLeft() : this.getPaddingRight();
        int childStart = paddingStart;
        int childTop = this.getPaddingTop();
        int childBottom = childTop;
        int maxChildEnd = right - left - paddingEnd;

        int childCount = this.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            ViewGroup.LayoutParams lp = child.getLayoutParams();
            int startMargin = 0, endMargin = 0, topMargin = 0, bottomMargin = 0;
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)lp;
                startMargin = MarginLayoutParamsCompat.getMarginStart(mlp);
                endMargin = MarginLayoutParamsCompat.getMarginEnd(mlp);
                topMargin = mlp.topMargin;
                bottomMargin = mlp.bottomMargin;
            }

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int childEnd = childStart + startMargin + childWidth;
            if (childEnd > maxChildEnd) {
                childStart = paddingStart;
                childTop = childBottom + this.mLineSpacing;
                ++this.mRowCount;
            }

            childEnd = childStart + startMargin + childWidth;
            childBottom = childTop + childHeight + topMargin + bottomMargin;
            if (isRtl) {
                child.layout(maxChildEnd - childEnd, childTop + topMargin, maxChildEnd - childStart - startMargin, childBottom - bottomMargin);
            } else {
                child.layout(childStart + startMargin, childTop + topMargin, childEnd, childBottom - bottomMargin);
            }

            childStart += startMargin + endMargin + childWidth + this.mItemSpacing;
        }

    }

    protected int getRowCount() {
        return this.mRowCount;
    }
}
