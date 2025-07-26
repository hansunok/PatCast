package com.hansunok.petcast;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {
    private int lineSpacing = 10;

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int parentWidth = r - l;
        int x = getPaddingLeft();
        int y = getPaddingTop();
        int rowHeight = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            // ✅ 자식 뷰를 제한된 폭 내에서 측정 (EditText 폭 제한 필수)
            int childWidthSpec = MeasureSpec.makeMeasureSpec(parentWidth - getPaddingLeft() - getPaddingRight(), MeasureSpec.AT_MOST);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            child.measure(childWidthSpec, childHeightSpec);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (x + childWidth > parentWidth) {
                x = getPaddingLeft();
                y += rowHeight + lineSpacing;
                rowHeight = 0;
            }

            child.layout(x, y, x + childWidth, y + childHeight);
            x += childWidth + lineSpacing;
            rowHeight = Math.max(rowHeight, childHeight);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int x = getPaddingLeft();
        int y = getPaddingTop();
        int rowHeight = 0;
        int totalHeight = y;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            int childWidthSpec = MeasureSpec.makeMeasureSpec(parentWidth - getPaddingLeft() - getPaddingRight(), MeasureSpec.AT_MOST);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            child.measure(childWidthSpec, childHeightSpec);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (x + childWidth > parentWidth) {
                x = getPaddingLeft();
                totalHeight += rowHeight + lineSpacing;
                rowHeight = 0;
            }

            x += childWidth + lineSpacing;
            rowHeight = Math.max(rowHeight, childHeight);
        }

        totalHeight += rowHeight + getPaddingBottom();
        setMeasuredDimension(parentWidth, totalHeight);
    }
}
