package robertapengelly.support.graphics.drawable;

import  android.content.res.Resources;
import  android.content.res.Resources.Theme;
import  android.graphics.Canvas;
import  android.graphics.drawable.Drawable;
import  android.os.SystemClock;

/**
 * An extension of LayerDrawables that is intended to cross-fade between
 * the first and second layer. To start the transition, call {@link #startTransition(int)}. To
 * display just the first layer, call {@link #resetTransition()}.
 *
 * <p>It can be defined in an XML file with the <code>&lt;transition></code> element.
 * Each Drawable in the transition is defined in a nested <code>&lt;item></code>. For more
 * information, see the guide to <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html">Drawable Resources</a>.</p>
 */
public class TransitionDrawable extends LayerDrawable implements Drawable.Callback {

    /** A transition is about to start. */
    private static final int TRANSITION_STARTING = 0;
    
    /** The transition has started and the animation is in progress. */
    private static final int TRANSITION_RUNNING = 1;
    
    /** No transition will be applied. */
    private static final int TRANSITION_NONE = 2;
    
    private boolean mCrossFade;
    private boolean mReverse;
    
    private int mAlpha = 0;
    private int mFrom;
    private int mDuration;
    private int mOriginalDuration;
    private int mTo;
    
    /**
     * The current state of the transition. One of {@link #TRANSITION_STARTING},
     * {@link #TRANSITION_RUNNING} and {@link #TRANSITION_NONE}
     */
    private int mTransitionState = TRANSITION_NONE;
    
    private long mStartTimeMillis;
    
    /**
     * Create a new transition drawable with no layer. To work correctly, at least 2
     * layers must be added to this drawable.
     *
     * @see #TransitionDrawable(Drawable[])
     */
    TransitionDrawable() {
        this(new TransitionState(null, null, null), null, null);
    }
    
    /**
     * Create a new transition drawable with the specified list of layers. At least
     * 2 layers are required for this drawable to work properly.
     */
    public TransitionDrawable(Drawable[] layers) {
        this(new TransitionState(null, null, null), layers);
    }
    
    private TransitionDrawable(TransitionState state, Drawable[] layers) {
        super(layers, state);
    }
    
    private TransitionDrawable(TransitionState state, Resources res, Theme theme) {
        super(state, res, theme);
    }
    
    @Override
    public void draw(Canvas canvas) {
    
        boolean done = true;
        
        switch (mTransitionState) {
        
            case TRANSITION_STARTING:
                done = false;
                mStartTimeMillis = SystemClock.uptimeMillis();
                mTransitionState = TRANSITION_RUNNING;
                break;
            case TRANSITION_RUNNING:
                if (mStartTimeMillis >= 0) {
                
                    float normalized = ((float) (SystemClock.uptimeMillis() - mStartTimeMillis) / mDuration);
                    done = (normalized >= 1.0f);
                    normalized = Math.min(normalized, 1.0f);
                    
                    mAlpha = (int) (mFrom  + (mTo - mFrom) * normalized);
                
                }
                break;
        
        }
        
        final boolean crossFade = mCrossFade;
        final int alpha = mAlpha;
        
        final ChildDrawable[] array = mLayerState.mChildren;
        
        if (done) {
        
            // the setAlpha() calls below trigger invalidation and redraw. If we're done, just draw
            // the appropriate drawable[s] and return
            if (!crossFade || (alpha == 0))
                array[0].mDrawable.draw(canvas);
            
            if (alpha == 0xFF)
                array[1].mDrawable.draw(canvas);
            
            return;
        
        }
        
        Drawable d = array[0].mDrawable;
        
        if (crossFade)
            d.setAlpha(255 - alpha);
        
        d.draw(canvas);
        
        if (crossFade)
            d.setAlpha(0xFF);
        
        if (alpha > 0) {
        
            d = array[1].mDrawable;
            
            d.setAlpha(alpha);
            d.draw(canvas);
            d.setAlpha(0xFF);
        
        }
        
        if (!done)
            invalidateSelf();
    
    }
    
    @Override
    LayerState createConstantState(LayerState state, Resources res) {
        return new TransitionState((TransitionState) state, this, res);
    }
    
    /**
     * Indicates whether the cross fade is enabled for this transition.
     *
     * @return True if cross fading is enabled, false otherwise.
     */
    public boolean isCrossFadeEnabled() {
        return mCrossFade;
    }
    
    /** Show only the first layer. */
    public void resetTransition() {
    
        mAlpha = 0;
        mTransitionState = TRANSITION_NONE;
        
        invalidateSelf();
    
    }
    
    /**
     * Reverses the transition, picking up where the transition currently is.
     * If the transition is not currently running, this will start the transition
     * with the specified duration. If the transition is already running, the last
     * known duration will be used.
     *
     * @param duration The duration to use if no transition is running.
     */
    public void reverseTransition(int duration) {
    
        final long time = SystemClock.uptimeMillis();
        
        // Animation is over
        if ((time - mStartTimeMillis) > mDuration) {
        
            if (mTo == 0) {
            
                mAlpha = 0;
                mFrom = 0;
                mReverse = false;
                mTo = 255;
            
            } else {
            
                mAlpha = 255;
                mFrom = 255;
                mReverse = true;
                mTo = 0;
            
            }
            
            mDuration = mOriginalDuration = duration;
            mTransitionState = TRANSITION_STARTING;
            
            invalidateSelf();
            return;
        
        }
        
        mFrom = mAlpha;
        mTo = (mReverse ? 0 : 255);
        
        mReverse = !mReverse;
        mDuration = (int) (mReverse ? time - mStartTimeMillis :
                mOriginalDuration - (time - mStartTimeMillis));
        
        mTransitionState = TRANSITION_STARTING;
    
    }
    
    /**
     * Enables or disables the cross fade of the drawables. When cross fade
     * is disabled, the first drawable is always drawn opaque. With cross
     * fade enabled, the first drawable is drawn with the opposite alpha of
     * the second drawable. Cross fade is disabled by default.
     *
     * @param enabled True to enable cross fading, false otherwise.
     */
    public void setCrossFadeEnabled(boolean enabled) {
        mCrossFade = enabled;
    }
    
    /**
     * Begin the second layer on top of the first layer.
     *
     * @param durationMillis The length of the transition in milliseconds
     */
    public void startTransition(int durationMillis) {
    
        mAlpha = 0;
        mDuration = mOriginalDuration = durationMillis;
        mFrom = 0;
        mReverse = false;
        mTo = 255;
        mTransitionState = TRANSITION_STARTING;
        
        invalidateSelf();
    
    }
    
    static class TransitionState extends LayerState {
    
        TransitionState(TransitionState orig, TransitionDrawable owner, Resources res) {
            super(orig, owner, res);
        }
        
        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
        
        @Override
        public Drawable newDrawable() {
            return new TransitionDrawable(this, null, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res) {
            return new TransitionDrawable(this, res, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res, Theme theme) {
            return new TransitionDrawable(this, res, theme);
        }
    
    }

}