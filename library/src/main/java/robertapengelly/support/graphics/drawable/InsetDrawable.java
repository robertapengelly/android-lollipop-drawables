package robertapengelly.support.graphics.drawable;

import  android.annotation.TargetApi;
import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.TypedArray;
import  android.content.res.Resources.Theme;
import  android.graphics.Canvas;
import  android.graphics.ColorFilter;
import  android.graphics.Outline;
import  android.graphics.PorterDuff.Mode;
import  android.graphics.Rect;
import  android.graphics.drawable.Drawable;
import  android.os.Build;
import  android.util.AttributeSet;
import  android.util.TypedValue;

import  java.io.IOException;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

import  robertapengelly.support.graphics.Insets;
import  robertapengelly.support.lollipopdrawables.R;

/**
 * A Drawable that insets another Drawable by a specified distance.
 * This is used when a View needs a background that is smaller than
 * the View's actual bounds.
 *
 * <p>It can be defined in an XML file with the <code>&lt;inset></code> element. For more
 * information, see the guide to <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html">Drawable Resources</a>.</p>
 */
public class InsetDrawable extends LollipopDrawable implements Drawable.Callback {

    private final Rect mTmpRect = new Rect();
    
    private boolean mMutated;
    private InsetState mInsetState;
    private Rect mHotspotBounds;
    
    /*package*/ InsetDrawable() {
        this(null, null);
    }
    
    public InsetDrawable(Drawable drawable, int inset) {
        this(drawable, inset, inset, inset, inset);
    }
    
    private InsetDrawable(InsetState state, Resources res) {
        mInsetState = new InsetState(state, this, res);
    }
    
    public InsetDrawable(Drawable drawable, int insetLeft, int insetTop, int insetRight, int insetBottom) {
        this(null, null);
        
        mInsetState.mDrawable = drawable;
        mInsetState.mInsetBottom = insetBottom;
        mInsetState.mInsetLeft = insetLeft;
        mInsetState.mInsetRight = insetRight;
        mInsetState.mInsetTop = insetTop;
        
        if (drawable != null)
            drawable.setCallback(this);
    
    }
    
    @Override
    public void applyTheme(Theme t) {
        super.applyTheme(t);

        final InsetState state = mInsetState;
        
        if ((state == null) || (state.mThemeAttrs == null))
            return;
        
        try {
            updateStateFromTypedArray(t, null, mInsetState.mThemeAttrs);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    
    }
    
    @Override
    public boolean canApplyTheme() {
        return ((mInsetState != null) && (mInsetState.mThemeAttrs != null));
    }
    
    @Override
    public void draw(Canvas canvas) {    
        mInsetState.mDrawable.draw(canvas);
    }
    
    @Override
    public int getAlpha() {
    
        if (Build.VERSION.SDK_INT >= 19)
            return mInsetState.mDrawable.getAlpha();
        else if (mInsetState.mDrawable instanceof LollipopDrawable)
            //noinspection RedundantCast
            ((LollipopDrawable) mInsetState.mDrawable).getAlpha();
        
        return super.getAlpha();
    }
    
    @Override
    public int getChangingConfigurations() {
        return (super.getChangingConfigurations()
            | mInsetState.mChangingConfigurations
                | mInsetState.mDrawable.getChangingConfigurations());
    }
    
    @Override
    public ConstantState getConstantState() {
    
        if (mInsetState.canConstantState()) {
        
            mInsetState.mChangingConfigurations = getChangingConfigurations();
            return mInsetState;
        
        }
        
        return null;
    
    }
    
    /** Returns the drawable wrapped by this InsetDrawable. May be null. */
    public Drawable getDrawable() {
        return mInsetState.mDrawable;
    }
    
    @Override
    public void getHotspotBounds(Rect outRect) {
    
        if (mHotspotBounds != null)
            outRect.set(mHotspotBounds);
        else
            super.getHotspotBounds(outRect);
    
    }
    
    @Override
    public int getIntrinsicHeight() {
        return mInsetState.mDrawable.getIntrinsicHeight();
    }
    
    @Override
    public int getIntrinsicWidth() {
        return mInsetState.mDrawable.getIntrinsicWidth();
    }
    
    @Override
    public int getOpacity() {
        return mInsetState.mDrawable.getOpacity();
    }
    
    @Override
    public Insets getOpticalInsets() {
    
        final Insets contentInsets = super.getOpticalInsets();
        return Insets.of((contentInsets.left + mInsetState.mInsetLeft), (contentInsets.top + mInsetState.mInsetTop),
            (contentInsets.right + mInsetState.mInsetRight), (contentInsets.bottom + mInsetState.mInsetBottom));
    
    }
    
    @Override
    @TargetApi(21)
    public void getOutline(Outline outline) {
        mInsetState.mDrawable.getOutline(outline);
    }
    
    @Override
    public boolean getPadding(Rect padding) {
    
        boolean pad = mInsetState.mDrawable.getPadding(padding);
        
        padding.bottom += mInsetState.mInsetBottom;
        padding.left += mInsetState.mInsetLeft;
        padding.right += mInsetState.mInsetRight;
        padding.top += mInsetState.mInsetTop;
        
        return (pad || ((mInsetState.mInsetLeft | mInsetState.mInsetRight |
            mInsetState.mInsetTop | mInsetState.mInsetBottom)) != 0);
    }
    
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        
        final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.InsetDrawable);
        super.inflateWithAttributes(a, R.styleable.InsetDrawable_android_visible);
        
        mInsetState.mDrawable = null;
        updateStateFromTypedArray(null, a, null);
        
        // Load inner XML elements.
        if (mInsetState.mDrawable == null) {
        
            int type;
            
            //noinspection StatementWithEmptyBody
            while ((type=parser.next()) == XmlPullParser.TEXT);
            
            if (type != XmlPullParser.START_TAG)
                throw new XmlPullParserException(parser.getPositionDescription()
                    + ": <inset> tag requires a 'drawable' attribute or child tag defining a drawable");
            
            final Drawable dr = LollipopDrawablesCompat.createFromXmlInner(r, parser, attrs, theme);
            mInsetState.mDrawable = dr;
            dr.setCallback(this);
        
        }
        
        verifyRequiredAttributes(a);
        a.recycle();
    
    }
    
    @Override
    public boolean isStateful() {
        return mInsetState.mDrawable.isStateful();
    }
    
    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }
    
    @Override
    public Drawable mutate() {
    
        if (!mMutated && (super.mutate() == this)) {
        
            mInsetState.mDrawable.mutate();
            mMutated = true;
        
        }
        
        return this;
    
    }
    
    @Override
    protected void onBoundsChange(Rect bounds) {
    
        final Rect r = mTmpRect;
        r.set(bounds);
        
        r.bottom -= mInsetState.mInsetBottom;
        r.left += mInsetState.mInsetLeft;
        r.right -= mInsetState.mInsetRight;
        r.top += mInsetState.mInsetTop;
        
        mInsetState.mDrawable.setBounds(r.left, r.top, r.right, r.bottom);
    
    }
    
    @Override
    protected boolean onLevelChange(int level) {
        return mInsetState.mDrawable.setLevel(level);
    }
    
    @Override
    protected boolean onStateChange(int[] state) {
    
        boolean changed = mInsetState.mDrawable.setState(state);
        onBoundsChange(getBounds());
        
        return changed;
    
    }
    
    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }
    
    @Override
    public void setAlpha(int alpha) {
        mInsetState.mDrawable.setAlpha(alpha);
    }
    
    @Override
    public void setColorFilter(ColorFilter cf) {
        mInsetState.mDrawable.setColorFilter(cf);
    }
    
    @Override
    public void setHotspot(float x, float y) {
    
        if (Build.VERSION.SDK_INT >= 21)
            mInsetState.mDrawable.setHotspot(x, y);
        else if (mInsetState.mDrawable instanceof LollipopDrawable)
            //noinspection RedundantCast
            ((LollipopDrawable) mInsetState.mDrawable).setHotspot(x, y);
    
    }
    
    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        
        if (Build.VERSION.SDK_INT >= 21)
            mInsetState.mDrawable.setHotspotBounds(left, top, right, bottom);
        else if (mInsetState.mDrawable instanceof LollipopDrawable)
            //noinspection RedundantCast
            ((LollipopDrawable) mInsetState.mDrawable).setHotspotBounds(left, top, right, bottom);
        
        if (mHotspotBounds == null)
            mHotspotBounds = new Rect(left, top, right, bottom);
        else
            mHotspotBounds.set(left, top, right, bottom);
    
    }
    
    @Override
    public void setTintList(ColorStateList tint) {
    
        if (Build.VERSION.SDK_INT >= 21)
            mInsetState.mDrawable.setTintList(tint);
        else if (mInsetState.mDrawable instanceof LollipopDrawable)
            //noinspection RedundantCast
            ((LollipopDrawable) mInsetState.mDrawable).setTintList(tint);
    
    }
    
    @Override
    public void setTintMode(Mode tintMode) {
    
        if (Build.VERSION.SDK_INT >= 21)
            mInsetState.mDrawable.setTintMode(tintMode);
        else if (mInsetState.mDrawable instanceof LollipopDrawable)
            //noinspection RedundantCast
            ((LollipopDrawable) mInsetState.mDrawable).setTintMode(tintMode);
    
    }
    
    @Override
    public boolean setVisible(boolean visible, boolean restart) {
    
        mInsetState.mDrawable.setVisible(visible, restart);
        return super.setVisible(visible, restart);
    
    }
    
    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }
    
    /** Initializes the constant state from the values in the typed array. */
    private void updateStateFromTypedArray(Theme theme, TypedArray a, TypedValue[] values) throws XmlPullParserException {
    
        final InsetState state = mInsetState;
        
        // Account for any configuration changes.
        state.mChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        state.mThemeAttrs = TypedArrayCompat.extractThemeAttrs(a);
        
        final int N = a.getIndexCount();
        
        for (int i = 0; i < N; ++i) {
        
            final int attr = a.getIndex(i);
            
            if (attr == R.styleable.InsetDrawable_android_drawable) {
            
                final Drawable dr = TypedArrayCompat.getDrawable(theme, a, values, attr);
                
                if (dr != null) {
                
                    state.mDrawable = dr;
                    dr.setCallback(this);
                
                }
            
            } else if (attr == R.styleable.InsetDrawable_android_inset) {
            
                final int inset = TypedArrayCompat.getDimensionPixelOffset(theme, a, values, attr, Integer.MIN_VALUE);
                
                if (inset != Integer.MIN_VALUE) {
                
                    state.mInsetBottom = inset;
                    state.mInsetLeft = inset;
                    state.mInsetRight = inset;
                    state.mInsetTop = inset;
                
                }
            
            } else if (attr == R.styleable.InsetDrawable_android_insetBottom)
                state.mInsetBottom = TypedArrayCompat.getDimensionPixelOffset(theme, a, values, attr, state.mInsetBottom);
            else if (attr == R.styleable.InsetDrawable_android_insetLeft)
                state.mInsetLeft = TypedArrayCompat.getDimensionPixelOffset(theme, a, values, attr, state.mInsetLeft);
            else if (attr == R.styleable.InsetDrawable_android_insetRight)
                state.mInsetRight = TypedArrayCompat.getDimensionPixelOffset(theme, a, values, attr, state.mInsetRight);
            else if (attr == R.styleable.InsetDrawable_android_insetTop)
                state.mInsetTop = TypedArrayCompat.getDimensionPixelOffset(theme, a, values, attr, state.mInsetTop);
        
        }
    
    }
    
    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        // If we're not waiting on a theme, verify required attributes.
        if ((mInsetState.mDrawable == null) && ((mInsetState.mThemeAttrs == null)
            || (mInsetState.mThemeAttrs[R.styleable.InsetDrawable_android_drawable].data == 0)))
                throw new XmlPullParserException(a.getPositionDescription() +
                    ": <inset> tag requires a 'drawable' attribute or child tag defining a drawable");
    }
    
    final static class InsetState extends ConstantState {
    
        boolean mCanConstantState;
        boolean mCheckedConstantState;
        
        int mChangingConfigurations;
        
        int mInsetBottom;
        int mInsetLeft;
        int mInsetRight;
        int mInsetTop;
        
        TypedValue[] mThemeAttrs;
        
        Drawable mDrawable;
        
        InsetState(InsetState orig, InsetDrawable owner, Resources res) {
        
            if (orig != null) {
            
                mChangingConfigurations = orig.mChangingConfigurations;
                mThemeAttrs = orig.mThemeAttrs;
                
                if (res != null)
                    mDrawable = orig.mDrawable.getConstantState().newDrawable(res);
                else
                    mDrawable = orig.mDrawable.getConstantState().newDrawable();
                
                mCheckedConstantState = mCanConstantState = true;
                
                mDrawable.setBounds(orig.mDrawable.getBounds());
                mDrawable.setCallback(owner);
                mDrawable.setLevel(orig.mDrawable.getLevel());
                
                if (Build.VERSION.SDK_INT >= 23)
                    mDrawable.setLayoutDirection(orig.mDrawable.getLayoutDirection());
                
                mInsetBottom = orig.mInsetBottom;
                mInsetLeft = orig.mInsetLeft;
                mInsetRight = orig.mInsetRight;
                mInsetTop = orig.mInsetTop;
            
            }
        
        }
        
        boolean canConstantState() {
        
            if (!mCheckedConstantState) {
            
                mCanConstantState = mDrawable.getConstantState() != null;
                mCheckedConstantState = true;
            
            }
            
            return mCanConstantState;
        
        }
        
        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
        
        @Override
        public Drawable newDrawable() {
            return new InsetDrawable(this, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res) {
            return new InsetDrawable(this, res);
        }
    
    }

}