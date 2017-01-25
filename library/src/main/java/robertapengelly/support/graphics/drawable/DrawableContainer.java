package robertapengelly.support.graphics.drawable;

import  android.annotation.TargetApi;
import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.Resources.Theme;
import  android.graphics.Canvas;
import  android.graphics.ColorFilter;
import  android.graphics.Outline;
import  android.graphics.PixelFormat;
import  android.graphics.Rect;
import  android.graphics.PorterDuff.Mode;
import  android.graphics.drawable.Drawable;
import  android.os.Build;
import  android.os.SystemClock;
import  android.util.LayoutDirection;

/**
 * A helper class that contains several {@link LollipopDrawable}s and selects which one to use.
 *
 * You can subclass it to create your own DrawableContainers or directly use one its child classes.
 */
public class DrawableContainer extends LollipopDrawable implements Drawable.Callback {

    /**
     * To be proper, we should have a getter for dither (and alpha, etc.)
     * so that proxy classes like this can save/restore their delegates'
     * values, but we don't have getters. Since we do have setters
     * (e.g. setDither), which this proxy forwards on, we have to have some
     * default/initial setting.
     *
     * The initial setting for dither is now true, since it almost always seems
     * to improve the quality at negligible cost.
     */
    private static final boolean DEFAULT_DITHER = true;
    
    /** Whether setAlpha() has been called at least once. */
    private boolean mHasAlpha;
    
    private boolean mMutated;
    
    private int mAlpha = 0xFF;
    private int mCurIndex = -1;
    
    private Drawable mCurrDrawable;
    private DrawableContainerState mDrawableContainerState;
    private Rect mHotspotBounds;
    
    // Animations.
    private long mEnterAnimationEnd;
    private long mExitAnimationEnd;
    private Drawable mLastDrawable;
    private Runnable mAnimationRunnable;
    
    void animate(boolean schedule) {
    
        mHasAlpha = true;
        
        boolean animating = false;
        final long now = SystemClock.uptimeMillis();
        
        if (mCurrDrawable != null) {
        
            if (mEnterAnimationEnd != 0) {
            
                if (mEnterAnimationEnd <= now) {
                
                    mCurrDrawable.mutate().setAlpha(mAlpha);
                    mEnterAnimationEnd = 0;
                
                } else {
                
                    int animAlpha = ((int) ((mEnterAnimationEnd - now) * 255) / mDrawableContainerState.mEnterFadeDuration);
                    
                    animating = true;
                    mCurrDrawable.mutate().setAlpha(((255 - animAlpha) * mAlpha) / 255);
                
                }
            
            }
        
        } else
            mEnterAnimationEnd = 0;
        
        if (mLastDrawable != null) {
        
            if (mExitAnimationEnd != 0) {
            
                if (mExitAnimationEnd <= now) {
                
                    mLastDrawable.setVisible(false, false);
                    
                    mExitAnimationEnd = 0;
                    mLastDrawable = null;
                
                } else {
                
                    int animAlpha = ((int) ((mExitAnimationEnd - now) * 255) / mDrawableContainerState.mExitFadeDuration);
                    
                    animating = true;
                    mLastDrawable.mutate().setAlpha((animAlpha * mAlpha) / 255);
                
                }
            
            }
        
        } else
            mExitAnimationEnd = 0;
        
        if (schedule && animating)
            scheduleSelf(mAnimationRunnable, (now + 1000 / 60));
    
    }
    
    @Override
    public void applyTheme(Theme theme) {
        mDrawableContainerState.applyTheme(theme);
    }
    
    @Override
    public boolean canApplyTheme() {
        return mDrawableContainerState.canApplyTheme();
    }
    
    @Override
    public void draw(Canvas canvas) {
    
        if (mCurrDrawable != null)
            mCurrDrawable.draw(canvas);
        
        if (mLastDrawable != null)
            mLastDrawable.draw(canvas);
    
    }
    
    @Override
    public int getAlpha() {
        return mAlpha;
    }
    
    @Override
    public int getChangingConfigurations() {
        return (super.getChangingConfigurations()
            | mDrawableContainerState.mChangingConfigurations
                | mDrawableContainerState.mChildrenChangingConfigurations);
    }
    
    @Override
    public ConstantState getConstantState() {
    
        if (mDrawableContainerState.canConstantState()) {
        
            mDrawableContainerState.mChangingConfigurations = getChangingConfigurations();
            return mDrawableContainerState;
        
        }
        
        return null;
    
    }
    
    @Override
    public Drawable getCurrent() {
        return mCurrDrawable;
    }
    
    public int getCurrentIndex() {
        return mCurIndex;
    }
    
    @Override
    public void getHotspotBounds(Rect outRect) {
    
        if (mHotspotBounds != null)
            outRect.set(mHotspotBounds);
        else
            super.getHotspotBounds(outRect);
    
    }
    
    @Override
    public int getIntrinsicHeight() {
    
        if (mDrawableContainerState.isConstantSize())
            return mDrawableContainerState.getConstantHeight();
        
        return ((mCurrDrawable != null) ? mCurrDrawable.getIntrinsicHeight() : -1);
    
    }
    
    @Override
    public int getIntrinsicWidth() {
    
        if (mDrawableContainerState.isConstantSize())
            return mDrawableContainerState.getConstantWidth();
        
        return ((mCurrDrawable != null) ? mCurrDrawable.getIntrinsicWidth() : -1);
    
    }
    
    @Override
    public int getMinimumHeight() {
    
        if (mDrawableContainerState.isConstantSize())
            return mDrawableContainerState.getConstantMinimumHeight();
        
        return ((mCurrDrawable != null) ? mCurrDrawable.getMinimumHeight() : 0);
    
    }
    
    @Override
    public int getMinimumWidth() {
    
        if (mDrawableContainerState.isConstantSize())
            return mDrawableContainerState.getConstantMinimumWidth();
        
        return ((mCurrDrawable != null) ? mCurrDrawable.getMinimumWidth() : 0);
    
    }
    
    @Override
    public int getOpacity() {
        //noinspection WrongConstant
        return (((mCurrDrawable == null) || (!mCurrDrawable.isVisible())) ? PixelFormat.TRANSPARENT :
                mDrawableContainerState.getOpacity());
    }
    
    @Override
    @TargetApi(21)
    public void getOutline(Outline outline) {
    
        if (mCurrDrawable != null)
            mCurrDrawable.getOutline(outline);
    
    }
    
    @Override
    public boolean getPadding(Rect padding) {
    
        boolean result;
        final Rect r = mDrawableContainerState.getConstantPadding();
        
        if (r != null) {
        
            padding.set(r);
            result = ((r.left | r.top | r.bottom | r.right) != 0);
        
        } else {
        
            if (mCurrDrawable != null)
                result = mCurrDrawable.getPadding(padding);
            else
                result = super.getPadding(padding);
        
        }
        
        if (needsMirroring()) {
        
            final int left = padding.left;
            final int right = padding.right;
            
            padding.left = right;
            padding.right = left;
        
        }
        
        return result;
    
    }
    
    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }
    
    @Override
    public boolean isAutoMirrored() {
        return mDrawableContainerState.mAutoMirrored;
    }
    
    @Override
    public boolean isStateful() {
        return mDrawableContainerState.isStateful();
    }
    
    @Override
    public void jumpToCurrentState() {
    
        boolean changed = false;
        
        if (mLastDrawable != null) {
        
            if (mLastDrawable instanceof LollipopDrawable)
                //noinspection RedundantCast
                ((LollipopDrawable) mLastDrawable).jumpToCurrentState();
            else if (Build.VERSION.SDK_INT >= 11)
                mLastDrawable.jumpToCurrentState();
            
            changed = true;
            mLastDrawable = null;
        
        }
        
        if (mCurrDrawable != null) {
        
            if (mCurrDrawable instanceof LollipopDrawable)
                //noinspection RedundantCast
                ((LollipopDrawable) mCurrDrawable).jumpToCurrentState();
            else if (Build.VERSION.SDK_INT >= 11)
                mCurrDrawable.jumpToCurrentState();
            
            if (mHasAlpha)
                mCurrDrawable.mutate().setAlpha(mAlpha);
        
        }
        
        if (mExitAnimationEnd != 0) {
        
            mExitAnimationEnd = 0;
            changed = true;
        
        }
        
        if (mEnterAnimationEnd != 0) {
        
            mEnterAnimationEnd = 0;
            changed = true;
        
        }
        
        if (changed)
            invalidateSelf();
    
    }
    
    @Override
    public Drawable mutate() {
    
        if (!mMutated && (super.mutate() == this)) {
        
            mDrawableContainerState.mutate();
            mMutated = true;
        
        }
        
        return this;
    
    }
    
    private boolean needsMirroring() {
    
        if (isAutoMirrored())
            if (Build.VERSION.SDK_INT >= 23)
                //noinspection WrongConstant
                return (getLayoutDirection() == LayoutDirection.RTL);
        
        return false;
    
    }
    
    @Override
    protected void onBoundsChange(Rect bounds) {
    
        if (mLastDrawable != null)
            mLastDrawable.setBounds(bounds);
        
        if (mCurrDrawable != null)
            mCurrDrawable.setBounds(bounds);
    
    }
    
    @Override
    protected boolean onLevelChange(int level) {
    
        if (mLastDrawable != null)
            return mLastDrawable.setLevel(level);
        
        if (mCurrDrawable != null)
            return mCurrDrawable.setLevel(level);
        
        return false;
    
    }
    
    @Override
    protected boolean onStateChange(int[] state) {
    
        if (mLastDrawable != null)
            return mLastDrawable.setState(state);
        
        if (mCurrDrawable != null)
            return mCurrDrawable.setState(state);
        
        return false;
    
    }
    
    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }
    
    public boolean selectDrawable(int idx) {
    
        if (idx == mCurIndex)
            return false;
        
        final long now = SystemClock.uptimeMillis();
        
        if (mDrawableContainerState.mExitFadeDuration > 0) {
        
            if (mLastDrawable != null)
                mLastDrawable.setVisible(false, false);
            
            if (mCurrDrawable != null) {
            
                mLastDrawable = mCurrDrawable;
                mExitAnimationEnd = (now + mDrawableContainerState.mExitFadeDuration);
            
            } else {
            
                mExitAnimationEnd = 0;
                mLastDrawable = null;
            
            }
        
        } else if (mCurrDrawable != null)
            mCurrDrawable.setVisible(false, false);
        
        if ((idx >= 0) && (idx < mDrawableContainerState.mNumChildren)) {
        
            final Drawable d = mDrawableContainerState.getChild(idx);
            
            mCurrDrawable = d;
            mCurIndex = idx;
            
            if (d != null) {
            
                d.mutate();
                
                if (mDrawableContainerState.mEnterFadeDuration > 0)
                    mEnterAnimationEnd = now + mDrawableContainerState.mEnterFadeDuration;
                else if (mHasAlpha)
                    d.setAlpha(mAlpha);
                
                if (mDrawableContainerState.mHasColorFilter)
                    // Color filter always overrides tint.
                    d.setColorFilter(mDrawableContainerState.mColorFilter);
                else {
                
                    if (mDrawableContainerState.mHasTintList) {
                    
                        if (d instanceof LollipopDrawable)
                            //noinspection RedundantCast
                            ((LollipopDrawable) d).setTintList(mDrawableContainerState.mTintList);
                        else if (Build.VERSION.SDK_INT >= 21)
                            d.setTintList(mDrawableContainerState.mTintList);
                    
                    }
                    
                    if (mDrawableContainerState.mHasTintMode) {
                    
                        if (d instanceof LollipopDrawable)
                            //noinspection RedundantCast
                            ((LollipopDrawable) d).setTintMode(mDrawableContainerState.mTintMode);
                        else if (Build.VERSION.SDK_INT >= 21)
                            d.setTintMode(mDrawableContainerState.mTintMode);
                    
                    }
                
                }
                
                d.setBounds(getBounds());
                d.setLevel(getLevel());
                d.setState(getState());
                d.setVisible(isVisible(), true);
                
                //noinspection deprecation
                d.setDither(mDrawableContainerState.mDither);
                
                if (Build.VERSION.SDK_INT >= 19)
                    d.setAutoMirrored(mDrawableContainerState.mAutoMirrored);
                
                if (Build.VERSION.SDK_INT >= 23)
                    d.setLayoutDirection(getLayoutDirection());
                
                final Rect hotspotBounds = mHotspotBounds;
                
                if (hotspotBounds != null) {
                
                    if (d instanceof LollipopDrawable)
                        //noinspection RedundantCast
                        ((LollipopDrawable) d).setHotspotBounds(hotspotBounds.left, hotspotBounds.top,
                            hotspotBounds.right, hotspotBounds.bottom);
                    else if (Build.VERSION.SDK_INT >= 21)
                        d.setHotspotBounds(hotspotBounds.left, hotspotBounds.top,
                            hotspotBounds.right, hotspotBounds.bottom);
                
                }
            
            }
        
        } else {
        
            mCurrDrawable = null;
            mCurIndex = -1;
        
        }
        
        if ((mEnterAnimationEnd != 0) || (mExitAnimationEnd != 0)) {
        
            if (mAnimationRunnable == null) {
            
                mAnimationRunnable = new Runnable() {
                
                    @Override
                    public void run() {
                    
                        animate(true);
                        invalidateSelf();
                    
                    }
                
                };
            
            } else
                unscheduleSelf(mAnimationRunnable);
            
            // Compute first frame and schedule next animation.
            animate(true);
        
        }
        
        invalidateSelf();
        return true;
    
    }
    
    @Override
    public void setAlpha(int alpha) {
    
        if (!mHasAlpha || (mAlpha != alpha)) {
        
            mAlpha = alpha;
            mHasAlpha = true;
            
            if (mCurrDrawable != null) {
            
                if (mEnterAnimationEnd == 0)
                    mCurrDrawable.mutate().setAlpha(alpha);
                else
                    animate(false);
            
            }
        
        }
    
    }
    
    @Override
    public void setAutoMirrored(boolean mirrored) {
    
        if (Build.VERSION.SDK_INT < 19)
            return;
        
        if (mDrawableContainerState.mAutoMirrored != mirrored) {
        
            mDrawableContainerState.mAutoMirrored = mirrored;
            
            if (mCurrDrawable != null)
                mCurrDrawable.mutate().setAutoMirrored(mDrawableContainerState.mAutoMirrored);
        
        }
    
    }
    
    @Override
    public void setColorFilter(ColorFilter cf) {
    
        mDrawableContainerState.mHasColorFilter = (cf != null);
        
        if (mDrawableContainerState.mColorFilter != cf) {
        
            mDrawableContainerState.mColorFilter = cf;
            
            if (mCurrDrawable != null)
                mCurrDrawable.mutate().setColorFilter(cf);
        
        }
    
    }
    
    protected void setConstantState(DrawableContainerState state) {
        mDrawableContainerState = state;
    }
    
    public void setCurrentIndex(int index) {
        selectDrawable(index);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void setDither(boolean dither) {
    
        if (mDrawableContainerState.mDither != dither) {
        
            mDrawableContainerState.mDither = dither;
            
            if (mCurrDrawable != null)
                mCurrDrawable.mutate().setDither(mDrawableContainerState.mDither);
        
        }
    
    }
    
    /**
     * Change the global fade duration when a new drawable is entering
     * the scene.
     *
     * @param ms The amount of time to fade in milliseconds.
     */
    public void setEnterFadeDuration(int ms) {
        mDrawableContainerState.mEnterFadeDuration = ms;
    }
    
    /**
     * Change the global fade duration when a new drawable is leaving
     * the scene.
     *
     * @param ms The amount of time to fade in milliseconds.
     */
    public void setExitFadeDuration(int ms) {
        mDrawableContainerState.mExitFadeDuration = ms;
    }
    
    @Override
    public void setHotspot(float x, float y) {
    
        if (mCurrDrawable != null) {
        
            if (mCurrDrawable instanceof LollipopDrawable)
                //noinspection RedundantCast
                ((LollipopDrawable) mCurrDrawable).setHotspot(x, y);
            else if (Build.VERSION.SDK_INT >= 21)
                mCurrDrawable.setHotspot(x, y);
        
        }
    
    }
    
    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
    
        if (mHotspotBounds == null)
            mHotspotBounds = new Rect(left, top, right, bottom);
        else
            mHotspotBounds.set(left, top, right, bottom);
        
        if (mCurrDrawable != null) {
        
            if (mCurrDrawable instanceof LollipopDrawable)
                //noinspection RedundantCast
                ((LollipopDrawable) mCurrDrawable).setHotspotBounds(left, top, right, bottom);
            else if (Build.VERSION.SDK_INT >= 21)
                mCurrDrawable.setHotspotBounds(left, top, right, bottom);
        
        }
    
    }
    
    @Override
    public void setTintList(ColorStateList tint) {
    
        mDrawableContainerState.mHasTintList = true;
        
        if (mDrawableContainerState.mTintList != tint) {
        
            mDrawableContainerState.mTintList = tint;
            
            if (mCurrDrawable != null) {
            
                if (mCurrDrawable instanceof LollipopDrawable)
                    //noinspection RedundantCast
                    ((LollipopDrawable) mCurrDrawable).setTintList(tint);
                else if (Build.VERSION.SDK_INT >= 21)
                    mCurrDrawable.mutate().setTintList(tint);
            
            }
        
        }
    
    }
    
    @Override
    public void setTintMode(Mode tintMode) {
    
        mDrawableContainerState.mHasTintMode = true;
        
        if (mDrawableContainerState.mTintMode != tintMode) {
        
            mDrawableContainerState.mTintMode = tintMode;
            
            if (mCurrDrawable != null) {
            
                if (mCurrDrawable instanceof LollipopDrawable)
                    //noinspection RedundantCast
                    ((LollipopDrawable) mCurrDrawable).setTintMode(tintMode);
                else if (Build.VERSION.SDK_INT >= 21)
                    mCurrDrawable.mutate().setTintMode(tintMode);
            
            }
        
        }
    
    }
    
    @Override
    public boolean setVisible(boolean visible, boolean restart) {
    
        boolean changed = super.setVisible(visible, restart);
        
        if (mLastDrawable != null)
            mLastDrawable.setVisible(visible, restart);
        
        if (mCurrDrawable != null)
            mCurrDrawable.setVisible(visible, restart);
        
        
        return changed;
    
    }
    
    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }
    
    /**
     * A ConstantState that can contain several {@link Drawable}s.
     *
     * This class was made public to enable testing, and its visibility may change in a future
     * release.
     */
    public abstract static class DrawableContainerState extends ConstantState {
    
        final DrawableContainer mOwner;
        final Resources mRes;
        
        boolean mAutoMirrored;
        boolean mCanConstantState;
        boolean mCheckedConstantState;
        boolean mCheckedOpacity;
        boolean mCheckedStateful;
        boolean mComputedConstantSize;
        boolean mConstantSize;
        boolean mDither = DEFAULT_DITHER;
        boolean mHasColorFilter;
        boolean mHasTintList;
        boolean mHasTintMode;
        boolean mMutated;
        boolean mPaddingChecked;
        boolean mStateful;
        boolean mVariablePadding;
        
        int mChangingConfigurations;
        int mChildrenChangingConfigurations;
        int mConstantHeight;
        int mConstantMinimumHeight;
        int mConstantMinimumWidth;
        int mConstantWidth;
        int mEnterFadeDuration;
        int mExitFadeDuration;
        int mLayoutDirection;
        int mNumChildren;
        int mOpacity;
        
        ColorFilter mColorFilter;
        ColorStateList mTintList;
        Drawable[] mDrawables;
        Mode mTintMode;
        SparseArrayCompat<ConstantStateFuture> mDrawableFutures;
        Rect mConstantPadding;
        Theme mTheme;
        
        DrawableContainerState(DrawableContainerState orig, DrawableContainer owner, Resources res) {
        
            mOwner = owner;
            mRes = res;
            
            if (orig != null) {
            
                mAutoMirrored = orig.mAutoMirrored;
                mCanConstantState = true;
                mChangingConfigurations = orig.mChangingConfigurations;
                mCheckedConstantState = true;
                mChildrenChangingConfigurations = orig.mChildrenChangingConfigurations;
                mColorFilter = orig.mColorFilter;
                mConstantSize = orig.mConstantSize;
                mDither = orig.mDither;
                mEnterFadeDuration = orig.mEnterFadeDuration;
                mExitFadeDuration = orig.mExitFadeDuration;
                mHasColorFilter = orig.mHasColorFilter;
                mHasTintList = orig.mHasTintList;
                mHasTintMode = orig.mHasTintMode;
                mLayoutDirection = orig.mLayoutDirection;
                mMutated = orig.mMutated;
                mTintList = orig.mTintList;
                mTintMode = orig.mTintMode;
                mVariablePadding = orig.mVariablePadding;
                
                // Cloning the following values may require creating futures.
                mCheckedOpacity = true;
                mCheckedStateful = true;
                mComputedConstantSize = true;
                mConstantHeight = orig.getConstantHeight();
                mConstantMinimumHeight = orig.getConstantMinimumHeight();
                mConstantMinimumWidth = orig.getConstantMinimumWidth();
                mConstantPadding = orig.getConstantPadding();
                mConstantWidth = orig.getConstantWidth();
                mOpacity = orig.getOpacity();
                mPaddingChecked = true;
                mStateful = orig.isStateful();
                
                // Postpone cloning children and futures until we're absolutely
                // sure that we're done computing values for the original state.
                final Drawable[] origDr = orig.mDrawables;
                
                mDrawables = new Drawable[origDr.length];
                mNumChildren = orig.mNumChildren;
                
                final SparseArrayCompat<ConstantStateFuture> origDf = orig.mDrawableFutures;
                
                if (origDf != null)
                    mDrawableFutures = origDf.clone();
                else
                    mDrawableFutures = new SparseArrayCompat<> (mNumChildren);
                
                final int N = mNumChildren;
                
                for (int i = 0; i < N; ++i)
                    if (origDr[i] != null)
                        mDrawableFutures.put(i, new ConstantStateFuture(origDr[i]));
            
            } else {
            
                mDrawables = new Drawable[10];
                mNumChildren = 0;
            
            }
        
        }
        
        public final int addChild(Drawable dr) {
        
            final int pos = mNumChildren;
            
            if (pos >= mDrawables.length)
                growArray(pos, (pos + 10));
            
            dr.setCallback(mOwner);
            dr.setVisible(false, true);
            
            mCheckedOpacity = false;
            mCheckedStateful = false;
            mChildrenChangingConfigurations |= dr.getChangingConfigurations();
            mComputedConstantSize = false;
            mConstantPadding = null;
            mDrawables[pos] = dr;
            mNumChildren++;
            mPaddingChecked = false;
            
            return pos;
        
        }
        
        final void applyTheme(Theme theme) {
        
            // No need to call createAllFutures, since future drawables will
            // apply the theme when they are prepared.
            
            final Drawable[] drawables = mDrawables;
            final int N = mNumChildren;
            
            for (int i = 0; i < N; ++i) {
            
                final Drawable d = drawables[i];
                
                if (drawables[i] != null) {
                
                    if (d instanceof LollipopDrawable)
                        //noinspection RedundantCast
                        ((LollipopDrawable) d).applyTheme(theme);
                    else if (Build.VERSION.SDK_INT >= 21)
                        d.applyTheme(theme);
                
                }
            
            }
            
            mTheme = theme;
        
        }
        
        @Override
        public boolean canApplyTheme() {
        
            final Drawable[] drawables = mDrawables;
            final int N = mNumChildren;
            
            for (int i = 0; i < N; ++i) {
            
                final Drawable d = drawables[i];
                
                if (d != null) {
                
                    if (d instanceof LollipopDrawable)
                        //noinspection RedundantCast
                        if (((LollipopDrawable) d).canApplyTheme())
                            return true;
                    else if (Build.VERSION.SDK_INT >= 21)
                        if (d.canApplyTheme())
                            return true;
                
                } else {
                
                    final ConstantStateFuture future = mDrawableFutures.get(i);
                    
                    if ((future != null) && future.canApplyTheme())
                        return true;
                
                }
            
            }
            
            return false;
        
        }
        
        public synchronized boolean canConstantState() {
        
            if (mCheckedConstantState)
                return mCanConstantState;
            
            createAllFutures();
            mCheckedConstantState = true;
            
            final Drawable[] drawables = mDrawables;
            final int N = mNumChildren;
            
            for (int i = 0; i < N; ++i) {
            
                if (drawables[i].getConstantState() == null) {
                
                    mCanConstantState = false;
                    return false;
                
                }
            
            }
            
            mCanConstantState = true;
            return true;
        
        }
        
        protected void computeConstantSize() {
        
            mComputedConstantSize = true;
            createAllFutures();
            
            final Drawable[] drawables = mDrawables;
            final int N = mNumChildren;
            
            mConstantMinimumWidth = mConstantMinimumHeight = 0;
            mConstantWidth = mConstantHeight = -1;
            
            for (int i = 0; i < N; ++i) {
            
                final Drawable dr = drawables[i];
                int s = dr.getIntrinsicWidth();
                
                if (s > mConstantWidth)
                    mConstantWidth = s;
                
                s = dr.getIntrinsicHeight();
                
                if (s > mConstantHeight)
                    mConstantHeight = s;
                
                s = dr.getMinimumWidth();
                
                if (s > mConstantMinimumWidth)
                    mConstantMinimumWidth = s;
                
                s = dr.getMinimumHeight();
                
                if (s > mConstantMinimumHeight)
                    mConstantMinimumHeight = s;
            
            }
        
        }
        
        private final void createAllFutures() {
        
            if (mDrawableFutures != null) {
            
                final int futureCount = mDrawableFutures.size();
                
                for (int keyIndex = 0; keyIndex < futureCount; keyIndex++) {
                
                    final int index = mDrawableFutures.keyAt(keyIndex);
                    mDrawables[index] = mDrawableFutures.valueAt(keyIndex).get(this);
                
                }
                
                mDrawableFutures = null;
            
            }
        
        }
        
        final int getCapacity() {
            return mDrawables.length;
        }
        
        @Override
        public int getChangingConfigurations() {
            return (mChangingConfigurations | mChildrenChangingConfigurations);
        }
        
        public final Drawable getChild(int index) {
        
            final Drawable result = mDrawables[index];
            
            if (result != null)
                return result;
            
            // Prepare future drawable if necessary.
            if (mDrawableFutures != null) {
            
                final int keyIndex = mDrawableFutures.indexOfKey(index);
                
                if (keyIndex >= 0) {
                
                    final Drawable prepared = mDrawableFutures.valueAt(keyIndex).get(this);
                    
                    mDrawables[index] = prepared;
                    mDrawableFutures.removeAt(keyIndex);
                    
                    return prepared;
                
                }
            
            }
            
            return null;
        
        }
        
        public final int getChildCount() {
            return mNumChildren;
        }
        
        public final int getConstantHeight() {
        
            if (!mComputedConstantSize)
                computeConstantSize();
            
            return mConstantHeight;
        
        }
        
        public final int getConstantMinimumHeight() {
        
            if (!mComputedConstantSize)
                computeConstantSize();
            
            return mConstantMinimumHeight;
        
        }
        
        public final int getConstantMinimumWidth() {
        
            if (!mComputedConstantSize)
                computeConstantSize();
            
            return mConstantMinimumWidth;
        
        }
        
        public final Rect getConstantPadding() {
        
            if (mVariablePadding)
                return null;
            
            if ((mConstantPadding != null) || mPaddingChecked)
                return mConstantPadding;
            
            createAllFutures();
            
            Rect r = null;
            final Rect t = new Rect();
            
            final Drawable[] drawables = mDrawables;
            final int N = mNumChildren;
            
            for (int i = 0; i < N; ++i) {
            
                if (drawables[i].getPadding(t)) {
                
                    if (r == null)
                        r = new Rect(0, 0, 0, 0);
                    
                    if (t.left > r.left)
                        r.left = t.left;
                    
                    if (t.top > r.top)
                        r.top = t.top;
                    
                    if (t.right > r.right)
                        r.right = t.right;
                    
                    if (t.bottom > r.bottom)
                        r.bottom = t.bottom;
                
                }
            
            }
            
            mPaddingChecked = true;
            return (mConstantPadding = r);
        
        }
        
        public final int getConstantWidth() {
        
            if (!mComputedConstantSize)
                computeConstantSize();
            
            return mConstantWidth;
        
        }
        
        public final int getEnterFadeDuration() {
            return mEnterFadeDuration;
        }
        
        public final int getExitFadeDuration() {
            return mExitFadeDuration;
        }
        
        public final int getOpacity() {
        
            if (mCheckedOpacity)
                return mOpacity;
            
            createAllFutures();
            mCheckedOpacity = true;
            
            final Drawable[] drawables = mDrawables;
            final int N = mNumChildren;
            
            int op = ((N > 0) ? drawables[0].getOpacity() : PixelFormat.TRANSPARENT);
            
            for (int i = 1; i < N; ++i)
                op = Drawable.resolveOpacity(op, drawables[i].getOpacity());
            
            mOpacity = op;
            return op;
        
        }
        
        public void growArray(int oldSize, int newSize) {
        
            Drawable[] newDrawables = new Drawable[newSize];
            System.arraycopy(mDrawables, 0, newDrawables, 0, oldSize);
            
            mDrawables = newDrawables;
        
        }
        
        public final boolean isConstantSize() {
            return mConstantSize;
        }
        
        public final boolean isStateful() {
        
            if (mCheckedStateful)
                return mStateful;
            
            createAllFutures();
            mCheckedStateful = true;
            
            final Drawable[] drawables = mDrawables;
            final int N = mNumChildren;
            
            for (int i = 0; i < N; ++i) {
            
                if (drawables[i].isStateful()) {
                
                    mStateful = true;
                    return true;
                
                }
            
            }
            
            mStateful = false;
            return false;
        
        }
        
        final void mutate() {
        
            // No need to call createAllFutures, since future drawables will
            // mutate when they are prepared.
            final Drawable[] drawables = mDrawables;
            final int N = mNumChildren;
            
            for (int i = 0; i < N; ++i)
                if (drawables[i] != null)
                    drawables[i].mutate();
            
            mMutated = true;
        
        }
        
        public final void setConstantSize(boolean constant) {
            mConstantSize = constant;
        }
        
        public final void setEnterFadeDuration(int duration) {
            mEnterFadeDuration = duration;
        }
        
        public final void setExitFadeDuration(int duration) {
            mExitFadeDuration = duration;
        }
        
        final void setLayoutDirection(int layoutDirection) {
        
            // No need to call createAllFutures, since future drawables will
            // change layout direction when they are prepared.
            final Drawable[] drawables = mDrawables;
            final int N = mNumChildren;
            
            for (int i = 0; i < N; ++i)
                if (drawables[i] != null)
                    if (Build.VERSION.SDK_INT >= 23)
                        drawables[i].setLayoutDirection(layoutDirection);
            
            mLayoutDirection = layoutDirection;
        
        }
        
        /**
         * A boolean value indicating whether to use the maximum padding value
         * of all frames in the set (false), or to use the padding value of the
         * frame being shown (true). Default value is false.
         */
        public final void setVariablePadding(boolean variable) {
            mVariablePadding = variable;
        }
        
        /** Class capable of cloning a Drawable from another Drawable's ConstantState. */
        private static class ConstantStateFuture {
        
            private final ConstantState mConstantState;
            
            private ConstantStateFuture(Drawable source) {
                mConstantState = (ConstantState) source.getConstantState();
            }
            
            /** Whether the constant state wrapped by this future can apply a theme. */
            boolean canApplyTheme() {
                return mConstantState.canApplyTheme();
            }
            
            /**
             * Obtains and prepares the Drawable represented by this future.
             *
             * @param state the container into which this future will be placed
             * @return a prepared Drawable
             */
            Drawable get(DrawableContainerState state) {
            
                final Drawable result;
                
                if (state.mRes == null)
                    result = mConstantState.newDrawable();
                else if (state.mTheme == null)
                    result = mConstantState.newDrawable(state.mRes);
                else
                    result = mConstantState.newDrawable(state.mRes, state.mTheme);
                
                result.setCallback(state.mOwner);
                
                if (Build.VERSION.SDK_INT >= 23)
                    result.setLayoutDirection(state.mLayoutDirection);
                
                if (state.mMutated)
                    result.mutate();
                
                return result;
            
            }
        
        }
    
    }

}