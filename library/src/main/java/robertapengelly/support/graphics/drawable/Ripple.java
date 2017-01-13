package robertapengelly.support.graphics.drawable;

import  android.graphics.Canvas;
import  android.graphics.Paint;
import  android.graphics.Rect;
import  android.view.animation.Interpolator;
import  android.view.animation.LinearInterpolator;

import  robertapengelly.support.animation.Animator;
import  robertapengelly.support.animation.AnimatorListenerAdapter;
import  robertapengelly.support.animation.ObjectAnimator;

/** Draw a Material ripple. */
class Ripple {

    private static final Interpolator DECEL_INTERPOLATOR = new LogInterpolator();
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    
    private static final float GLOBAL_SPEED = 1f;
    private static final float WAVE_OPACITY_DECAY_VELOCITY = (3f / GLOBAL_SPEED);
    private static final float WAVE_TOUCH_DOWN_ACCELERATION = (1024f * GLOBAL_SPEED);
    private static final float WAVE_TOUCH_UP_ACCELERATION = (3400f * GLOBAL_SPEED);
    
    private static final long RIPPLE_ENTER_DELAY = 80;
    
    private final AnimatorListenerAdapter mAnimationListener = new AnimatorListenerAdapter() {
    
        @Override
        public void onAnimationEnd(Animator animation) {
            removeSelf();
        }
    
    };
    
    /** Bounds used for computing max radius. */
    private final Rect mBounds;
    
    private final RippleDrawable mOwner;
    
    // Software animators.
    private ObjectAnimator mAnimOpacity, mAnimRadius, mAnimX, mAnimY;
    
    /** Whether we were canceled externally and should avoid self-removal. */
    private boolean mCanceled;
    
    private float mClampedStartingX, mClampedStartingY, mStartingX, mStartingY;
    
    /** Screen density used to adjust pixel-based velocities. */
    private float mDensity;
    
    /** Whether we have an explicit maximum radius. */
    private boolean mHasMaxRadius;
    
    private float mOpacity = 1, mOuterX, mOuterY;
    
    /** Maximum ripple radius. */
    private float mOuterRadius;
    
    // Values used to tween between the start and end positions.
    private float mTweenRadius = 0;
    private float mTweenX = 0;
    private float mTweenY = 0;
    
    Ripple(RippleDrawable owner, Rect bounds, float startingX, float startingY) {
    
        mBounds = bounds;
        mOwner = owner;
        
        mStartingX = startingX;
        mStartingY = startingY;
    
    }
    
    /**
     * Cancels all animations. The caller is responsible for removing
     * the ripple from the list of animating ripples.
     */
    void cancel() {
    
        mCanceled = true;
        cancelSoftwareAnimations();
        mCanceled = false;
    
    }
    
    private void cancelSoftwareAnimations() {
    
        if (mAnimRadius != null) {
        
            mAnimRadius.cancel();
            mAnimRadius = null;
        
        }
        
        if (mAnimOpacity != null) {
        
            mAnimOpacity.cancel();
            mAnimOpacity = null;
        
        }
        
        if (mAnimX != null) {
        
            mAnimX.cancel();
            mAnimX = null;
        
        }
        
        if (mAnimY != null) {
        
            mAnimY.cancel();
            mAnimY = null;
        
        }
    
    }
    
    private void clampStartingPosition() {
    
        final float cx = mBounds.exactCenterX();
        final float cy = mBounds.exactCenterY();
        
        final float dx = (mStartingX - cx);
        final float dy = (mStartingY - cy);
        
        final float r = mOuterRadius;
        
        if ((dx * dx + dy * dy) > (r * r)) {
        
            // Point is outside the circle, clamp to the circumference.
            final double angle = Math.atan2(dy, dx);
            
            mClampedStartingX = (cx + (float) (Math.cos(angle) * r));
            mClampedStartingY = (cy + (float) (Math.sin(angle) * r));
        
        } else {
        
            mClampedStartingX = mStartingX;
            mClampedStartingY = mStartingY;
        
        }
    
    }
    
    /** Draws the ripple centered at (0,0) using the specified paint. */
    boolean draw(Canvas c, Paint p) {
    
        boolean hasContent = false;
        
        final int paintAlpha = p.getAlpha();
        
        final int alpha = (int) (paintAlpha * mOpacity + 0.5f);
        final float radius = MathUtils.lerp(0, mOuterRadius, mTweenRadius);
        
        if ((alpha > 0) && (radius > 0)) {
        
            final float x = MathUtils.lerp((mClampedStartingX - mBounds.exactCenterX()), mOuterX, mTweenX);
            final float y = MathUtils.lerp((mClampedStartingY - mBounds.exactCenterY()), mOuterY, mTweenY);
            
            p.setAlpha(alpha);
            c.drawCircle(x, y, radius, p);
            
            p.setAlpha(paintAlpha);
            hasContent = true;
        
        }
        
        return hasContent;
    
    }
    
    private void endSoftwareAnimations() {
    
        if (mAnimRadius != null) {
        
            mAnimRadius.end();
            mAnimRadius = null;
        
        }
        
        if (mAnimOpacity != null) {
        
            mAnimOpacity.end();
            mAnimOpacity = null;
        
        }
        
        if (mAnimX != null) {
        
            mAnimX.end();
            mAnimX = null;
        
        }
        
        if (mAnimY != null) {
        
            mAnimY.end();
            mAnimY = null;
        
        }
    
    }
    
    /** Starts the enter animation. */
    void enter() {
    
        cancel();
        
        final int radiusDuration = (int) (1000 * Math.sqrt(mOuterRadius / WAVE_TOUCH_DOWN_ACCELERATION * mDensity) + 0.5);
        
        final ObjectAnimator radius = ObjectAnimator.ofFloat(this, "radiusGravity", 1);
        radius.setDuration(radiusDuration);
        radius.setInterpolator(LINEAR_INTERPOLATOR);
        radius.setStartDelay(RIPPLE_ENTER_DELAY);
        
        final ObjectAnimator cx = ObjectAnimator.ofFloat(this, "xGravity", 1);
        cx.setDuration(radiusDuration);
        cx.setInterpolator(LINEAR_INTERPOLATOR);
        cx.setStartDelay(RIPPLE_ENTER_DELAY);
        
        final ObjectAnimator cy = ObjectAnimator.ofFloat(this, "yGravity", 1);
        cy.setDuration(radiusDuration);
        cy.setInterpolator(LINEAR_INTERPOLATOR);
        cy.setStartDelay(RIPPLE_ENTER_DELAY);
        
        mAnimRadius = radius;
        mAnimX = cx;
        mAnimY = cy;
        
        // Enter animations always run on the UI thread, since it's unlikely
        // that anything interesting is happening until the user lifts their finger.
        AnimatorsCompat.startWithAutoCancel(radius);
        AnimatorsCompat.startWithAutoCancel(cx);
        AnimatorsCompat.startWithAutoCancel(cy);
    
    }
    
    /** Starts the exit animation. */
    void exit() {
    
        cancel();
        
        final float radius = MathUtils.lerp(0, mOuterRadius, mTweenRadius);
        final float remaining;
        
        if ((mAnimRadius != null) && mAnimRadius.isRunning())
            remaining = (mOuterRadius - radius);
        else
            remaining = mOuterRadius;
        
        final int opacityDuration = (int) (1000 * mOpacity / WAVE_OPACITY_DECAY_VELOCITY + 0.5f);
        final int radiusDuration = (int) (1000 * Math.sqrt(remaining / (WAVE_TOUCH_UP_ACCELERATION
            + WAVE_TOUCH_DOWN_ACCELERATION) * mDensity) + 0.5);
        
        exitSoftware(radiusDuration, opacityDuration);
    
    }
    
    private void exitSoftware(int radiusDuration, int opacityDuration) {
    
        final ObjectAnimator radiusAnim = ObjectAnimator.ofFloat(this, "radiusGravity", 1);
        radiusAnim.setDuration(radiusDuration);
        radiusAnim.setInterpolator(DECEL_INTERPOLATOR);
        
        final ObjectAnimator xAnim = ObjectAnimator.ofFloat(this, "xGravity", 1);
        xAnim.setDuration(radiusDuration);
        xAnim.setInterpolator(DECEL_INTERPOLATOR);
        
        final ObjectAnimator yAnim = ObjectAnimator.ofFloat(this, "yGravity", 1);
        yAnim.setDuration(radiusDuration);
        yAnim.setInterpolator(DECEL_INTERPOLATOR);
        
        final ObjectAnimator opacityAnim = ObjectAnimator.ofFloat(this, "opacity", 0);
        opacityAnim.addListener(mAnimationListener);
        opacityAnim.setDuration(opacityDuration);
        opacityAnim.setInterpolator(LINEAR_INTERPOLATOR);
        
        mAnimOpacity = opacityAnim;
        mAnimRadius = radiusAnim;
        mAnimX = xAnim;
        mAnimY = yAnim;
        
        AnimatorsCompat.startWithAutoCancel(radiusAnim);
        AnimatorsCompat.startWithAutoCancel(opacityAnim);
        AnimatorsCompat.startWithAutoCancel(xAnim);
        AnimatorsCompat.startWithAutoCancel(yAnim);
    
    }
    
    /** Returns the maximum bounds of the ripple relative to the ripple center. */
    void getBounds(Rect bounds) {
    
        final int outerX = (int) mOuterX;
        final int outerY = (int) mOuterY;
        
        final int r = (int) mOuterRadius + 1;
        bounds.set((outerX - r), (outerY - r), (outerX + r), (outerY + r));
    
    }
    
    float getOpacity() {
        return mOpacity;
    }
    
    float getRadiusGravity() {
        return mTweenRadius;
    }
    
    float getXGravity() {
        return mTweenX;
    }
    
    float getYGravity() {
        return mTweenY;
    }
    
    private void invalidateSelf() {
        mOwner.invalidateSelf();
    }
    
    /**
     * Jump all animations to their end state. The caller is responsible for
     * removing the ripple from the list of animating ripples.
     */
    void jump() {
    
        mCanceled = true;
        endSoftwareAnimations();
        mCanceled = false;
    
    }
    
    /**
     * Specifies the starting position relative to the drawable bounds. No-op if
     * the ripple has already entered.
     */
    void move(float x, float y) {
    
        mStartingX = x;
        mStartingY = y;
        
        clampStartingPosition();
    
    }
    
    void onHotspotBoundsChanged() {
    
        if (!mHasMaxRadius) {
        
            final float halfHeight = (mBounds.height() / 2f);
            final float halfWidth = (mBounds.width() / 2f);
            
            mOuterRadius = (float) Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);
            clampStartingPosition();
        
        }
    
    }
    
    private void removeSelf() {
    
        // The owner will invalidate itself.
        if (!mCanceled)
            mOwner.removeRipple(this);
    
    }
    
    void setOpacity(float a) {
    
        mOpacity = a;
        invalidateSelf();
    
    }
    
    void setRadiusGravity(float r) {
    
        mTweenRadius = r;
        invalidateSelf();
    
    }
    
    void setXGravity(float x) {
    
        mTweenX = x;
        invalidateSelf();
    
    }
    
    void setYGravity(float y) {
    
        mTweenY = y;
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
        
        clampStartingPosition();
    
    }
    
    /** Interpolator with a smooth log deceleration. */
    private static final class LogInterpolator implements Interpolator {
    
        @Override
        public float getInterpolation(float input) {
            return (1 - (float) Math.pow(400, (-input * 1.4)));
        }
    
    }

}