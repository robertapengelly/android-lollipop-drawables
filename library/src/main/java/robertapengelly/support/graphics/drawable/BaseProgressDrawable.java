package robertapengelly.support.graphics.drawable;

import  android.content.Context;
import  android.content.res.ColorStateList;
import  android.graphics.Canvas;
import  android.graphics.Color;
import  android.graphics.ColorFilter;
import  android.graphics.Paint;
import  android.graphics.PixelFormat;
import  android.graphics.PorterDuff;
import  android.graphics.PorterDuffColorFilter;
import  android.graphics.Rect;
import  android.graphics.drawable.Animatable;
import  android.graphics.drawable.Drawable;

import  robertapengelly.support.animation.Animator;

abstract class BaseProgressDrawable extends LollipopDrawable {

    private DummyConstantState mConstantState = new DummyConstantState();
    private Paint mPaint;
    
    protected int mAlpha = 0xFF;
    protected ColorFilter mColorFilter;
    protected PorterDuffColorFilter mTintFilter;
    protected ColorStateList mTintList;
    protected PorterDuff.Mode mTintMode = PorterDuff.Mode.SRC_IN;
    protected boolean mUseIntrinsicPadding = true;
    
     /** {@inheritDoc} */
    @Override
    public void draw(Canvas canvas) {
    
        Rect bounds = getBounds();
        
        if ((bounds.height() == 0) || (bounds.width() == 0))
            return;
        
        int saveCount = canvas.save();
        canvas.translate(bounds.left, bounds.top);
        
        if (mPaint == null) {
        
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.BLACK);
            
            onPreparePaint(mPaint);
        
        }
        
        mPaint.setAlpha(mAlpha);
        mPaint.setColorFilter(getColorFilterForDrawing());

        onDraw(canvas, bounds.width(), bounds.height(), mPaint);
        canvas.restoreToCount(saveCount);
    
    }
    
    @Override
    public int getAlpha() {
        return mAlpha;
    }
    
    /** {@inheritDoc} */
    @Override
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }
    
    ColorFilter getColorFilterForDrawing() {
        return ((mColorFilter != null) ? mColorFilter : mTintFilter);
    }
    
    // Workaround LayerDrawable.ChildDrawable which calls getConstantState().newDrawable()
    // without checking for null.
    // We are never inflated from XML so the protocol of ConstantState does not apply to us. In
    // order to make LayerDrawable happy, we return ourselves from DummyConstantState.newDrawable().
    
    @Override
    public ConstantState getConstantState() {
        return mConstantState;
    }
    
    /** {@inheritDoc} */
    @Override
    public int getOpacity() {
        // Be safe.
        return PixelFormat.TRANSLUCENT;
    }
    
    /**
     * Get whether this drawable is using an intrinsic padding. The default is {@code true}.
     *
     * @return Whether this drawable is using an intrinsic padding.
     */
    boolean getUseIntrinsicPadding() {
        return mUseIntrinsicPadding;
    }
    
    @Override
    public boolean isStateful() {
        return ((mTintList != null) && mTintList.isStateful());
    }
    
    protected abstract void onDraw(Canvas canvas, int width, int height, Paint paint);
    
    protected abstract void onPreparePaint(Paint paint);
    
    @Override
    protected boolean onStateChange(int[] state) {
        return updateTintFilter();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setAlpha(int alpha) {
    
        if (mAlpha != alpha) {
        
            mAlpha = alpha;
            invalidateSelf();
        
        }
    
    }
    
    /** {@inheritDoc} */
    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    
        mColorFilter = colorFilter;
        invalidateSelf();
    
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTint(int tintColor) {
        setTintList(ColorStateList.valueOf(tintColor));
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTintList(ColorStateList tint) {
    
        mTintList = tint;
        
        if (updateTintFilter())
            invalidateSelf();
    
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
    
        mTintMode = tintMode;
        
        if (updateTintFilter())
            invalidateSelf();
    
    }
    
    /**
     * Set whether this drawable should use an intrinsic padding. The default is {@code true}.
     *
     * @param useIntrinsicPadding Whether this drawable should use its intrinsic padding.
     */
    void setUseIntrinsicPadding(boolean useIntrinsicPadding) {
    
        if (mUseIntrinsicPadding != useIntrinsicPadding) {
        
            mUseIntrinsicPadding = useIntrinsicPadding;
            invalidateSelf();
        
        }
    
    }
    
    private boolean updateTintFilter() {
    
        if ((mTintList == null) || (mTintMode == null)) {
        
            boolean hadTintFilter = mTintFilter != null;
            mTintFilter = null;
            
            return hadTintFilter;
        
        }
        
        int tintColor = mTintList.getColorForState(getState(), Color.TRANSPARENT);
        
        // They made PorterDuffColorFilter.setColor() and setMode() @hide.
        mTintFilter = new PorterDuffColorFilter(tintColor, mTintMode);
        return true;
    
    }
    
    private class DummyConstantState extends ConstantState {
    
        @Override
        public int getChangingConfigurations() {
            return 0;
        }
        
        @Override
        public Drawable newDrawable() {
            return BaseProgressDrawable.this;
        }
    
    }

}