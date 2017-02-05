package robertapengelly.support.graphics.drawable;

import  android.content.res.Resources;
import  android.content.res.TypedArray;
import  android.content.res.Resources.Theme;
import  android.graphics.drawable.Drawable;
import  android.util.AttributeSet;
import  android.util.StateSet;

import  java.io.IOException;
import  java.util.Arrays;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

import  robertapengelly.support.lollipopdrawables.R;

/**
 * Lets you assign a number of graphic images to a single Drawable and swap out the visible item by a string
 * ID value.
 *
 * <p>It can be defined in an XML file with the <code>&lt;selector></code> element.
 * Each state Drawable is defined in a nested <code>&lt;item></code> element. For more
 * information, see the guide to <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html">Drawable Resources</a>.</p>
 */
public class StateListDrawable extends DrawableContainer {

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
    
    private boolean mMutated;
    private StateListState mStateListState;
    
    public StateListDrawable() {
        this(null, null);
    }
    
    /**
     * This constructor exists so subclasses can avoid calling the default
     * constructor and setting up a StateListDrawable-specific constant state.
     */
    StateListDrawable(StateListState state) {
    
        if (state != null)
            setConstantState(state);
    
    }
    
    private StateListDrawable(StateListState state, Resources res) {
    
        final StateListState newState = new StateListState(state, this, res);
        setConstantState(newState);
        
        onStateChange(getState());
    
    }
    
    /**
     * Add a new image/string ID to the set of images.
     *
     * @param stateSet - An array of resource Ids to associate with the image.
     *                   Switch to this image by calling setState().
     * @param drawable - The image to show.
     */
    public void addState(int[] stateSet, Drawable drawable) {
    
        if (drawable != null) {
        
            mStateListState.addStateSet(stateSet, drawable);
            
            // in case the new state matches our current state...
            onStateChange(getState());
        
        }
    
    }
    
    /**
     * Gets the number of states contained in this drawable.
     *
     * @return The number of states contained in this drawable.
     *
     * @see #getStateSet(int)
     * @see #getStateDrawable(int)
     */
    public int getStateCount() {
        return mStateListState.getChildCount();
    }
    
    /**
     * Gets the drawable at an index.
     *
     * @param index The index of the drawable.
     * @return The drawable at the index.
     *
     * @see #getStateCount()
     * @see #getStateSet(int)
     */
    public Drawable getStateDrawable(int index) {
        return mStateListState.getChild(index);
    }
    
    /**
     * Gets the index of the drawable with the provided state set.
     *
     * @param stateSet the state set to look up
     * @return the index of the provided state set, or -1 if not found
     *
     * @see #getStateDrawable(int)
     * @see #getStateSet(int)
     */
    public int getStateDrawableIndex(int[] stateSet) {
        return mStateListState.indexOfStateSet(stateSet);
    }
    
    StateListState getStateListState() {
        return mStateListState;
    }
    
    /**
     * Gets the state set at an index.
     *
     * @param index The index of the state set.
     * @return The state set at the index.
     *
     * @see #getStateCount()
     * @see #getStateDrawable(int)
     */
    public int[] getStateSet(int index) {
        return mStateListState.mStateSets[index];
    }
    
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        
        final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.StateListDrawable);
        
        super.inflateWithAttributes(a, R.styleable.StateListDrawable_android_visible);
        
        mStateListState.setConstantSize(a.getBoolean(R.styleable.StateListDrawable_android_constantSize, false));
        mStateListState.setEnterFadeDuration(a.getInt(R.styleable.StateListDrawable_android_enterFadeDuration, 0));
        mStateListState.setExitFadeDuration(a.getInt(R.styleable.StateListDrawable_android_exitFadeDuration, 0));
        mStateListState.setVariablePadding(a.getBoolean(R.styleable.StateListDrawable_android_variablePadding, false));
        
        setAutoMirrored(a.getBoolean(R.styleable.StateListDrawable_android_autoMirrored, false));
        setDither(a.getBoolean(R.styleable.StateListDrawable_android_dither, DEFAULT_DITHER));
        
        a.recycle();
        
        final int innerDepth = (parser.getDepth() + 1);
        
        int depth, type;
        
        while (((type = parser.next()) != XmlPullParser.END_DOCUMENT) && (((depth = parser.getDepth()) >= innerDepth)
            || (type != XmlPullParser.END_TAG))) {
            
            if (type != XmlPullParser.START_TAG)
                continue;
            
            if ((depth > innerDepth) || !parser.getName().equals("item"))
                continue;
            
            int drawableRes = 0;
            int i;
            int j = 0;
            
            final int numAttrs = attrs.getAttributeCount();
            
            int[] states = new int[numAttrs];
            
            for (i = 0; i < numAttrs; ++i) {
            
                final int stateResId = attrs.getAttributeNameResource(i);
                
                if (stateResId == 0)
                    break;
                
                if (stateResId == android.R.attr.drawable)
                    drawableRes = attrs.getAttributeResourceValue(i, 0);
                else
                    states[j++] = attrs.getAttributeBooleanValue(i, false) ? stateResId : -stateResId;
            
            }
            
            states = StateSet.trimStateSet(states, j);
            
            final Drawable dr;
            
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
            
            mStateListState.addStateSet(states, dr);
        
        }
        
        onStateChange(getState());
    
    }
    
    @Override
    public boolean isStateful() {
        return true;
    }
    
    @Override
    public Drawable mutate() {
    
        if (!mMutated && (super.mutate() == this)) {
        
            final int[][] sets = mStateListState.mStateSets;
            
            final int count = sets.length;
            mStateListState.mStateSets = new int[count][];
            
            for (int i = 0; i < count; ++i) {
            
                final int[] set = sets[i];
                
                if (set != null)
                    mStateListState.mStateSets[i] = set.clone();
            
            }
            
            mMutated = true;
        
        }
        
        return this;
    
    }
    
    @Override
    protected boolean onStateChange(int[] stateSet) {
    
        int idx = mStateListState.indexOfStateSet(stateSet);
        
        if (idx < 0)
            idx = mStateListState.indexOfStateSet(StateSet.WILD_CARD);
        
        if (selectDrawable(idx))
            return true;
        
        return super.onStateChange(stateSet);
    
    }
    
    void setConstantState(StateListState state) {
        super.setConstantState(state);
        
        mStateListState = state;
    
    }
    
    static class StateListState extends DrawableContainerState {
    
        int[][] mStateSets;
        
        StateListState(StateListState orig, StateListDrawable owner, Resources res) {
            super(orig, owner, res);
        
            if (orig != null)
                mStateSets = Arrays.copyOf(orig.mStateSets, orig.mStateSets.length);
            else
                mStateSets = new int[getCapacity()][];
        
        }
        
        int addStateSet(int[] stateSet, Drawable drawable) {
        
            final int pos = addChild(drawable);
            mStateSets[pos] = stateSet;
            
            return pos;
        
        }
        
        @Override
        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            
            final int[][] newStateSets = new int[newSize][];
            System.arraycopy(mStateSets, 0, newStateSets, 0, oldSize);
            
            mStateSets = newStateSets;
        
        }
        
        int indexOfStateSet(int[] stateSet) {
        
            final int N = getChildCount();
            final int[][] stateSets = mStateSets;
            
            for (int i = 0; i < N; ++i)
                if (StateSet.stateSetMatches(stateSets[i], stateSet))
                    return i;
            
            return -1;
        
        }
        
        @Override
        public Drawable newDrawable() {
            return new StateListDrawable(this, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res) {
            return new StateListDrawable(this, res);
        }
    
    }

}