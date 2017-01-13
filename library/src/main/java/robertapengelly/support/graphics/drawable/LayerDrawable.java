package robertapengelly.support.graphics.drawable;

import  android.annotation.TargetApi;
import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.Resources.Theme;
import  android.content.res.TypedArray;
import  android.graphics.Canvas;
import  android.graphics.ColorFilter;
import  android.graphics.Outline;
import  android.graphics.PixelFormat;
import  android.graphics.PorterDuff.Mode;
import  android.graphics.Rect;
import  android.graphics.drawable.Drawable;
import  android.os.Build;
import  android.util.AttributeSet;
import  android.util.TypedValue;
import  android.view.View;

import  java.io.IOException;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

import  robertapengelly.support.lollipopdrawables.R;

/**
 * A Drawable that manages an array of other Drawables. These are drawn in array
 * order, so the element with the largest index will be drawn on top.
 *
 * <p>It can be defined in an XML file with the <code>&lt;layer-list></code> element.
 * Each Drawable in the layer is defined in a nested <code>&lt;item></code>.</p>
 *
 * <p>For more information, see the guide to
 * <a href="{@docRoot}guide/topics/resources/drawable-resource.html">Drawable Resources</a>.</p>
 *
 * @attr ref R.styleable#LayerDrawable_paddingMode
 * @attr ref R.styleable#LayerDrawableItem_left
 * @attr ref R.styleable#LayerDrawableItem_top
 * @attr ref R.styleable#LayerDrawableItem_right
 * @attr ref R.styleable#LayerDrawableItem_bottom
 * @attr ref R.styleable#LayerDrawableItem_drawable
 * @attr ref R.styleable#LayerDrawableItem_id
 */
public class LayerDrawable extends LollipopDrawable implements Drawable.Callback {

    /**
     * Padding mode used to nest each layer inside the padding of the previous layer.
     *
     * @see #setPaddingMode(int)
     */
    public static final int PADDING_MODE_NEST = 0;
    
    /**
     * Padding mode used to stack each layer directly atop the previous layer.
     *
     * @see #setPaddingMode(int)
     */
    public static final int PADDING_MODE_STACK = 1;
    
    private final Rect mTmpRect = new Rect();
    
    private Rect mHotspotBounds;
    
    private int mOpacityOverride = PixelFormat.UNKNOWN;
    
    private boolean mMutated;
    
    private int[] mPaddingB, mPaddingL, mPaddingR, mPaddingT;
    
    LayerState mLayerState;
    
    LayerDrawable() {
        this((LayerState) null, null, null);
    }
    
    /**
     * Create a new layer drawable with the list of specified layers.
     *
     * @param layers A list of drawables to use as layers in this new drawable.
     */
    public LayerDrawable(Drawable[] layers) {
        this(layers, null);
    }
    
    /**
     * Create a new layer drawable with the specified list of layers and the
     * specified constant state.
     *
     * @param layers The list of layers to add to this drawable.
     * @param state  The constant drawable state.
     */
    LayerDrawable(Drawable[] layers, LayerState state) {
        this(state, null, null);
        
        int length = layers.length;
        ChildDrawable[] r = new ChildDrawable[length];
        
        for (int i = 0; i < length; ++i) {
        
            r[i] = new ChildDrawable();
            r[i].mDrawable = layers[i];
            
            layers[i].setCallback(this);
            mLayerState.mChildrenChangingConfigurations |= layers[i].getChangingConfigurations();
        
        }
        
        mLayerState.mChildren = r;
        mLayerState.mNum = length;
        
        ensurePadding();
    }
    
    LayerDrawable(LayerState state, Resources res, Theme theme) {
    
        final LayerState as = createConstantState(state, res);
        mLayerState = as;
        
        if (as.mNum > 0)
            ensurePadding();
        
        if (theme != null && canApplyTheme())
            applyTheme(theme);
    
    }
    
    LayerState createConstantState(LayerState state, Resources res) {
        return new LayerState(state, this, res);
    }
    
    void addLayer(ChildDrawable layer) {
    
        final LayerState st = mLayerState;
        
        final int i = st.mNum;
        final int N = ((st.mChildren != null) ? st.mChildren.length : 0);
        
        if (i >= N) {
        
            final ChildDrawable[] nu = new ChildDrawable[N + 10];
            
            if (i > 0)
                System.arraycopy(st.mChildren, 0, nu, 0, i);
            
            st.mChildren = nu;
        
        }
        
        st.mChildren[i] = layer;
        st.mNum++;
        
        st.invalidateCache();
    
    }
    
    /**
     * Add a new layer to this drawable. The new layer is identified by an id.
     *
     * @param layer      The drawable to add as a layer.
     * @param themeAttrs Theme attributes extracted from the layer.
     * @param id         The id of the new layer.
     * @param left       The left padding of the new layer.
     * @param top        The top padding of the new layer.
     * @param right      The right padding of the new layer.
     * @param bottom     The bottom padding of the new layer.
     */
    ChildDrawable addLayer(Drawable layer, TypedValue[] themeAttrs, int id, int left, int top, int right, int bottom) {
    
        final ChildDrawable childDrawable = new ChildDrawable();
        childDrawable.mDrawable = layer;
        childDrawable.mId = id;
        childDrawable.mThemeAttrs = themeAttrs;
        childDrawable.mInsetB = bottom;
        childDrawable.mInsetL = left;
        childDrawable.mInsetR = right;
        childDrawable.mInsetT = top;
        
        if (Build.VERSION.SDK_INT >= 19)
            childDrawable.mDrawable.setAutoMirrored(isAutoMirrored());
        
        addLayer(childDrawable);
        
        mLayerState.mChildrenChangingConfigurations |= layer.getChangingConfigurations();
        layer.setCallback(this);
        
        return childDrawable;
    
    }
    
    @Override
    public void applyTheme(Theme t) {
        super.applyTheme(t);
        
        if (mLayerState == null)
            return;
        
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            final ChildDrawable layer = array[i];
            
            if (layer.mThemeAttrs != null)
                updateLayerFromTypedArray(t, layer.mThemeAttrs, layer, null);
            
            final Drawable d = layer.mDrawable;
            
            if (LollipopDrawablesCompat.canApplyTheme(d))
                LollipopDrawablesCompat.applyTheme(d, t);
        
        }
        
        ensurePadding();
        onStateChange(getState());
    
    }
    
    @Override
    public boolean canApplyTheme() {
    
        final LayerState state = mLayerState;
        
        if (state == null)
            return false;
        
        if (state.mThemeAttrs != null)
            return true;
        
        final ChildDrawable[] array = state.mChildren;
        final int N = state.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            final ChildDrawable layer = array[i];
            
            if ((layer.mThemeAttrs != null) || LollipopDrawablesCompat.canApplyTheme(layer.mDrawable))
                return true;
        
        }
        
        return false;
    
    }
    
    private void computeNestedPadding(Rect padding) {
    
        padding.bottom = padding.left = padding.right = padding.top = 0;
        
        // Add all the padding.
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            refreshChildPadding(i, array[i]);
            
            padding.bottom += mPaddingB[i];
            padding.left += mPaddingL[i];
            padding.right += mPaddingR[i];
            padding.top += mPaddingT[i];
        
        }
    
    }
    
    private void computeStackedPadding(Rect padding) {
    
        padding.bottom = padding.left = padding.right = padding.top = 0;
        
        // Take the max padding.
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            refreshChildPadding(i, array[i]);
            
            padding.bottom = Math.max(padding.bottom, mPaddingB[i]);
            padding.left = Math.max(padding.left, mPaddingL[i]);
            padding.right = Math.max(padding.right, mPaddingR[i]);
            padding.top = Math.max(padding.top, mPaddingT[i]);
        
        }
    
    }
    
    @Override
    public void draw(Canvas canvas) {
    
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i)
            array[i].mDrawable.draw(canvas);
    
    }
    
    /** Ensures the child padding caches are large enough. */
    void ensurePadding() {
    
        final int N = mLayerState.mNum;
        
        if ((mPaddingL != null) && (mPaddingL.length >= N))
            return;
        
        mPaddingB = new int[N];
        mPaddingL = new int[N];
        mPaddingR = new int[N];
        mPaddingT = new int[N];
    
    }
    
    /**
     * Looks for a layer with the given ID and returns its {@link Drawable}.
     *
     * <p>If multiple layers are found for the given ID, returns the
     * {@link Drawable} for the matching layer at the highest index.</p>
     *
     * @param id The layer ID to search for.
     * @return The {@link Drawable} for the highest-indexed layer that has the
     *         given ID, or null if not found.
     */
    public Drawable findDrawableByLayerId(int id) {
    
        final ChildDrawable[] layers = mLayerState.mChildren;
        
        for (int i = (mLayerState.mNum - 1); i >= 0; --i)
            if (layers[i].mId == id)
                return layers[i].mDrawable;
        
        return null;
    
    }
    
    @Override
    public int getAlpha() {
    
        final ChildDrawable[] array = mLayerState.mChildren;
        
        if (mLayerState.mNum > 0) {
        
            Drawable d = array[0].mDrawable;
            
            // All layers should have the same alpha set on them - just return the first one
            if (Build.VERSION.SDK_INT >= 19)
                return d.getAlpha();
            else if (d instanceof LollipopDrawable)
                //noinspection RedundantCast
                return ((LollipopDrawable) d).getAlpha();
            
            //noinspection Range
            return -1;
        
        } else
            return super.getAlpha();
    
    }
    
    @Override
    public int getChangingConfigurations() {
        return (super.getChangingConfigurations()
            | mLayerState.mChangingConfigurations | mLayerState.mChildrenChangingConfigurations);
    }
    
    @Override
    public ConstantState getConstantState() {
    
        if (mLayerState.canConstantState()) {
        
            mLayerState.mChangingConfigurations = getChangingConfigurations();
            return mLayerState;
        
        }
        
        return null;
    
    }
    
    /**
     * Returns the drawable at the specified layer index.
     *
     * @param index The layer index of the drawable to retrieve.
     * @return The {@link android.graphics.drawable.Drawable} at the specified layer index.
     */
    public Drawable getDrawable(int index) {
        return mLayerState.mChildren[index].mDrawable;
    }
    
    @Override
    public void getHotspotBounds(Rect outRect) {
    
        if (mHotspotBounds != null)
            outRect.set(mHotspotBounds);
        else
            super.getHotspotBounds(outRect);
    
    }
    
    /**
     * Returns the id of the specified layer.
     *
     * @param index The index of the layer.
     * @return The id of the layer or {@link android.view.View#NO_ID} if the layer has no id.
     */
    public int getId(int index) {
        return mLayerState.mChildren[index].mId;
    }
    
    @Override
    public int getIntrinsicHeight() {
    
        int height = -1;
        int padB = 0;
        int padT = 0;
        
        final boolean nest = (mLayerState.mPaddingMode == PADDING_MODE_NEST);
        
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            final ChildDrawable r = array[i];
            final int h = (r.mDrawable.getIntrinsicHeight() + r.mInsetT + r.mInsetB + padT + padB);
            
            if (h > height)
                height = h;
            
            if (nest) {
            
                padB += mPaddingB[i];
                padT += mPaddingT[i];
            
            }
        
        }
        
        return height;
    }
    
    @Override
    public int getIntrinsicWidth() {
    
        int padL = 0;
        int padR = 0;
        int width = -1;
        
        final boolean nest = (mLayerState.mPaddingMode == PADDING_MODE_NEST);
        
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            final ChildDrawable r = array[i];
            final int w = (r.mDrawable.getIntrinsicWidth() + r.mInsetL + r.mInsetR + padL + padR);
            
            if (w > width)
                width = w;
            
            if (nest) {
            
                padL += mPaddingL[i];
                padR += mPaddingR[i];
            
            }
        
        }
        
        return width;
    
    }
    
    /**
     * Returns the number of layers contained within this.
     * @return The number of layers.
     */
    public int getNumberOfLayers() {
        return mLayerState.mNum;
    }
    
    @Override
    public int getOpacity() {
    
        if (mOpacityOverride != PixelFormat.UNKNOWN)
            return mOpacityOverride;
        
        //noinspection WrongConstant
        return mLayerState.getOpacity();
    
    }
    
    @Override
    @TargetApi(21)
    public void getOutline(Outline outline) {
    
        final LayerState state = mLayerState;
        
        final ChildDrawable[] children = state.mChildren;
        final int N = state.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            children[i].mDrawable.getOutline(outline);
            
            if (!outline.isEmpty())
                return;
        
        }
    
    }
    
    @Override
    public boolean getPadding(Rect padding) {
    
        if (mLayerState.mPaddingMode == PADDING_MODE_NEST)
            computeNestedPadding(padding);
        else
            computeStackedPadding(padding);
        
        return ((padding.bottom != 0) || (padding.left != 0) || (padding.right != 0) || (padding.top != 0));
    
    }
    
    /**
     * @return the current padding mode
     * @see #setPaddingMode(int)
     * @attr ref android.R.styleable#LayerDrawable_paddingMode
     */
    public int getPaddingMode() {
      return mLayerState.mPaddingMode;
    }
    
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        
        final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.LayerDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
        
        inflateLayers(r, parser, attrs, theme);
        
        ensurePadding();
        onStateChange(getState());
    
    }
    
    /** Inflates child layers using the specified parser. */
    private void inflateLayers(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        
        final LayerState state = mLayerState;
        final int innerDepth = (parser.getDepth() + 1);
        
        int depth, type;
        
        while (((type = parser.next()) != XmlPullParser.END_DOCUMENT) && (((depth = parser.getDepth()) >= innerDepth)
            || (type != XmlPullParser.END_TAG))) {
            
            if (type != XmlPullParser.START_TAG)
                continue;
            
            if ((depth > innerDepth) || !parser.getName().equals("item"))
                continue;
            
            final ChildDrawable layer = new ChildDrawable();
            
            final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.LayerDrawableItem);
            updateLayerFromTypedArray(theme, null, layer, a);
            a.recycle();
            
            if (layer.mDrawable == null) {
            
                while ((type = parser.next()) == XmlPullParser.TEXT);
                
                if (type != XmlPullParser.START_TAG)
                    throw new XmlPullParserException(parser.getPositionDescription()
                            + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                
                layer.mDrawable = LollipopDrawablesCompat.createFromXmlInner(r, parser, attrs, theme);
            
            }
            
            if (layer.mDrawable != null) {
            
                state.mChildrenChangingConfigurations |= layer.mDrawable.getChangingConfigurations();
                layer.mDrawable.setCallback(this);
            
            }
            
            addLayer(layer);
        
        }
    
    }
    
    @Override
    public boolean isAutoMirrored() {
        return mLayerState.mAutoMirrored;
    }
    
    @Override
    public boolean isStateful() {
        return mLayerState.isStateful();
    }
    
    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }
    
    @Override
    public Drawable mutate() {
    
        if (!mMutated && (super.mutate() == this)) {
        
            mLayerState = createConstantState(mLayerState, null);
            
            final ChildDrawable[] array = mLayerState.mChildren;
            final int N = mLayerState.mNum;
            
            for (int i = 0; i < N; ++i)
                array[i].mDrawable.mutate();
            
            mMutated = true;
        
        }
        
        return this;
    
    }
    
    /**
     * Obtains styled attributes from the theme, if available, or unstyled
     * resources if the theme is null.
     */
    static TypedArray obtainAttributes(Resources res, Theme theme, AttributeSet set, int[] attrs) {
    
        if (theme == null)
            return res.obtainAttributes(set, attrs);
        
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    
    }
    
    @Override
    protected void onBoundsChange(Rect bounds) {
    
        int padB = 0;
        int padL = 0;
        int padR = 0;
        int padT = 0;
        
        final boolean nest = (mLayerState.mPaddingMode == PADDING_MODE_NEST);
        
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            final ChildDrawable r = array[i];
            r.mDrawable.setBounds((bounds.left + r.mInsetL + padL), (bounds.top + r.mInsetT + padT),
                (bounds.right - r.mInsetR + padR), (bounds.bottom - r.mInsetB + padB));
            
            if (nest) {
            
                padB += mPaddingB[i];
                padL += mPaddingL[i];
                padR += mPaddingR[i];
                padT += mPaddingT[i];
            
            }
        
        }
    
    }
    
    @Override
    protected boolean onLevelChange(int level) {
    
        boolean changed = false;
        boolean paddingChanged = false;
        
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            final ChildDrawable r = array[i];
            
            if (r.mDrawable.setLevel(level))
                changed = true;
            
            if (refreshChildPadding(i, r))
                paddingChanged = true;
        
        }
        
        if (paddingChanged)
            onBoundsChange(getBounds());
        
        return changed;
    
    }
    
    @Override
    protected boolean onStateChange(int[] state) {
    
        boolean changed = false;
        boolean paddingChanged = false;
        
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            final ChildDrawable r = array[i];
            
            if (r.mDrawable.isStateful() && r.mDrawable.setState(state))
                changed = true;
            
            if (refreshChildPadding(i, r))
                paddingChanged = true;
        
        }
        
        if (paddingChanged)
            onBoundsChange(getBounds());
        
        return changed;
    
    }
    
    /**
     * Refreshes the cached padding values for the specified child.
     *
     * @return true if the child's padding has changed
     */
    private boolean refreshChildPadding(int i, ChildDrawable r) {
    
        final Rect rect = mTmpRect;
        r.mDrawable.getPadding(rect);
        
        if ((rect.bottom != mPaddingB[i]) || (rect.left != mPaddingL[i])
            || (rect.right != mPaddingR[i]) || (rect.top != mPaddingT[i])) {
            
            mPaddingB[i] = rect.bottom;
            mPaddingL[i] = rect.left;
            mPaddingR[i] = rect.right;
            mPaddingT[i] = rect.top;
            
            return true;
        
        }
        
        return false;
    
    }
    
    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }
    
    @Override
    public void setAlpha(int alpha) {
    
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i)
            array[i].mDrawable.setAlpha(alpha);
    
    }
    
    @Override
    public void setAutoMirrored(boolean mirrored) {
    
        mLayerState.mAutoMirrored = mirrored;
        
        if (Build.VERSION.SDK_INT < 19)
            return;
        
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i)
            array[i].mDrawable.setAutoMirrored(mirrored);
    
    }
    
    @Override
    public void setColorFilter(ColorFilter cf) {
    
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i)
            array[i].mDrawable.setColorFilter(cf);
    
    }
    
    @Override
    public void setDither(boolean dither) {
    
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i)
            array[i].mDrawable.setDither(dither);
    
    }
    
    /**
     * Sets (or replaces) the {@link Drawable} for the layer with the given id.
     *
     * @param id       The layer ID to search for.
     * @param drawable The replacement {@link Drawable}.
     * @return Whether the {@link Drawable} was replaced (could return false if
     *         the id was not found).
     */
    public boolean setDrawableByLayerId(int id, Drawable drawable) {
    
        final ChildDrawable[] layers = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            final ChildDrawable childDrawable = layers[i];
            
            if (childDrawable.mId == id) {
            
                if (childDrawable.mDrawable != null) {
                
                    if (drawable != null) {
                    
                        final Rect bounds = childDrawable.mDrawable.getBounds();
                        drawable.setBounds(bounds);
                    
                    }
                    
                    childDrawable.mDrawable.setCallback(null);
                
                }
                
                if (drawable != null)
                    drawable.setCallback(this);
                
                childDrawable.mDrawable = drawable;
                mLayerState.invalidateCache();
                
                return true;
            
            }
        
        }
        
        return false;
    
    }
    
    @Override
    public void setHotspot(float x, float y) {
    
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            Drawable d = array[i].mDrawable;
            
            if (Build.VERSION.SDK_INT >= 21)
                d.setHotspot(x, y);
            else if (d instanceof LollipopDrawable)
                //noinspection RedundantCast
                ((LollipopDrawable ) d).setHotspot(x, y);
        
        }
    
    }
    
    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
    
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            Drawable d = array[i].mDrawable;
            
            if (Build.VERSION.SDK_INT >= 21)
                d.setHotspotBounds(left, top, right, bottom);
            else if (d instanceof LollipopDrawable)
                //noinspection RedundantCast
                ((LollipopDrawable ) d).setHotspotBounds(left, top, right, bottom);
        
        }
        
        if (mHotspotBounds == null)
            mHotspotBounds = new Rect(left, top, right, bottom);
        else
            mHotspotBounds.set(left, top, right, bottom);
    
    }
    
    /**
     * Sets the ID of a layer.
     *
     * @param index The index of the layer which will received the ID.
     * @param id    The ID to assign to the layer.
     */
    public void setId(int index, int id) {
        mLayerState.mChildren[index].mId = id;
    }
    
    /**
     * Specifies the insets in pixels for the drawable at the specified index.
     *
     * @param index the index of the drawable to adjust
     * @param l     number of pixels to add to the left bound
     * @param t     number of pixels to add to the top bound
     * @param r     number of pixels to subtract from the right bound
     * @param b     number of pixels to subtract from the bottom bound
     */
    public void setLayerInset(int index, int l, int t, int r, int b) {
    
        final ChildDrawable childDrawable = mLayerState.mChildren[index];
        childDrawable.mInsetB = b;
        childDrawable.mInsetL = l;
        childDrawable.mInsetR = r;
        childDrawable.mInsetT = t;
    
    }
    
    /**
     * Sets the opacity of this drawable directly, instead of collecting the
     * states from the layers
     *
     * @param opacity The opacity to use, or {@link PixelFormat#UNKNOWN
     *                PixelFormat.UNKNOWN} for the default behavior
     * @see PixelFormat#UNKNOWN
     * @see PixelFormat#TRANSLUCENT
     * @see PixelFormat#TRANSPARENT
     * @see PixelFormat#OPAQUE
     */
    public void setOpacity(int opacity) {
        mOpacityOverride = opacity;
    }
    
    /**
     * Specifies how layer padding should affect the bounds of subsequent
     * layers. The default value is {@link #PADDING_MODE_NEST}.
     *
     * @param mode padding mode, one of:
     *            <ul>
     *                <li>{@link #PADDING_MODE_NEST} to nest each layer inside the
     *                    padding of the previous layer
     *                <li>{@link #PADDING_MODE_STACK} to stack each layer directly
     *                    atop the previous layer
     *            </ul>
     * @see #getPaddingMode()
     * @attr ref android.R.styleable#LayerDrawable_paddingMode
     */
    public void setPaddingMode(int mode) {
    
        if (mLayerState.mPaddingMode != mode)
            mLayerState.mPaddingMode = mode;
    
    }
    
    @Override
    public void setTintList(ColorStateList tint) {
    
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            Drawable d = array[i].mDrawable;
            
            if (Build.VERSION.SDK_INT >= 21)
                d.setTintList(tint);
            else if (d instanceof LollipopDrawable)
                //TODO: Add tintList without requiring cast
                //noinspection RedundantCast
                ((LollipopDrawable ) d).setTintList(tint);
        
        }
    
    }
    
    @Override
    public void setTintMode(Mode tintMode) {
    
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i) {
        
            Drawable d = array[i].mDrawable;
            
            if (Build.VERSION.SDK_INT >= 21)
                d.setTintMode(tintMode);
            else if (d instanceof LollipopDrawable)
                //TODO: Add tintMode without requiring cast
                //noinspection RedundantCast
                ((LollipopDrawable ) d).setTintMode(tintMode);
        
        }
    
    }
    
    @Override
    public boolean setVisible(boolean visible, boolean restart) {
    
        final boolean changed = super.setVisible(visible, restart);
        
        final ChildDrawable[] array = mLayerState.mChildren;
        final int N = mLayerState.mNum;
        
        for (int i = 0; i < N; ++i)
            array[i].mDrawable.setVisible(visible, restart);
        
        return changed;
    
    }
    
    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }
    
    private void updateLayerFromTypedArray(Theme theme, TypedValue[] extracted, ChildDrawable layer, TypedArray a) {
    
        final LayerState state = mLayerState;
        
        // Account for any configuration changes.
        state.mChildrenChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        layer.mThemeAttrs = TypedArrayCompat.extractThemeAttrs(a);
        
        layer.mId = TypedArrayCompat.getResourceId(theme, a, extracted,
            R.styleable.LayerDrawableItem_android_id, layer.mId);
        
        layer.mInsetB = TypedArrayCompat.getDimensionPixelOffset(theme, a, extracted,
            R.styleable.LayerDrawableItem_android_bottom, layer.mInsetB);
        layer.mInsetL = TypedArrayCompat.getDimensionPixelOffset(theme, a, extracted,
            R.styleable.LayerDrawableItem_android_left, layer.mInsetL);
        layer.mInsetR = TypedArrayCompat.getDimensionPixelOffset(theme, a, extracted,
            R.styleable.LayerDrawableItem_android_right, layer.mInsetR);
        layer.mInsetT = TypedArrayCompat.getDimensionPixelOffset(theme, a, extracted,
            R.styleable.LayerDrawableItem_android_top, layer.mInsetT);
        
        final Drawable dr = TypedArrayCompat.getDrawable(theme, a, extracted,
            R.styleable.LayerDrawableItem_android_drawable);
        
        if (dr != null)
            layer.mDrawable = dr;
    
    }
    
    /** Initializes the constant state from the values in the typed array. */
    private void updateStateFromTypedArray(TypedArray a) {
    
        final LayerState state = mLayerState;
        
        // Account for any configuration changes.
        state.mChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        state.mThemeAttrs = TypedArrayCompat.extractThemeAttrs(a);
        
        mOpacityOverride = a.getInt(R.styleable.LayerDrawable_android_opacity, mOpacityOverride);
        
        state.mAutoMirrored = a.getBoolean(R.styleable.LayerDrawable_android_autoMirrored, state.mAutoMirrored);
        state.mPaddingMode = a.getInteger(R.styleable.LayerDrawable_android_paddingMode, state.mPaddingMode);
    
    }
    
    static class ChildDrawable {
    
        Drawable mDrawable;
        int mId = View.NO_ID;
        int mInsetB, mInsetL, mInsetR, mInsetT;
        TypedValue[] mThemeAttrs;
        
        ChildDrawable() {}
        
        ChildDrawable(ChildDrawable orig, LayerDrawable owner, Resources res) {
        
            if (res != null)
                mDrawable = orig.mDrawable.getConstantState().newDrawable(res);
            else
                mDrawable = orig.mDrawable.getConstantState().newDrawable();
            
            mDrawable.setBounds(orig.mDrawable.getBounds());
            mDrawable.setCallback(owner);
            mDrawable.setLevel(orig.mDrawable.getLevel());
            
            if (Build.VERSION.SDK_INT >= 23)
                mDrawable.setLayoutDirection(orig.mDrawable.getLayoutDirection());
            
            mId = orig.mId;
            
            mInsetB = orig.mInsetB;
            mInsetL = orig.mInsetL;
            mInsetR = orig.mInsetR;
            mInsetT = orig.mInsetT;
            
            mThemeAttrs = orig.mThemeAttrs;
        
        }
    
    }
    
    static class LayerState extends ConstantState {
    
        private boolean mAutoMirrored = false;
        private boolean mHaveIsStateful, mHaveOpacity, mIsStateful;
        private int mOpacity;
        private int mPaddingMode = PADDING_MODE_NEST;
        
        int mChangingConfigurations, mChildrenChangingConfigurations, mNum;
        ChildDrawable[] mChildren;
        TypedValue[] mThemeAttrs;
        
        LayerState(LayerState orig, LayerDrawable owner, Resources res) {
        
            if (orig != null) {
            
                final ChildDrawable[] origChildDrawable = orig.mChildren;
                final int N = orig.mNum;
                
                mChildren = new ChildDrawable[N];
                mNum = N;
                
                mChangingConfigurations = orig.mChangingConfigurations;
                mChildrenChangingConfigurations = orig.mChildrenChangingConfigurations;
                
                for (int i = 0; i < N; ++i) {
                
                    final ChildDrawable or = origChildDrawable[i];
                    mChildren[i] = new ChildDrawable(or, owner, res);
                
                }
                
                mAutoMirrored = orig.mAutoMirrored;
                mHaveIsStateful = orig.mHaveIsStateful;
                mHaveOpacity = orig.mHaveOpacity;
                mIsStateful = orig.mIsStateful;
                mOpacity = orig.mOpacity;
                mPaddingMode = orig.mPaddingMode;
                mThemeAttrs = orig.mThemeAttrs;
            
            } else {
            
                mChildren = null;
                mNum = 0;
            
            }
        
        }
        
        @Override
        public boolean canApplyTheme() {
            return (mThemeAttrs != null);
        }
        
        final boolean canConstantState() {
        
            final ChildDrawable[] array = mChildren;
            final int N = mNum;
            
            for (int i = 0; i < N; ++i)
                if (array[i].mDrawable.getConstantState() == null)
                    return false;
            
            // Don't cache the result, this method is not called very often.
            return true;
        
        }
        
        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
        
        final int getOpacity() {
        
            if (mHaveOpacity)
                return mOpacity;
            
            final ChildDrawable[] array = mChildren;
            final int N = mNum;
            
            int op = ((N > 0) ? array[0].mDrawable.getOpacity() : PixelFormat.TRANSPARENT);
            
            for (int i = 1; i < N; ++i)
                op = Drawable.resolveOpacity(op, array[i].mDrawable.getOpacity());
            
            mHaveOpacity = true;
            mOpacity = op;
            
            return op;
        
        }
        
        void invalidateCache() {
        
            mHaveIsStateful = false;
            mHaveOpacity = false;
        
        }
        
        final boolean isStateful() {
        
            if (mHaveIsStateful)
                return mIsStateful;
            
            final ChildDrawable[] array = mChildren;
            final int N = mNum;
            
            boolean isStateful = false;
            
            for (int i = 0; i < N; ++i) {
            
                if (array[i].mDrawable.isStateful()) {
                
                    isStateful = true;
                    break;
                
                }
            
            }
            
            mHaveIsStateful = true;
            mIsStateful = isStateful;
            
            return isStateful;
        
        }
        
        @Override
        public Drawable newDrawable() {
            return new LayerDrawable(this, null, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res) {
            return new LayerDrawable(this, res, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res, Theme theme) {
            return new LayerDrawable(this, res, theme);
        }
    
    }

}