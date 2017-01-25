package robertapengelly.support.graphics.drawable;

import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.Resources.Theme;
import  android.content.res.TypedArray;
import  android.graphics.Canvas;
import  android.graphics.ColorFilter;
import  android.graphics.Rect;
import  android.graphics.PorterDuff.Mode;
import  android.graphics.drawable.Drawable;
import  android.os.Build;
import  android.os.SystemClock;
import  android.util.AttributeSet;
import  android.util.Log;
import  android.util.TypedValue;

import  java.io.IOException;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

import  robertapengelly.support.lollipopdrawables.R;

public class AnimatedRotateDrawable extends LollipopDrawable implements Animatable, Drawable.Callback, Runnable {

    private boolean mMutated, mRunning;
    private float mCurrentDegrees, mIncrement;
    
    private AnimatedRotateState mState;
    
    public AnimatedRotateDrawable() {
        this(null, null);
    }
    
    private AnimatedRotateDrawable(AnimatedRotateState rotateState, Resources res) {
    
        mState = new AnimatedRotateState(rotateState, this, res);
        init();
    
    }
    
    @Override
    public void draw(Canvas canvas) {
    
        int saveCount = canvas.save();
        
        final AnimatedRotateState st = mState;
        final Drawable drawable = st.mDrawable;
        final Rect bounds = drawable.getBounds();
        
        int h = (bounds.bottom - bounds.top);
        int w = (bounds.right - bounds.left);
        
        float px = (st.mPivotXRel ? (w * st.mPivotX) : st.mPivotX);
        float py = (st.mPivotYRel ? (h * st.mPivotY) : st.mPivotY);
        
        canvas.rotate(mCurrentDegrees, (px + bounds.left), (py + bounds.top));
        
        drawable.draw(canvas);
        
        canvas.restoreToCount(saveCount);
    
    }
    
    @Override
    public int getAlpha() {
    
        Drawable d = mState.mDrawable;
        
        if (d instanceof LollipopDrawable)
            //noinspection RedundantCast
            return ((LollipopDrawable) d).getAlpha();
        else if (Build.VERSION.SDK_INT >= 19)
            return mState.mDrawable.getAlpha();
        
        return super.getAlpha();
    
    }
    
    @Override
    public int getChangingConfigurations() {
        return (super.getChangingConfigurations()
            | mState.mChangingConfigurations | mState.mDrawable.getChangingConfigurations());
    }
    
    @Override
    public ConstantState getConstantState() {
    
        if (mState.canConstantState()) {
        
            mState.mChangingConfigurations = getChangingConfigurations();
            return mState;
        
        }
        
        return null;
    
    }
    
    /** Returns the drawable rotated by this RotateDrawable. */
    public Drawable getDrawable() {
        return mState.mDrawable;
    }
    
    @Override
    public int getIntrinsicHeight() {
        return mState.mDrawable.getIntrinsicHeight();
    }
    
    @Override
    public int getIntrinsicWidth() {
        return mState.mDrawable.getIntrinsicWidth();
    }
    
    @Override
    public int getOpacity() {
        return mState.mDrawable.getOpacity();
    }
    
    @Override
    public boolean getPadding(Rect padding) {
        return mState.mDrawable.getPadding(padding);
    }
    
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        
        final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.AnimatedRotateDrawable);
        
        super.inflateWithAttributes(a, R.styleable.AnimatedRotateDrawable_android_visible);
        
        TypedValue tv = a.peekValue(R.styleable.AnimatedRotateDrawable_android_pivotX);
        
        final boolean pivotXRel = (tv.type == TypedValue.TYPE_FRACTION);
        final float pivotX = (pivotXRel ? tv.getFraction(1.0f, 1.0f) : tv.getFloat());
        
        tv = a.peekValue(R.styleable.AnimatedRotateDrawable_android_pivotY);
        
        final boolean pivotYRel = (tv.type == TypedValue.TYPE_FRACTION);
        final float pivotY = (pivotYRel ? tv.getFraction(1.0f, 1.0f) : tv.getFloat());
        
        setFramesCount(a.getInt(R.styleable.AnimatedRotateDrawable_framesCount, 12));
        setFramesDuration(a.getInt(R.styleable.AnimatedRotateDrawable_frameDuration, 150));
        
        final int res = a.getResourceId(R.styleable.AnimatedRotateDrawable_android_drawable, 0);
        
        Drawable drawable = null;
        
        if (res > 0)
            drawable = LollipopDrawablesCompat.getDrawable(r, res, theme);
        
        a.recycle();
        
        int outerDepth = parser.getDepth();
        int type;
        
        while (((type = parser.next()) != XmlPullParser.END_DOCUMENT) &&
            ((type != XmlPullParser.END_TAG) || (parser.getDepth() > outerDepth))) {
            
            if (type != XmlPullParser.START_TAG)
                continue;
            
            if ((drawable = LollipopDrawablesCompat.createFromXmlInner(r, parser, attrs, theme)) == null)
                Log.w("drawable", "Bad element under <animated-rotate>: " + parser .getName());
        
        }
        
        if (drawable == null)
            Log.w("drawable", "No drawable specified for <animated-rotate>");
        
        final AnimatedRotateState rotateState = mState;
        rotateState.mDrawable = drawable;
        
        rotateState.mPivotX = pivotX;
        rotateState.mPivotXRel = pivotXRel;
        
        rotateState.mPivotY = pivotY;
        rotateState.mPivotYRel = pivotYRel;
        
        init();
        
        if (drawable != null)
            drawable.setCallback(this);
    
    }
    
    private void init() {
    
        final AnimatedRotateState state = mState;
        final Drawable drawable = state.mDrawable;
        
        mIncrement = (360.0f / state.mFramesCount);
        
        if (drawable != null) {
        
            drawable.setFilterBitmap(true);
            
            if (drawable instanceof BitmapDrawable)
                ((BitmapDrawable) drawable).setAntiAlias(true);
        
        }
    
    }
    
    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }
    
    @Override
    public boolean isRunning() {
        return mRunning;
    }
    
    @Override
    public boolean isStateful() {
        return mState.mDrawable.isStateful();
    }
    
    @Override
    public Drawable mutate() {
    
        if (!mMutated && (super.mutate() == this)) {
        
            mState.mDrawable.mutate();
            mMutated = true;
        
        }
        
        return this;
    
    }
    
    private void nextFrame() {
    
        unscheduleSelf(this);
        scheduleSelf(this, (SystemClock.uptimeMillis() + mState.mFrameDuration));
    
    }
    
    @Override
    protected void onBoundsChange(Rect bounds) {
        mState.mDrawable.setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }
    
    @Override
    protected boolean onLevelChange(int level) {
        return mState.mDrawable.setLevel(level);
    }
    
    @Override
    protected boolean onStateChange(int[] state) {
        return mState.mDrawable.setState(state);
    }
    
    @Override
    public void run() {
    
        // TODO: This should be computed in draw(Canvas), based on the amount
        // of time since the last frame drawn
        mCurrentDegrees += mIncrement;
        
        if (mCurrentDegrees > (360.0f - mIncrement))
            mCurrentDegrees = 0.0f;
        
        invalidateSelf();
        nextFrame();
    
    }
    
    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }
    
    @Override
    public void setAlpha(int alpha) {
        mState.mDrawable.setAlpha(alpha);
    }
    
    @Override
    public void setColorFilter(ColorFilter cf) {
        mState.mDrawable.setColorFilter(cf);
    }
    
    public void setFramesCount(int framesCount) {
    
        mState.mFramesCount = framesCount;
        mIncrement = (360.0f / mState.mFramesCount);
    
    }
    
    public void setFramesDuration(int framesDuration) {
        mState.mFrameDuration = framesDuration;
    }
    
    @Override
    public void setTintList(ColorStateList tint) {
    
        Drawable d = mState.mDrawable;
        
        if (d instanceof LollipopDrawable)
            //noinspection RedundantCast
            ((LollipopDrawable) d).setTintList(tint);
        else if (Build.VERSION.SDK_INT >= 21)
            mState.mDrawable.setTintList(tint);
    
    }
    
    @Override
    public void setTintMode(Mode tintMode) {
    
        Drawable d = mState.mDrawable;
        
        if (d instanceof LollipopDrawable)
            //noinspection RedundantCast
            ((LollipopDrawable) d).setTintMode(tintMode);
        else if (Build.VERSION.SDK_INT >= 21)
            mState.mDrawable.setTintMode(tintMode);
    
    }
    
    @Override
    public boolean setVisible(boolean visible, boolean restart) {
    
        mState.mDrawable.setVisible(visible, restart);
        
        boolean changed = super.setVisible(visible, restart);
        
        if (visible) {
        
            if (changed || restart) {
            
                mCurrentDegrees = 0.0f;
                nextFrame();
            
            }
        
        } else
            unscheduleSelf(this);
        
        return changed;
    
    }
    
    @Override
    public void start() {
    
        if (!mRunning) {
        
            mRunning = true;
            nextFrame();
        
        }
    
    }
    
    @Override
    public void stop() {
    
        mRunning = false;
        unscheduleSelf(this);
    
    }
    
    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }
    
    final static class AnimatedRotateState extends ConstantState {
    
        private boolean mCanConstantState;
        private boolean mCheckedConstantState;
        
        boolean mPivotXRel;
        boolean mPivotYRel;
        
        float mPivotX;
        float mPivotY;
        
        int mChangingConfigurations;
        int mFrameDuration;
        int mFramesCount;
        
        Drawable mDrawable;
        
        AnimatedRotateState(AnimatedRotateState orig, AnimatedRotateDrawable owner, Resources res) {
        
            if (orig != null) {
            
                if (res != null)
                    //noinspection ConstantConditions
                    mDrawable = orig.mDrawable.getConstantState().newDrawable(res);
                else
                    //noinspection ConstantConditions
                    mDrawable = orig.mDrawable.getConstantState().newDrawable();
                
                mCanConstantState = mCheckedConstantState = true;
                
                mDrawable.setCallback(owner);
                mDrawable.setBounds(orig.mDrawable.getBounds());
                mDrawable.setLevel(orig.mDrawable.getLevel());
                
                if (Build.VERSION.SDK_INT >= 23)
                    mDrawable.setLayoutDirection(orig.mDrawable.getLayoutDirection());
                
                mFramesCount = orig.mFramesCount;
                mFrameDuration = orig.mFrameDuration;
                
                mPivotX = orig.mPivotX;
                mPivotXRel = orig.mPivotXRel;
                
                mPivotY = orig.mPivotY;
                mPivotYRel = orig.mPivotYRel;
            
            }
        
        }
        
        boolean canConstantState() {
        
            if (!mCheckedConstantState) {
            
                mCanConstantState = mDrawable.getConstantState() != null;
                mCheckedConstantState = true;
            
            }
            
            return mCanConstantState;
        
        }
        
        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
        
        @Override
        public Drawable newDrawable() {
            return new AnimatedRotateDrawable(this, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res) {
            return new AnimatedRotateDrawable(this, res);
        }
    
    }

}