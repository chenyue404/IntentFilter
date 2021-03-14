package com.chenyue404.intentfilter.ui;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Administrator on 2016/4/13.
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int space = -1;
    private int left = -1;
    private int top = -1;
    private int right = -1;
    private int bottom = -1;

    public SpaceItemDecoration(int space) {
        this.space = space;
    }

    public SpaceItemDecoration(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (space != -1) {
            outRect.bottom = space;
            outRect.left = space / 2;
            outRect.right = space / 2;
        } else {
            if (left != -1) {
                outRect.left = left;
            }
            if (top != -1) {
                outRect.top = top;
            }
            if (right != -1) {
                outRect.right = right;
            }
            if (bottom != -1) {
                outRect.bottom = bottom;
            }
        }
    }
}
