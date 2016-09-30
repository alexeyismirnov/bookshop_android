package com.rlc.bookshop;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;


public class AutofitRecyclerView extends RecyclerView {
    private StaggeredGridLayoutManager gridManager;
    private LinearLayoutManager listManager;
    String viewType;

    public AutofitRecyclerView(Context context) {
        super(context);
        init(context, null);
    }

    public AutofitRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AutofitRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        SharedPreferences prefs = ((Activity)getContext()).getPreferences(0);

        viewType = prefs.getString("view", "list");

        if (viewType.equals("list")) {
            listManager = new LinearLayoutManager((Activity)getContext());
            listManager.setReverseLayout(true);
            listManager.setStackFromEnd(true);
            setLayoutManager(listManager);

        } else {
            gridManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            setLayoutManager(gridManager);
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        if (viewType.equals("list"))
            return;

        int columnWidth = 150;

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        columnWidth = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, columnWidth, metrics );

        if (columnWidth > 0) {
            int spanCount = Math.max(1, getMeasuredWidth() / columnWidth);
            gridManager.setSpanCount(spanCount);
        }
    }
}
