package com.kevin.kevinzhuo.mylibrary;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SwipeDismissRecyclerView implements View.OnTouchListener {

    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    private RecyclerView mRecyclerView;
    private DismissCallbacls mCallbacks;
    private boolean mIsVertical;
    private OnItemTouchCallBack mItemTouchCallback;

    private int mViewWidth = 1;

    private List<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();
    private int mDismissAnimationRefCount = 0;
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private int mDownPosition;
    private View mDownView;
    private boolean mPaused;

    public SwipeDismissRecyclerView(Builder builder) {
        ViewConfiguration vc = ViewConfiguration.get(builder.mRecyclerView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = builder.mRecyclerView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        mRecyclerView = builder.mRecyclerView;
        mCallbacks = builder.mCallbacks;
        mIsVertical = builder.mIsVertical;
        mItemTouchCallback = builder.mItemTouchCallBack;
    }

    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mViewWidth < 2) {
            mViewWidth = mIsVertical ? mRecyclerView.getHeight() : mRecyclerView.getWidth();
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mPaused) {
                    return false;
                }

                Rect rect = new Rect();
                int childCount = mRecyclerView.getChildCount();
                int[] listViewCoords = new int[2];
                mRecyclerView.getLocationOnScreen(listViewCoords);
                int x = (int) (event.getRawX() - listViewCoords[0]);
                int y = (int) (event.getRawY() - listViewCoords[1]);
                View child;

                mDownView = mRecyclerView.findChildViewUnder(x, y);

                if (mDownView != null) {
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();

                    mDownPosition = mRecyclerView.getChildPosition(mDownView);
                    if (mCallbacks.canDismiss(mDownPosition)) {
                        mVelocityTracker = VelocityTracker.obtain();
                        mVelocityTracker.addMovement(event);
                    } else {
                        mDownView = null;
                    }
                }
                return false;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    break;
                }

                if (mDownView != null && mSwiping) {
                    if (mIsVertical) {
                        mDownView.animate().translationY(0).alpha(1).setDuration(mAnimationTime).setListener(null);
                    } else {
                        mDownView.animate().translationX(0).alpha(1).setDuration(mAnimationTime).setListener(null);
                    }
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = RecyclerView.NO_POSITION;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (!mSwiping && mDownView != null && mItemTouchCallback != null) {
                    mItemTouchCallback.onTouch(mRecyclerView.getChildPosition(mDownView));
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    mDownX = 0;
                    mDownY = 0;
                    mDownView = null;
                    mDownPosition = ListView.INVALID_POSITION;
                    mSwiping = false;
                    return true;
                }

                if (mVelocityTracker == null) {
                    break;
                }

                float deltaX = event.getRawX() - mDownX;
            }
        }
    }

    public interface DismissCallbacls {

        boolean canDismiss(int position);

        void onDismiss(View view);
    }

    public interface OnItemTouchCallBack {
        void onTouch(int index);
    }

    static public class Builder {
        private RecyclerView mRecyclerView;
        private DismissCallbacls mCallbacks;

        private OnItemTouchCallBack mItemTouchCallBack = null;
        private boolean mIsVertical = false;

        public Builder(RecyclerView recyclerView, DismissCallbacls callbacls) {
            mRecyclerView = recyclerView;
            mCallbacks = callbacls;
        }

        public Builder setIsVertical(boolean isVertical) {
            mIsVertical = isVertical;
            return this;
        }

        public Builder setItemTouchCallback(OnItemTouchCallBack callBack) {
            mItemTouchCallBack = callBack;
            return this;
        }

        public SwipeDismissRecyclerView create() {
            return new SwipeDismissRecyclerView(this);
        }

    }

    class PendingDismissData implements Comparable<PendingDismissData> {
        public int position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            return other.position - position;
        }
    }

}
