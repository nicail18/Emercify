package nicail.bscs.com.emercify.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import nicail.bscs.com.emercify.R;

public class RecyclerViewDivider extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

//    public RecyclerViewDivider(Context context) {
//        mDivider = context.getResources().getDrawable(R.drawable.line_divider);
//    }
//
//    @Override
//    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
//        int left = parent.getPaddingLeft();
//        int right = parent.getWidth() - parent.getPaddingRight();
//
//        int childCount = parent.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View child = parent.getChildAt(i);
//
//            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
//
//            int top = child.getBottom() + params.bottomMargin;
//            int bottom = top + mDivider.getIntrinsicHeight();
//
//            mDivider.setBounds(left, top, right, bottom);
//            mDivider.draw(c);
//        }
//    }

            public RecyclerViewDivider(Drawable divider) {
                mDivider = divider;
            }
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                if (parent.getChildAdapterPosition(view) == 0) {
                    return;
                }

                outRect.top = mDivider.getIntrinsicHeight();
            }
            @Override
            public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
                int dividerLeft = parent.getPaddingLeft();
                int dividerRight = parent.getWidth() - parent.getPaddingRight();

                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount - 1; i++) {
                    View child = parent.getChildAt(i);

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int dividerTop = child.getBottom() + params.bottomMargin;
                    int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();

                    mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
                    mDivider.draw(canvas);
                }
            }
}
