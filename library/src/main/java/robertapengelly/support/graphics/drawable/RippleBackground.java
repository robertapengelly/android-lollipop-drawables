package robertapengelly.support.graphics.drawable;

import  android.graphics.Canvas;
import  android.graphics.Color;
import  android.graphics.Paint;
import  android.graphics.Rect;
import  android.view.animation.LinearInterpolator;

import  robertapengelly.support.animation.Animator;
import  robertapengelly.support.animation.AnimatorListenerAdapter;
import  robertapengelly.support.animation.ObjectAnimator;

/** Draws a Material ripple. */
class RippleBackground {

    private static final LinearInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    
    private static final float GLOBAL_SPEED = 1f;
    private static final float WAVE_OPACITY_DECAY_VELOCITY = (3f / GLOBAL_SPEED);
    
    private static final float WAVE_OUTER_OPACITY_ENTER_VELOCITY = (10f * GLOBAL_SPEED);
    
    private static final float WAVE_OUTER_OPACITY_EXIT_VELOCITY_MAX = (4.5f * GLOBAL_SPEED);
    private static final float WAVE_OUTER_OPACITY_EXIT_VELOCITY_MIN = (1.5f * GLOBAL_SPEED);
    
    private static final float WAVE_OUTER_SIZE_INFLUENCE_MAX = 200f;
    private static final float WAVE_OUTER_SIZE_INFLUENCE_MIN = 40f;
    
    private final RippleDrawable mOwner;
    
    private ObjectAnimator mAnimOuterOpacity;
    
    /** Bounds used for computing max radius. */
    private final Rect mBounds;
    
    /** ARGB color for drawing this ripple. */
    private int mColor;
    
    /** Screen density used to adjust pixel-based velocities. */
    private float mDensity;
    
    /** Whether we have an explicit maximum radius. */
    private boolean mHasMaxRadius;
    
    private float mOuterOpacity = 0, mOuterX, mOuterY;
    
    /** Maximum ripple radius. */
    private float mOuterRadius;
    
    /** Creates a new ripple. */
    RippleBackground(RippleDrawable owner, Rect bounds) {
    
        mBounds = bounds;
        mOwner = owner;
    
    }
    
    /**
     * Cancel all animations. The caller is responsible for removing
     * the ripple from the list of animating ripples.
     */
    void cancel() {
    
        if (mAnimOuterOpacity != null) {
        
            mAnimOuterOpacity.cancel();
            mAnimOuterOpacity = null;
        
        }
    
    }
    
    /** Draws the ripple centered at (0,0) using the specified paint. */
    boolean draw(Canvas c, Paint p) {
    
        mColor = p.getColor();
        
        boolean hasContent = false;
        final int paintAlpha = p.getAlpha();
        
        final int outerAlpha = (int) (paintAlpha * mOuterOpacity + 0.5f);
        
        if ((outerAlpha > 0) && (mOuterRadius > 0)) {
        
            p.setAlpha(outerAlpha);
            c.drawCircle(mOuterX, mOuterY, mOuterRadius, p);
            
            p.setAlpha(paintAlpha);
            hasContent = true;
        
        }
        
        return hasContent;
    
    }
    
    private void endSoftwareAnimations() {
    
        if (mAnimOuterOpacity != null) {
        
            mAnimOuterOpacity.end();
            mAnimOuterOpacity = null;
        
        }
    
    }
    
    /** Starts the enter animation. */
    void enter() {
    
        cancel();
        
        final int outerDuration = (int) (1000 * 1.0f / WAVE_OUTER_OPACITY_ENTER_VELOCITY);
        
        final ObjectAnimator outer = ObjectAnimator.ofFloat(this, "outerOpacity", 0, 1);
        outer.setDuration(outerDuration);
        outer.setInterpolator(LINEAR_INTERPOLATOR);
        
        mAnimOuterOpacity = outer;
        
        // Enter animations always run on the UI thread, since it's unlikely
        // that anything interesting is happening until the user lifts their finger.
        AnimatorsCompat.startWithAutoCancel(outer);
    
    }
    
    /** Starts the exit animation. */
    void exit() {
    
        cancel();
        
        // Scale the outer max opacity and opacity velocity based
        // on the size of the outer radius.
        final int opacityDuration = (int) (1000 / WAVE_OPACITY_DECAY_VELOCITY + 0.5f);
        
        final float outerSizeInfluence = MathUtils.constrain((mOuterRadius - WAVE_OUTER_SIZE_INFLUENCE_MIN * mDensity)
            / (WAVE_OUTER_SIZE_INFLUENCE_MAX * mDensity), 0, 1);
        
        final float outerOpacityVelocity = MathUtils.lerp(WAVE_OUTER_OPACITY_EXIT_VELOCITY_MIN,
            WAVE_OUTER_OPACITY_EXIT_VELOCITY_MAX, outerSizeInfluence);
        
        // Determine at what time the inner and outer opacity intersect.
        // inner(t) = mOpacity - t * WAVE_OPACITY_DECAY_VELOCITY / 1000
        // outer(t) = mOuterOpacity + t * WAVE_OUTER_OPACITY_VELOCITY / 1000
        final int inflectionDuration = Math.max(0, (int) (1000 * (1 - mOuterOpacity)
            / (WAVE_OPACITY_DECAY_VELOCITY + outerOpacityVelocity) + 0.5f));
        
        final int inflectionOpacity = (int) (Color.alpha(mColor) * (mOuterOpacity
            + inflectionDuration * outerOpacityVelocity * outerSizeInfluence / 1000) + 0.5f);
        
        exitSoftware(opacityDuration, inflectionDuration, inflectionOpacity);
    
    }
    
    private void exitSoftware(int opacityDuration, int inflectionDuration, int inflectionOpacity) {
    
        final ObjectAnimator outerOpacityAnim;
        
        if (inflectionDuration > 0) {
        
            // Outer opacity continues to increase for a bit.
            outerOpacityAnim = ObjectAnimator.ofFloat(this, "outerOpacity", (inflectionOpacity / 255.0f));
            outerOpacityAnim.setDuration(inflectionDuration);
            outerOpacityAnim.setInterpolator(LINEAR_INTERPOLATOR);
            
            // Chain the outer opacity exit animation.
            final int outerDuration = (opacityDuration - inflectionDuration);
            
            if (outerDuration > 0) {
            
                outerOpacityAnim.addListener(new AnimatorListenerAdapter() {
                
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        animation.removeListener(this);
                    }
                    
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    
                        final ObjectAnimator outerFadeOutAnim = ObjectAnimator.ofFloat(RippleBackground.this, "outerOpacity", 0);
                        outerFadeOutAnim.setDuration(outerDuration);
                        outerFadeOutAnim.setInterpolator(LINEAR_INTERPOLATOR);
                        
                        mAnimOuterOpacity = outerFadeOutAnim;
                        AnimatorsCompat.startWithAutoCancel(outerFadeOutAnim);
                    
                    }
                
                });
            
            }
        
        } else {
        
            outerOpacityAnim = ObjectAnimator.ofFloat(this, "outerOpacity", 0);
            outerOpacityAnim.setDuration(opacityDuration);
        
        }
        
        mAnimOuterOpacity = outerOpacityAnim;
        AnimatorsCompat.startWithAutoCancel(outerOpacityAnim);
    
    }
    
    /** Returns the maximum bounds of the ripple relative to the ripple center. */
    void getBounds(Rect bounds) {
    
        final int outerX = (int) mOuterX;
        final int outerY = (int) mOuterY;
        
        final int r = (int) mOuterRadius + 1;
        bounds.set((outerX - r), (outerY - r), (outerX + r), (outerY + r));
    
    }
    
    float getOuterOpacity() {
        return mOuterOpacity;
    }
    
    private void invalidateSelf() {
        mOwner.invalidateSelf();
    }
    
    /**
     * Jump all animations to their end state. The caller is responsible for
     * removing the ripple from the list of animating ripples.
     */
    void jump() {
        endSoftwareAnimations();
    }
    
    void onHotspotBoundsChanged() {
    
        if (!mHasMaxRadius) {
        
            final float halfWidth = (mBounds.width() / 2f);
            final float halfHeight = (mBounds.height() / 2f);
            
            mOuterRadius = (float) Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);
        
        }
    
    }
    
    void setOuterOpacity(float a) {
    
        mOuterOpacity = a;
        invalidateSelf();
    
    }
    
    void setup(int maxRadius, float density) {
    
        if (maxRadius != RippleDrawable.RADIUS_AUTO) {
        
            mHasMaxRadius = true;
            mOuterRadius = maxRadius;
        
        } else {
        
            final float halfWidth = (mBounds.width() / 2f);
            final float halfHeight = (mBounds.height() / 2f);
            
            mOuterRadius = (float) Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);
        
        }
        
        mDensity = density;
        
        mOuterX = 0;
        mOuterY = 0;
    
    }
    
    boolean shouldDraw() {
        return ((mOuterOpacity > 0) && (mOuterRadius > 0));
    }

}