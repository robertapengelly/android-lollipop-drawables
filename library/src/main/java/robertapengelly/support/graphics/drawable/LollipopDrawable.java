package robertapengelly.support.graphics.drawable;

import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.Resources.Theme;
import  android.content.res.TypedArray;
import  android.graphics.Color;
import  android.graphics.ColorFilter;
import  android.graphics.PorterDuff;
import  android.graphics.PorterDuff.Mode;
import  android.graphics.PorterDuffColorFilter;
import  android.graphics.Rect;
import  android.graphics.drawable.Drawable;
import  android.util.AttributeSet;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

import  java.io.IOException;

/** This drawable was created to support old API's */
public abstract class LollipopDrawable extends Drawable {

    static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;
    
    private boolean mVisible = true;
    
    private ColorFilter mColorFilter;
    
    /** Applies the specified theme to this Drawable and its children. */
    public void applyTheme(Resources.Theme t) {}
    
    public boolean canApplyTheme() {
        return false;
    }
    
    /**
     * Gets the current alpha value for the drawable. 0 means fully transparent,
     * 255 means fully opaque. This method is implemented by
     * Drawable subclasses and the value returned is specific to how that class treats alpha.
     * The default return value is 255 if the class does not override this method to return a value
     * specific to its use of alpha.
     */
    public int getAlpha() {
        return 0xFF;
    }
    
    /**
     * Returns the current color filter, or {@code null} if none set.
     *
     * @return the current color filter, or {@code null} if none set
     */
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }
    
    /** For internal use only. Individual results may vary. */
    public void getHotspotBounds(Rect outRect) {
        outRect.set(getBounds());
    }
    
    /**
     * Inflate this Drawable from an XML resource optionally styled by a theme.
     *
     * @param r      Resources used to resolve attribute values
     * @param parser XML parser from which to inflate this Drawable
     * @param attrs  Base set of attribute values
     * @param theme  Theme to apply, may be null
     * @throws XmlPullParserException
     * @throws IOException
     */
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme)
        throws XmlPullParserException, IOException {}
    
    /** Inflate a Drawable from an XML resource. */
    void inflateWithAttributes(TypedArray attrs, int visibleAttr) {
        mVisible = attrs.getBoolean(visibleAttr, mVisible);
    }
    
    /**
     * If this Drawable does transition animations between states, ask that
     * it immediately jump to the current state and skip any active animations.
     */
    public void jumpToCurrentState() {}
    
    /**
     * Obtains styled attributes from the theme, if available, or unstyled
     * resources if the theme is null.
     */
    final static TypedArray obtainAttributes(Resources res, Theme theme, AttributeSet set, int[] attrs) {
    
        if (theme == null)
            return res.obtainAttributes(set, attrs);
        
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    
    }
    
    /**
     * Parses a {@link android.graphics.PorterDuff.Mode} from a tintMode
     * attribute's enum value.
     *
     * @hide
     */
    static PorterDuff.Mode parseTintMode(int value, Mode defaultMode) {
    
        switch (value) {
        
            case 3:
                return Mode.SRC_OVER;
            case 5:
                return Mode.SRC_IN;
            case 9:
                return Mode.SRC_ATOP;
            case 14:
                return Mode.MULTIPLY;
            case 15:
                return Mode.SCREEN;
            case 16:
                return Mode.ADD;
            default:
                return defaultMode;
        
        }
    
    }
    
    @Override
    public void setAlpha(int alpha) {}
    
    @Override
    public void setColorFilter(ColorFilter cf) {
        mColorFilter = cf;
    }
    
    /**
     * Specifies the hotspot's location within the drawable.
     *
     * @param x The X coordinate of the center of the hotspot
     * @param y The Y coordinate of the center of the hotspot
     */
    public void setHotspot(float x, float y) {}
    
    /**
     * Sets the bounds to which the hotspot is constrained, if they should be
     * different from the drawable bounds.
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setHotspotBounds(int left, int top, int right, int bottom) {}
    
    /**
     * Specifies a tint for this drawable.
     *
     * <p>Setting a color filter via {@link #setColorFilter(ColorFilter)} overrides tint.</p>
     *
     * @param tint Color to use for tinting this drawable
     * @see #setTintMode(PorterDuff.Mode)
     */
    public void setTint(int tint) {
        setTintList(ColorStateList.valueOf(tint));
    }
    
    /**
     * Specifies a tint for this drawable as a color state list.
     *
     * <p>Setting a color filter via {@link #setColorFilter(ColorFilter)} overrides tint.</p>
     *
     * @param tint Color state list to use for tinting this drawable, or null to clear the tint
     * @see #setTintMode(PorterDuff.Mode)
     */
    public void setTintList(ColorStateList tint) {}
    
    /**
     * Specifies a tint blending mode for this drawable.
     *
     * <p>Setting a color filter via {@link #setColorFilter(ColorFilter)} overrides tint.</p>
     *
     * @param tintMode Color state list to use for tinting this drawable, or null to
     *                 clear the tint
     * @param tintMode A Porter-Duff blending mode
     */
    public void setTintMode(PorterDuff.Mode tintMode) {}
    
    /**
     * Set whether this Drawable is visible.  This generally does not impact
     * the Drawable's behavior, but is a hint that can be used by some
     * Drawables, for example, to decide whether run animations.
     *
     * @param visible Set to true if visible, false if not.
     * @param restart You can supply true here to force the drawable to behave
     *                as if it has just become visible, even if it had last
     *                been set visible.  Used for example to force animations
     *                to restart.
     *
     * @return boolean Returns true if the new visibility is different than
     *         its previous state.
     */
    public boolean setVisible(boolean visible, boolean restart) {
    
        boolean changed = (mVisible != visible);
        
        if (changed) {
        
            mVisible = visible;
            invalidateSelf();
        
        }
        
        return changed;
    
    }
    
    /** Ensures the tint filter is consistent with the current tint color and mode. */
    PorterDuffColorFilter updateTintFilter(PorterDuffColorFilter tintFilter, ColorStateList tint,
        PorterDuff.Mode tintMode) {
        
        if ((tint == null) || (tintMode == null))
            return null;
        
        final int color = tint.getColorForState(getState(), Color.TRANSPARENT);
        return new PorterDuffColorFilter(color, tintMode);
    }
    
    /**
     * This abstract class is used by {@link Drawable}s to store shared constant state and data
     * between Drawables. {@link BitmapDrawable}s created from the same resource will for instance
     * share a unique bitmap stored in their ConstantState.
     *
     * <p>
     * {@link #newDrawable(Resources)} can be used as a factory to create new Drawable instances
     * from this ConstantState.
     * </p>
     *
     * Use {@link Drawable#getConstantState()} to retrieve the ConstantState of a Drawable. Calling
     * {@link Drawable#mutate()} on a Drawable should typically create a new ConstantState for that
     * Drawable.
     */
    public static abstract class ConstantState extends Drawable.ConstantState {
    
        /** Return whether this constant state can have a theme applied. */
        public boolean canApplyTheme() {
            return false;
        }
        
        /**
         * Create a new Drawable instance from its constant state. This must be
         * implemented for drawables that can have a theme applied.
         */
        public Drawable newDrawable(Resources res, Theme theme) {
            return newDrawable();
        }
    
    }

}