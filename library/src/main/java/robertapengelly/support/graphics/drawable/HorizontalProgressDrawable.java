package robertapengelly.support.graphics.drawable;

import  android.annotation.SuppressLint;
import  android.content.Context;
import  android.content.res.ColorStateList;
import  android.content.res.TypedArray;
import  android.graphics.Color;
import  android.graphics.PorterDuff;
import  android.graphics.drawable.Drawable;
import  android.graphics.drawable.LayerDrawable;
import  android.os.Build;
import  android.util.Log;

import  robertapengelly.support.lollipopdrawables.R;

/** A backported {@code Drawable} for determinate horizontal {@code ProgressBar}. */
public class HorizontalProgressDrawable extends LayerDrawable {
    
    private static final String TAG = HorizontalProgressDrawable.class.getSimpleName();
    
    private float mBackgroundAlpha;
    
    private HorizontalProgressBackgroundDrawable mBackgroundDrawable;
    private SingleHorizontalProgressDrawable mProgressDrawable;
    private SingleHorizontalProgressDrawable mSecondaryProgressDrawable;
    
    private boolean mHasSecondaryProgressTint;
    private boolean mHasSecondaryProgressTintColor;
    
    private ColorStateList mSecondaryProgressTint;
    private int mSecondaryProgressTintColor;
    
    /**
     * Create a new {@code HorizontalProgressDrawable}.
     *
     * @param context the {@code Context} for retrieving style information.
     */
    public HorizontalProgressDrawable(Context context) {
        super(new Drawable[] {
            new HorizontalProgressBackgroundDrawable(context),
            new SingleHorizontalProgressDrawable(context),
            new SingleHorizontalProgressDrawable(context)
        });
        
        mBackgroundAlpha = getFloatFromAttrRes(android.R.attr.disabledAlpha, context);
        
        setId(0, android.R.id.background);
        mBackgroundDrawable = (HorizontalProgressBackgroundDrawable) getDrawable(0);
        
        setId(1, android.R.id.secondaryProgress);
        mSecondaryProgressDrawable = (SingleHorizontalProgressDrawable) getDrawable(1);
        
        setId(2, android.R.id.progress);
        mProgressDrawable = (SingleHorizontalProgressDrawable) getDrawable(2);
        
        int colorControlActivated = getColorFromAttrRes(R.attr.colorControlActivated, context);
        
        if (colorControlActivated == 0)
            if (Build.VERSION.SDK_INT >= 21)
                colorControlActivated = getColorFromAttrRes(android.R.attr.colorControlActivated, context);
        
        setTint(colorControlActivated);
    
    }
    
    // See https://en.wikipedia.org/wiki/Alpha_compositing
    private float compositeAlpha(float alpha1, float alpha2) {
        return (alpha1 + alpha2 * (1 - alpha1));
    }
    
    private int getColorFromAttrRes(int attr, Context context) {
    
        TypedArray a = context.obtainStyledAttributes(new int[] {attr});
        
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    
    }
    
    private float getFloatFromAttrRes(int attrRes, Context context) {
    
        TypedArray a = context.obtainStyledAttributes(new int[] {attrRes});
        
        try {
            return a.getFloat(0, 0);
        } finally {
            a.recycle();
        }
    
    }
    
    /**
     * Get whether this drawable is showing a background. The default is {@code true}.
     *
     * @return Whether this drawable is showing a background.
     */
    public boolean getShowBackground() {
        return mBackgroundDrawable.getShowBackground();
    }
    
    /**
     * Get whether this drawable is using an intrinsic padding. The default is {@code true}.
     *
     * @return Whether this drawable is using an intrinsic padding.
     */
    public boolean getUseIntrinsicPadding() {
        return mBackgroundDrawable.getUseIntrinsicPadding();
    }
    
    /**
     * Set whether this drawable should show a background. The default is {@code true}.
     *
     * @param show Whether background should be shown.
     */
    public void setShowBackground(boolean show) {
    
        if (mBackgroundDrawable.getShowBackground() != show) {
        
            mBackgroundDrawable.setShowBackground(show);
            updateSecondaryProgressTint();
        
        }
    
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTint(int tintColor) {
    
        // Modulate alpha of tintColor against mBackgroundAlpha.
        int backgroundTintColor = ColorUtils.setAlphaComponent(tintColor,
            Math.round(Color.alpha(tintColor) * mBackgroundAlpha));
        mBackgroundDrawable.setTint(backgroundTintColor);
        
        setSecondaryProgressTint(backgroundTintColor);
        mProgressDrawable.setTint(tintColor);
    
    }
    
    /** {@inheritDoc} */
    @Override
    @SuppressLint("NewApi")
    public void setTintList(ColorStateList tint) {
    
        ColorStateList backgroundTint;
        
        if (tint != null) {
        
            if (!tint.isOpaque())
                Log.w(TAG, "setTintList() called with a non-opaque ColorStateList, its original alpha will be discarded");
            
            backgroundTint = tint.withAlpha(Math.round(0xFF * mBackgroundAlpha));
        
        } else
            backgroundTint = null;
        
        mBackgroundDrawable.setTintList(backgroundTint);
        setSecondaryProgressTintList(backgroundTint);
        
        mProgressDrawable.setTintList(tint);
    
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
    
        mBackgroundDrawable.setTintMode(tintMode);
        mProgressDrawable.setTintMode(tintMode);
        mSecondaryProgressDrawable.setTintMode(tintMode);
    
    }
    
    private void setSecondaryProgressTint(int tintColor) {
    
        mHasSecondaryProgressTintColor = true;
        mSecondaryProgressTintColor = tintColor;
        
        mHasSecondaryProgressTint = false;
        updateSecondaryProgressTint();
    
    }
    
    private void setSecondaryProgressTintList(ColorStateList tint) {
    
        mHasSecondaryProgressTintColor = false;
        
        mHasSecondaryProgressTint = true;
        mSecondaryProgressTint = tint;
        
        updateSecondaryProgressTint();
    
    }
    
    /**
     * Set whether this drawable should use an intrinsic padding. The default is {@code true}.
     *
     * @param useIntrinsicPadding Whether this drawable should use its intrinsic padding.
     */
    public void setUseIntrinsicPadding(boolean useIntrinsicPadding) {
    
        mBackgroundDrawable.setUseIntrinsicPadding(useIntrinsicPadding);
        mProgressDrawable.setUseIntrinsicPadding(useIntrinsicPadding);
        mSecondaryProgressDrawable.setUseIntrinsicPadding(useIntrinsicPadding);
    
    }
    
    private void updateSecondaryProgressTint() {
    
        if (mHasSecondaryProgressTintColor) {
        
            int tintColor = mSecondaryProgressTintColor;
            
            if (!getShowBackground()) {
            
                // Alpha of tintColor may not be mBackgroundAlpha because we modulated it in
                // setTint().
                float backgroundAlpha = (float) Color.alpha(tintColor) / 0xFF;
                tintColor = ColorUtils.setAlphaComponent(tintColor, Math.round(
                    0xFF * compositeAlpha(backgroundAlpha, backgroundAlpha)));
            
            }
            
            mSecondaryProgressDrawable.setTint(tintColor);
        
        } else if (mHasSecondaryProgressTint) {
        
            ColorStateList tint = mSecondaryProgressTint;
            
            if (!getShowBackground())
                // Composite alpha so that the secondary progress looks as before.
                tint = tint.withAlpha(Math.round(0xFF * compositeAlpha(mBackgroundAlpha, mBackgroundAlpha)));
            
            mSecondaryProgressDrawable.setTintList(tint);
        
        }
    
    }

}