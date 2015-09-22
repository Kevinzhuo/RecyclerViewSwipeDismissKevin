package com.kevin.kevinzhuo.mylibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
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
                float deltaY = event.getRawY() - mDownY;
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                float velocityY = mVelocityTracker.getYVelocity();
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(velocityY);
                boolean dismiss = false;
                boolean dismissRight = false;

                if (mIsVertical) {
                    if (Math.abs(deltaY) > mViewWidth / 2 && mSwiping) {
                        dismiss = true;
                        dismissRight = deltaY > 0;
                    } else if (mMinFlingVelocity <= absVelocityY && absVelocityY <= mMaxFlingVelocity && absVelocityX < absVelocityY && mSwiping) {
                        dismiss = (velocityY < 0) == (deltaY < 0);
                        dismissRight = mVelocityTracker.getYVelocity() > 0;
                    }

                    if (dismiss && mDownPosition != ListView.INVALID_POSITION) {
                        final View downView = mDownView;
                        final int downPosition = mDownPosition;
                        ++mDismissAnimationRefCount;
                        mDownView.animate().translationY(dismissRight ? mViewWidth : -mViewWidth).alpha(0).setDuration(mAnimationTime).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                performDismiss(downView, downPosition);
                            }
                        });
                    } else {
                        mDownView.animate().translationY(0).alpha(1).setDuration(mAnimationTime).setListener(null);
                    }
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    mDownX = 0;
                    mDownY = 0;
                    mDownView = null;
                    mDownPosition = ListView.INVALID_POSITION;
                    mSwiping = false;
                } else {
                    if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
                        dismiss = true;
                        dismissRight = deltaX > 0;
                    } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity && absVelocityY < absVelocityX && mSwiping) {
                        dismiss = (velocityX < 0) == (deltaX < 0);
                        dismissRight = mVelocityTracker.getXVelocity() > 0;
                    }

                    if (dismiss && mDownPosition != ListView.INVALID_POSITION) {
                        final View downView = mDownView;
                        final int downPosition = mDownPosition;
                        ++mDismissAnimationRefCount;
                        mDownView.animate()
                                .translationX(dismissRight ? mViewWidth : -mViewWidth)
                                .alpha(0)
                                .setDuration(mAnimationTime)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        performDismiss(downView, downPosition);
                                    }
                                });
                    } else {
                        // cancel
                        mDownView.animate()
                                .translationX(0)
                                .alpha(1)
                                .setDuration(mAnimationTime)
                                .setListener(null);
                    }
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    mDownX = 0;
                    mDownY = 0;
                    mDownView = null;
                    mDownPosition = ListView.INVALID_POSITION;
                    mSwiping = false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null || mPaused) {
                    break;
                }

                mVelocityTracker.addMovement(event);
                float deltaX = event.getRawX() - mDownX;
                float deltaY = event.getRawY() - mDownY;
                if (mIsVertical) {
                    if (Math.abs(deltaY) > mSlop && Math.abs(deltaX) < Math.abs(deltaY) / 2) {
                        mSwiping = true;
                        mSwipingSlop = (deltaY > 0 ? mSlop : -mSlop);
                        mRecyclerView.requestDisallowInterceptTouchEvent(true);
                        MotionEvent cancelEvent = MotionEvent.obtain(event);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        mRecyclerView.onTouchEvent(cancelEvent);
                        cancelEvent.recycle();
                    }

                    if (mSwiping) {
                        mDownView.setTranslationY(deltaY - mSwipingSlop);
                        mDownView.setAlpha(Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaY) / mViewWidth)));
                        return true;
                    }
                } else {
                    if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                        mSwiping = true;
                        mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
                        mRecyclerView.requestDisallowInterceptTouchEvent(true);

                        // Cancel ListView's touch (un-highlighting the item)
                        MotionEvent cancelEvent = MotionEvent.obtain(event);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                                (event.getActionIndex()
                                        << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        mRecyclerView.onTouchEvent(cancelEvent);
                        cancelEvent.recycle();
                    }

                    if (mSwiping) {
                        mDownView.setTranslationX(deltaX - mSwipingSlop);
                        mDownView.setAlpha(Math.max(0f, Math.min(1f,
                                1f - 2f * Math.abs(deltaX) / mViewWidth)));
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }

    private void performDismiss(final View dismissView, final int dismissPostion) {
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight;
        if (mIsVertical)
            originalHeight = dismissView.getWidth();
        else
            originalHeight = dismissView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                --mDismissAnimationRefCount;
                if (mDismissAnimationRefCount == 0) {
                    Collections.sort(mPendingDismisses);

                    int[] dismissPositions = new int[mPendingDismisses.size()];
                    for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
                        dismissPositions[i] = mPendingDismisses.get(i).position;
                    }
                    mCallbacks.onDismiss(dismissView);

                    mDownPosition = ListView.INVALID_POSITION;

                    ViewGroup.LayoutParams lp;
                    for (PendingDismissData pendingDismiss : mPendingDismisses) {
                        pendingDismiss.view.setAlpha(1f);
                        if (mIsVertical)
                            pendingDismiss.view.setTranslationY(0);
                        else
                            pendingDismiss.view.setTranslationX(0);
                        lp = pendingDismiss.view.getLayoutParams();
                        if (mIsVertical)
                            lp.width = originalHeight;
                        else
                            lp.height = originalHeight;

                        pendingDismiss.view.setLayoutParams(lp);
                    }

                    long time = SystemClock.uptimeMillis();
                    MotionEvent cancleEvent = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    mRecyclerView.dispatchTouchEvent(cancleEvent);

                    mPendingDismisses.clear();
                }
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mIsVertical)
                    lp.width = (int) animation.getAnimatedValue();
                else
                    lp.height = (int) animation.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

        mPendingDismisses.add(new PendingDismissData(dismissPostion, dismissView));
        animator.start();
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
