package robertapengelly.support.graphics.drawable;

import  android.content.res.Resources;
import  android.content.res.TypedArray;
import  android.content.res.Resources.Theme;
import  android.graphics.drawable.Drawable;
import  android.os.SystemClock;
import  android.util.AttributeSet;

import  java.io.IOException;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

import  robertapengelly.support.lollipopdrawables.R;

/**
 * An object used to create frame-by-frame animations, defined by a series of Drawable objects,
 * which can be used as a View object's background.
 * <p>
 * The simplest way to create a frame-by-frame animation is to define the animation in an XML
 * file, placed in the res/drawable/ folder, and set it as the background to a View object. Then, call
 * {@link #start()} to run the animation.
 * <p>
 * An AnimationDrawable defined in XML consists of a single <code>&lt;animation-list></code> element,
 * and a series of nested <code>&lt;item></code> tags. Each item defines a frame of the animation.
 * See the example below.
 * </p>
 * <p>spin_animation.xml file in res/drawable/ folder:</p>
 * <pre>&lt;!-- Animation frames are wheel0.png -- wheel5.png files inside the
 * res/drawable/ folder --&gt;
 * &lt;animation-list android:id=&quot;@+id/selected&quot; android:oneshot=&quot;false&quot;&gt;
 *    &lt;item android:drawable=&quot;@drawable/wheel0&quot; android:duration=&quot;50&quot; /&gt;
 *    &lt;item android:drawable=&quot;@drawable/wheel1&quot; android:duration=&quot;50&quot; /&gt;
 *    &lt;item android:drawable=&quot;@drawable/wheel2&quot; android:duration=&quot;50&quot; /&gt;
 *    &lt;item android:drawable=&quot;@drawable/wheel3&quot; android:duration=&quot;50&quot; /&gt;
 *    &lt;item android:drawable=&quot;@drawable/wheel4&quot; android:duration=&quot;50&quot; /&gt;
 *    &lt;item android:drawable=&quot;@drawable/wheel5&quot; android:duration=&quot;50&quot; /&gt;
 * &lt;/animation-list&gt;</pre>
 *
 * <p>Here is the code to load and play this animation.</p>
 * <pre>
 * // Load the ImageView that will host the animation and
 * // set its background to our AnimationDrawable XML resource.
 * ImageView img = (ImageView)findViewById(R.id.spinning_wheel_image);
 * img.setBackgroundResource(R.drawable.spin_animation);
 *
 * // Get the background, which has been compiled to an AnimationDrawable object.
 * AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
 *
 * // Start the animation (looped playback by default).
 * frameAnimation.start();
 * </pre>
 *
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For more information about animating with {@code AnimationDrawable}, read the
 * <a href="{@docRoot}guide/topics/graphics/drawable-animation.html">Drawable Animation</a>
 * developer guide.</p>
 * </div>
 */
public class AnimationDrawable extends DrawableContainer implements Animatable, Runnable {

    private final AnimationState mAnimationState;
    
    /** Whether the drawable should animate when visible. */
    private boolean mAnimating;
    
    private boolean mMutated;
    
    /** Whether the drawable has an animation callback posted. */
    private boolean mRunning;
    
    /** The current frame, may be -1 when not animating. */
    private int mCurFrame = -1;
    
    public AnimationDrawable() {
        this(null, null);
    }
    
    private AnimationDrawable(AnimationState state, Resources res) {
    
        AnimationState as = new AnimationState(state, this, res);
        mAnimationState = as;
        
        setConstantState(as);
        
        if (state != null)
            setFrame(0, true, false);
    
    }
    
    /**
     * Add a frame to the animation
     *
     * @param frame    The frame to add
     * @param duration How long in milliseconds the frame should appear
     */
    public void addFrame(Drawable frame, int duration) {
    
        mAnimationState.addFrame(frame, duration);
        
        if (mCurFrame < 0)
            setFrame(0, true, false);
    
    }
    
    public int getCurrentIndex() {
        return super.getCurrentIndex();
    }
    
    /**
     * @return The duration in milliseconds of the frame at the
     * specified index
     */
    public int getDuration(int i) {
        return mAnimationState.mDurations[i];
    }
    
    /** @return The Drawable at the specified frame index */
    public Drawable getFrame(int index) {
        return mAnimationState.getChild(index);
    }
    
    /** @return The number of frames in the animation */
    public int getNumberOfFrames() {
        return mAnimationState.getChildCount();
    }
    
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.AnimationDrawable);
        
        super.inflateWithAttributes(a, R.styleable.AnimationDrawable_android_visible);
        
        mAnimationState.mOneShot = a.getBoolean(R.styleable.AnimationDrawable_android_oneshot, false);
        mAnimationState.setVariablePadding(a.getBoolean(R.styleable.AnimationDrawable_android_variablePadding, false));
        
        a.recycle();
        
        final int innerDepth = parser.getDepth()+1;
        
        int depth, type;
        
        while (((type = parser.next()) != XmlPullParser.END_DOCUMENT)
            && (((depth = parser.getDepth()) >= innerDepth) || (type != XmlPullParser.END_TAG))) {
            
            if (type != XmlPullParser.START_TAG)
                continue;
            
            if ((depth > innerDepth) || !parser.getName().equals("item"))
                continue;
            
            a = obtainAttributes(r, theme, attrs, R.styleable.AnimationDrawableItem);
            
            int duration = a.getInt(R.styleable.AnimationDrawableItem_android_duration, -1);
            
            if (duration < 0)
                throw new XmlPullParserException(parser.getPositionDescription()
                    + ": <item> tag requires a 'duration' attribute");
            
            int drawableRes = a.getResourceId(R.styleable.AnimationDrawableItem_android_drawable, 0);
            
            a.recycle();
            
            Drawable dr;
            
            if (drawableRes != 0)
                dr = LollipopDrawablesCompat.getDrawable(r, drawableRes, theme);
            else {
            
                //noinspection StatementWithEmptyBody
                while ((type = parser.next()) == XmlPullParser.TEXT);
                
                if (type != XmlPullParser.START_TAG)
                    throw new XmlPullParserException(parser.getPositionDescription()
                        + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                
                dr = LollipopDrawablesCompat.createFromXmlInner(r, parser, attrs, theme);
            
            }
            
            mAnimationState.addFrame(dr, duration);
            
            if (dr != null)
                dr.setCallback(this);
        
        }
        
        setFrame(0, true, false);
    
    }
    
    /** @return True of the animation will play once, false otherwise */
    public boolean isOneShot() {
        return mAnimationState.mOneShot;
    }
    
    /**
     * <p>Indicates whether the animation is currently running or not.</p>
     *
     * @return true if the animation is running, false otherwise
     */
    @Override
    public boolean isRunning() {
        return mRunning;
    }
    
    @Override
    public Drawable mutate() {
    
        if (!mMutated && (super.mutate() == this)) {
        
            mAnimationState.mDurations = mAnimationState.mDurations.clone();
            mMutated = true;
        
        }
        
        return this;
    
    }
    
    private void nextFrame(boolean unschedule) {
    
        int next = mCurFrame+1;
        
        final int N = mAnimationState.getChildCount();
        
        if (next >= N)
            next = 0;
        
        setFrame(next, unschedule, (!mAnimationState.mOneShot || (next < (N - 1))));
    
    }
    
    /**
     * <p>This method exists for implementation purpose only and should not be
     * called directly. Invoke {@link #start()} instead.</p>
     *
     * @see #start()
     */
    @Override
    public void run() {
        nextFrame(false);
    }
    
    public void setCurrentIndex(int index) {
        selectDrawable(index);
    }
    
    private void setFrame(int frame, boolean unschedule, boolean animate) {
    
        if (frame >= mAnimationState.getChildCount())
            return;
        
        mAnimating = animate;
        mCurFrame = frame;
        
        selectDrawable(frame);
        
        if (unschedule || animate)
            unscheduleSelf(this);
        
        if (animate) {
        
            // Unscheduling may have clobbered these values; restore them
            mCurFrame = frame;
            mRunning = true;
            
            scheduleSelf(this, (SystemClock.uptimeMillis() + mAnimationState.mDurations[frame]));
        
        }
    
    }
    
    /**
     * Sets whether the animation should play once or repeat.
     *
     * @param oneShot Pass true if the animation should only play once
     */
    public void setOneShot(boolean oneShot) {
        mAnimationState.mOneShot = oneShot;
    }
    
    /**
     * Sets whether this AnimationDrawable is visible.
     *
     * <p>When the drawable becomes invisible, it will pause its animation. A
     * subsequent change to visible with <code>restart</code> set to true will
     * restart the animation from the first frame. If <code>restart</code> is
     * false, the animation will resume from the most recent frame.</p>
     *
     * @param visible true if visible, false otherwise
     * @param restart when visible, true to force the animation to restart
     *                from the first frame
     *
     * @return true if the new visibility is different than its previous state
     */
    @Override
    public boolean setVisible(boolean visible, boolean restart) {
    
        final boolean changed = super.setVisible(visible, restart);
        
        if (visible) {
        
            if (restart || changed) {
            
                boolean startFromZero = (restart || (mCurFrame < 0) || (mCurFrame >= mAnimationState.getChildCount()));
                setFrame(startFromZero ? 0 : mCurFrame, true, mAnimating);
            
            }
        
        } else
            unscheduleSelf(this);
        
        return changed;
    
    }
    
    /**
     * <p>Starts the animation, looping if necessary. This method has no effect
     * if the animation is running. Do not call this in the {@link android.app.Activity#onCreate}
     * method of your activity, because the {@link robertapengelly.support.graphics.drawable.AnimationDrawable} is
     * not yet fully attached to the window. If you want to play
     * the animation immediately, without requiring interaction, then you might want to call it
     * from the {@link android.app.Activity#onWindowFocusChanged} method in your activity,
     * which will get called when Android brings your window into focus.</p>
     *
     * @see #isRunning()
     * @see #stop()
     */
    @Override
    public void start() {
    
        mAnimating = true;
        
        if (!isRunning())
            run();
    
    }
    
    /**
     * <p>Stops the animation. This method has no effect if the animation is
     * not running.</p>
     *
     * @see #isRunning()
     * @see #start()
     */
    @Override
    public void stop() {
    
        mAnimating = false;
        
        if (isRunning())
            unscheduleSelf(this);
    
    }
    
    @Override
    public void unscheduleSelf(Runnable what) {
    
        mCurFrame = -1;
        mRunning = false;
        
        super.unscheduleSelf(what);
    
    }
    
    private final static class AnimationState extends DrawableContainerState {
    
        private boolean mOneShot;
        private int[] mDurations;
        
        AnimationState(AnimationState orig, AnimationDrawable owner, Resources res) {
            super(orig, owner, res);
            
            if (orig != null) {
            
                mDurations = orig.mDurations;
                mOneShot = orig.mOneShot;
            
            } else {
            
                mDurations = new int[getCapacity()];
                mOneShot = true;
            
            }
        
        }
        
        public void addFrame(Drawable dr, int dur) {
        
            // Do not combine the following. The array index must be evaluated before
            // the array is accessed because super.addChild(dr) has a side effect on mDurations.
            int pos = super.addChild(dr);
            mDurations[pos] = dur;
        
        }
        
        @Override
        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            
            int[] newDurations = new int[newSize];
            
            System.arraycopy(mDurations, 0, newDurations, 0, oldSize);
            
            mDurations = newDurations;
        
        }
        
        @Override
        public Drawable newDrawable() {
            return new AnimationDrawable(this, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res) {
            return new AnimationDrawable(this, res);
        }
    
    }

}