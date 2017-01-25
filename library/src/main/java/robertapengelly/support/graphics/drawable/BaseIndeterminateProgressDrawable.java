package robertapengelly.support.graphics.drawable;

import  android.content.Context;
import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.TypedArray;
import  android.graphics.Canvas;
import  android.graphics.Color;
import  android.graphics.drawable.Animatable;
import  android.os.Build;

import  robertapengelly.support.animation.Animator;
import  robertapengelly.support.lollipopdrawables.R;

abstract class BaseIndeterminateProgressDrawable extends BaseProgressDrawable implements Animatable {

    protected Animator[] mAnimators;
    
    BaseIndeterminateProgressDrawable(Context context) {
    
        TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.colorControlActivated });
        ColorStateList colorControlActivated = a.getColorStateList(0);
        
        a.recycle();
        
        if (colorControlActivated == null) {
        
            if (Build.VERSION.SDK_INT >= 21) {
            
                a = context.obtainStyledAttributes(new int[] { android.R.attr.colorControlActivated });
                colorControlActivated = a.getColorStateList(0);
                
                a.recycle();
            
            }
            
            if (colorControlActivated == null) {
            
                a = context.obtainStyledAttributes(new int[] { android.R.attr.colorBackground });
                
                int themeColorBackground  = a.getColor(0, 0);
                
                a.recycle();
                
                // If the theme colorBackground is light, use our own light color, otherwise dark
                final float[] hsv = new float[3];
                Color.colorToHSV(themeColorBackground, hsv);
                
                Resources res = context.getResources();
                Resources.Theme theme = context.getTheme();
                
                if (hsv[2] > 0.5f)
                    colorControlActivated = TypedArrayCompat.getColorStateList(res, R.color.accent_material_light, theme);
                else
                    colorControlActivated = TypedArrayCompat.getColorStateList(res, R.color.accent_material_dark, theme);
            
            }
        
        }
        
        // setTintList() has been overridden for compatibility; DrawableCompat won't work because
        // wrapped Drawable won't be Animatable.
        setTintList(colorControlActivated);
    
    }
    
    /** {@inheritDoc} */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        
        if (isStarted())
            invalidateSelf();
    
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isRunning() {
    
        for (Animator animator : mAnimators)
            if (animator.isRunning())
                return true;
        
        return false;
    
    }
    
    private boolean isStarted() {
    
        for (Animator animator : mAnimators)
            if (animator.isStarted())
                return true;
        
        return false;
    
    }
    
    /** {@inheritDoc} */
    @Override
    public void start() {
    
        if (isStarted())
            return;
        
        for (Animator animator : mAnimators)
            animator.start();
        
        invalidateSelf();
    
    }
    
    /** {@inheritDoc} */
    @Override
    public void stop() {
    
        for (Animator animator : mAnimators)
            animator.end();
    
    }

}