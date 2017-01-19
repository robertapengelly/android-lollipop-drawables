package robertapengelly.support.graphics.drawable;

import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.Resources.Theme;
import  android.content.res.TypedArray;
import  android.graphics.Bitmap;
import  android.graphics.BitmapFactory;
import  android.graphics.Canvas;
import  android.graphics.ColorFilter;
import  android.graphics.NinePatch;
import  android.graphics.Paint;
import  android.graphics.PixelFormat;
import  android.graphics.PorterDuff;
import  android.graphics.PorterDuff.Mode;
import  android.graphics.PorterDuffColorFilter;
import  android.graphics.Rect;
import  android.graphics.Region;
import  android.graphics.drawable.Drawable;
import  android.os.Build;
import  android.util.AttributeSet;
import  android.util.DisplayMetrics;
import  android.util.LayoutDirection;
import  android.util.TypedValue;

import  java.io.IOException;
import  java.io.InputStream;
import  java.lang.reflect.Field;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

import  robertapengelly.support.lollipopdrawables.R;

/**
 * A resizeable bitmap, with stretchable areas that you define. This type of image
 * is defined in a .png file with a special format.
 *
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For more information about how to use a NinePatchDrawable, read the
 * <a href="{@docRoot}guide/topics/graphics/2d-graphics.html#nine-patch">
 * Canvas and Drawables</a> developer guide. For information about creating a NinePatch image
 * file using the draw9patch tool, see the
 * <a href="{@docRoot}guide/developing/tools/draw9patch.html">Draw 9-patch</a> tool guide.</p></div>
 */
public class NinePatchDrawable extends LollipopDrawable {

    // dithering helps a lot, and is pretty cheap, so default is true
    private static final boolean DEFAULT_DITHER = false;
    
    private boolean mMutated;
    
    // These are scaled to match the target density.
    private int mBitmapWidth = -1;
    private int mBitmapHeight = -1;
    
    private int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
    
    private NinePatchState mNinePatchState;
    private NinePatch mNinePatch;
    private PorterDuffColorFilter mTintFilter;
    private Paint mPaint;
    private Rect mPadding;
    
    NinePatchDrawable() {
        mNinePatchState = new NinePatchState();
    }
    
    /**
     * The one constructor to rule them all. This is called by all public
     * constructors to set the state and initialize local properties.
     */
    private NinePatchDrawable(NinePatchState state, Resources res, Theme theme) {
    
        if ((theme != null) && state.canApplyTheme()) {
        
            // If we need to apply a theme, implicitly mutate.
            mNinePatchState = new NinePatchState(state);
            applyTheme(theme);
        
        } else
            mNinePatchState = state;
        
        initializeWithState(state, res);
    
    }
    
    /**
     * Create drawable from raw nine-patch data, setting initial target density
     * based on the display metrics of the resources.
     */
    public NinePatchDrawable(Resources res, Bitmap bitmap, byte[] chunk, Rect padding, String srcName) {
        this(new NinePatchState(new NinePatch(bitmap, chunk, srcName), padding), res, null);
        
        mNinePatchState.mTargetDensity = mTargetDensity;
    
    }
    
    /**
     * Create drawable from existing nine-patch, setting initial target density
     * based on the display metrics of the resources.
     */
    public NinePatchDrawable(Resources res, NinePatch patch) {
        this(new NinePatchState(patch, new Rect()), res, null);
        
        mNinePatchState.mTargetDensity = mTargetDensity;
    
    }
    
    @Override
    public void applyTheme(Theme t) {
        super.applyTheme(t);
        
        final NinePatchState state = mNinePatchState;
        
        if ((state == null) || (state.mThemeAttrs == null))
            return;
        
        try {
            updateStateFromTypedArray(t, null, state.mThemeAttrs);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    
    }
    
    @Override
    public boolean canApplyTheme() {
        return ((mNinePatchState != null) && (mNinePatchState.mThemeAttrs != null));
    }
    
    private void computeBitmapSize() {
    
        final int sdensity = mNinePatch.getDensity();
        final int tdensity = mTargetDensity;
        
        if (sdensity == tdensity) {
        
            mBitmapHeight = mNinePatch.getHeight();
            mBitmapWidth = mNinePatch.getWidth();
        
        } else {
        
            mBitmapHeight = scaleFromDensity(mNinePatch.getHeight(), sdensity, tdensity);
            mBitmapWidth = scaleFromDensity(mNinePatch.getWidth(), sdensity, tdensity);
            
            if (mNinePatchState.mPadding != null && mPadding != null) {
            
                Rect dest = mPadding;
                Rect src = mNinePatchState.mPadding;
                
                if (dest == src)
                    mPadding = dest = new Rect(src);
                
                dest.bottom = scaleFromDensity(src.bottom, sdensity, tdensity);
                dest.left = scaleFromDensity(src.left, sdensity, tdensity);
                dest.right = scaleFromDensity(src.right, sdensity, tdensity);
                dest.top = scaleFromDensity(src.top, sdensity, tdensity);
            
            }
        
        }
    
    }
    
    @Override
    public void draw(Canvas canvas) {
    
        final boolean clearColorFilter;
        final Rect bounds = getBounds();
        
        if ((mTintFilter != null) && (getPaint().getColorFilter() == null)) {
        
            mPaint.setColorFilter(mTintFilter);
            clearColorFilter = true;
        
        } else
            clearColorFilter = false;
        
        final boolean needsMirroring = needsMirroring();
        
        if (needsMirroring) {
        
            // Mirror the 9patch
            canvas.translate(bounds.right - bounds.left, 0);
            canvas.scale(-1.0f, 1.0f);
        
        }
        
        final int restoreAlpha;
        
        if (mNinePatchState.mBaseAlpha != 1.0f) {
        
            restoreAlpha = mPaint.getAlpha();
            mPaint.setAlpha((int) (restoreAlpha * mNinePatchState.mBaseAlpha + 0.5f));
        
        } else
            restoreAlpha = -1;
        
        mNinePatch.draw(canvas, bounds, mPaint);
        
        if (clearColorFilter)
            mPaint.setColorFilter(null);
        
        if (restoreAlpha >= 0)
            mPaint.setAlpha(restoreAlpha);
    
    }
    
    @Override
    public int getAlpha() {
    
        if (mPaint == null)
            // Fast common case -- normal alpha.
            return 0xFF;
        
        return getPaint().getAlpha();
    
    }
    
    @Override
    public int getChangingConfigurations() {
        return (super.getChangingConfigurations() | mNinePatchState.mChangingConfigurations);
    }
    
    @Override
    public ConstantState getConstantState() {
    
        mNinePatchState.mChangingConfigurations = getChangingConfigurations();
        return mNinePatchState;
    
    }
    
    /** Retrieves the height of the source .png file (before resizing). */
    @Override
    public int getIntrinsicHeight() {
        return mBitmapHeight;
    }
    
    /** Retrieves the width of the source .png file (before resizing). */
    @Override
    public int getIntrinsicWidth() {
        return mBitmapWidth;
    }
    
    @Override
    public int getMinimumHeight() {
        return mBitmapHeight;
    }
    
    @Override
    public int getMinimumWidth() {
        return mBitmapWidth;
    }
    
    /**
     * Returns a {@link android.graphics.PixelFormat graphics.PixelFormat}
     * value of OPAQUE or TRANSLUCENT.
     */
    @Override
    public int getOpacity() {
        return ((mNinePatch.hasAlpha() || (mPaint != null && mPaint.getAlpha() < 255)) ?
                PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE);
    }
    
    @Override
    public boolean getPadding(Rect padding) {
    
        final Rect scaledPadding = mPadding;
        
        if (scaledPadding != null) {
        
            if (needsMirroring())
                padding.set(scaledPadding.right, scaledPadding.top, scaledPadding.left, scaledPadding.bottom);
            else
                padding.set(scaledPadding);
            
            return ((padding.left | padding.top | padding.right | padding.bottom) != 0);
        
        }
        
        return false;
    
    }
    
    public Paint getPaint() {
    
        if (mPaint == null) {
        
            mPaint = new Paint();
            mPaint.setDither(DEFAULT_DITHER);
        
        }
        
        return mPaint;
    
    }
    
    @Override
    public Region getTransparentRegion() {
        return mNinePatch.getTransparentRegion(getBounds());
    }
    
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        //super.inflate(r, parser, attrs, theme);
        
        final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.NinePatchDrawable);
        updateStateFromTypedArray(null, a, null);
        a.recycle();
    
    }
    
    /** Initializes local dynamic properties from state. */
    private void initializeWithState(NinePatchState state, Resources res) {
    
        if (res != null)
            mTargetDensity = res.getDisplayMetrics().densityDpi;
        else
            mTargetDensity = state.mTargetDensity;
        
        // If we can, avoid calling any methods that initialize Paint.
        if (state.mDither != DEFAULT_DITHER)
            setDither(state.mDither);
        
        // Make a local copy of the padding.
        if (state.mPadding != null)
            mPadding = new Rect(state.mPadding);
        
        mTintFilter = updateTintFilter(mTintFilter, state.mTint, state.mTintMode);
        
        setNinePatch(state.mNinePatch);
    
    }
    
    @Override
    public boolean isAutoMirrored() {
        return mNinePatchState.mAutoMirrored;
    }
    
    @Override
    public boolean isStateful() {
    
        final NinePatchState s = mNinePatchState;
        return super.isStateful() || (s.mTint != null && s.mTint.isStateful());
    
    }
    
    @Override
    public Drawable mutate() {
    
        if (!mMutated && (super.mutate() == this)) {
        
            mMutated = true;
            mNinePatchState = new NinePatchState(mNinePatchState);
            
            mNinePatch = mNinePatchState.mNinePatch;
        
        }
        
        return this;
    
    }
    
    private boolean needsMirroring() {
    
        if (isAutoMirrored())
            if (Build.VERSION.SDK_INT >= 23)
                //noinspection WrongConstant
                return (getLayoutDirection() == LayoutDirection.RTL);
        
        return false;
    
    }
    
    @Override
    protected boolean onStateChange(int[] stateSet) {
    
        final NinePatchState state = mNinePatchState;
        
        if ((state.mTint != null) && (state.mTintMode != null)) {
        
            mTintFilter = updateTintFilter(mTintFilter, state.mTint, state.mTintMode);
            return true;
        
        }
        
        return false;
    
    }
    
    private int scaleFromDensity(int size, int sdensity, int tdensity) {
    
        if ((sdensity == Bitmap.DENSITY_NONE) || (tdensity == Bitmap.DENSITY_NONE) || (sdensity == tdensity))
            return size;
        
        // Scale by tdensity / sdensity, rounding up.
        return (((size * tdensity) + (sdensity >> 1)) / sdensity);
    
    }
    
    @Override
    public void setAlpha(int alpha) {
    
        if ((mPaint == null) && (alpha == 0xFF))
            // Fast common case -- leave at normal alpha.
            return;
        
        getPaint().setAlpha(alpha);
        invalidateSelf();
    
    }
    
    @Override
    public void setAutoMirrored(boolean mirrored) {
        mNinePatchState.mAutoMirrored = mirrored;
    }
    
    @Override
    public void setColorFilter(ColorFilter cf) {
    
        if ((mPaint == null) && (cf == null))
            // Fast common case -- leave at no color filter.
            return;
        
        getPaint().setColorFilter(cf);
        invalidateSelf();
    
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void setDither(boolean dither) {
    
        //noinspection PointlessBooleanExpression
        if ((mPaint == null) && (dither == DEFAULT_DITHER))
            // Fast common case -- leave at default dither.
            return;
        
        getPaint().setDither(dither);
        invalidateSelf();
    
    }
    
    @Override
    public void setFilterBitmap(boolean filter) {
    
        getPaint().setFilterBitmap(filter);
        invalidateSelf();
    
    }
    
    private void setNinePatch(NinePatch ninePatch) {
    
        if (mNinePatch != ninePatch) {
        
            mNinePatch = ninePatch;
            
            if (ninePatch != null)
                computeBitmapSize();
            else
                mBitmapWidth = (mBitmapHeight = -1);
            
            invalidateSelf();
        
        }
    
    }
    
    /**
     * Set the density at which this drawable will be rendered.
     *
     * @param density The density scale for this drawable.
     *
     * @see android.graphics.Bitmap#setDensity(int)
     * @see android.graphics.Bitmap#getDensity()
     */
    public void setTargetDensity(int density) {
    
        if (density != mTargetDensity) {
        
            mTargetDensity = ((density == 0) ? DisplayMetrics.DENSITY_DEFAULT : density);
            
            if (mNinePatch != null)
                computeBitmapSize();
            
            invalidateSelf();
        
        }
    
    }
    
    /**
     * Set the density scale at which this drawable will be rendered. This
     * method assumes the drawable will be rendered at the same density as the
     * specified canvas.
     *
     * @param canvas The Canvas from which the density scale must be obtained.
     *
     * @see android.graphics.Bitmap#setDensity(int)
     * @see android.graphics.Bitmap#getDensity()
     */
    public void setTargetDensity(Canvas canvas) {
        setTargetDensity(canvas.getDensity());
    }
    
    /**
     * Set the density scale at which this drawable will be rendered.
     *
     * @param metrics The DisplayMetrics indicating the density scale for this drawable.
     *
     * @see android.graphics.Bitmap#setDensity(int)
     * @see android.graphics.Bitmap#getDensity()
     */
    public void setTargetDensity(DisplayMetrics metrics) {
        setTargetDensity(metrics.densityDpi);
    }
    
    @Override
    public void setTintList(ColorStateList tint) {
    
        mNinePatchState.mTint = tint;
        mTintFilter = updateTintFilter(mTintFilter, tint, mNinePatchState.mTintMode);
        
        invalidateSelf();
    
    }
    
    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
    
        mNinePatchState.mTintMode = tintMode;
        mTintFilter = updateTintFilter(mTintFilter, mNinePatchState.mTint, tintMode);
        
        invalidateSelf();
    
    }
    
    /** Updates the constant state from the values in the typed array. */
    private void updateStateFromTypedArray(Theme theme, TypedArray a, TypedValue[] values) throws XmlPullParserException {
    
        final Resources r = a.getResources();
        final NinePatchState state = mNinePatchState;
        
        // Account for any configuration changes.
        state.mChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        state.mThemeAttrs = TypedArrayCompat.extractThemeAttrs(a);
        
        state.mDither = a.getBoolean(R.styleable.NinePatchDrawable_android_dither, state.mDither);
        
        final int srcResId = a.getResourceId(R.styleable.NinePatchDrawable_android_src, 0);
        
        if (srcResId != 0) {
        
            final BitmapFactory.Options options = new BitmapFactory.Options();
            
            //noinspection deprecation
            options.inDither = !state.mDither;
            
            try {
            
                final Field field = DisplayMetrics.class.getDeclaredField("noncompatDensityDpi");
                field.setAccessible(true);
                field.get(r.getDisplayMetrics());
            
            } catch (Exception ex) {
            
                try {
                
                    final Field field = DisplayMetrics.class.getDeclaredField("DENSITY_DEVICE");
                    field.setAccessible(true);
                    field.get(r.getDisplayMetrics());
                
                } catch (Exception ignored) {}
            
            }
            
            final Rect padding = new Rect();
            
            Bitmap bitmap = null;
            
            try {
            
                final TypedValue value = new TypedValue();
                final InputStream is = r.openRawResource(srcResId, value);
                
                bitmap = BitmapFactory.decodeResourceStream(r, value, is, padding, options);
                is.close();
            
            } catch (IOException ignored) {}
            
            if (bitmap == null)
                throw new XmlPullParserException(a.getPositionDescription() +
                    ": <nine-patch> requires a valid src attribute");
            else if (bitmap.getNinePatchChunk() == null)
                throw new XmlPullParserException(a.getPositionDescription() +
                    ": <nine-patch> requires a valid 9-patch source image");
            
            state.mNinePatch = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);
            state.mPadding = padding;
        
        }
        
        state.mAutoMirrored = a.getBoolean(R.styleable.NinePatchDrawable_android_autoMirrored, state.mAutoMirrored);
        state.mBaseAlpha = a.getFloat(R.styleable.NinePatchDrawable_android_alpha, state.mBaseAlpha);
        
        final int tintMode = a.getInt(R.styleable.NinePatchDrawable_android_tintMode, -1);
        
        if (tintMode != -1)
            state.mTintMode = parseTintMode(tintMode, Mode.SRC_IN);
        
        final ColorStateList tint = TypedArrayCompat.getColorStateList(theme, a, values,
            R.styleable.NinePatchDrawable_android_tint);
        
        if (tint != null)
            state.mTint = tint;
        
        // Update local properties.
        initializeWithState(state, r);
        
        // Push density applied by setNinePatchState into state.
        state.mTargetDensity = mTargetDensity;
    
    }
    
    final static class NinePatchState extends ConstantState {
    
        boolean mAutoMirrored = false;
        boolean mDither = DEFAULT_DITHER;
        
        float mBaseAlpha = 1.0f;
        
        int mChangingConfigurations;
        int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
        
        ColorStateList mTint = null;
        Mode mTintMode = DEFAULT_TINT_MODE;
        NinePatch mNinePatch = null;
        Rect mPadding = null;
        TypedValue[] mThemeAttrs = null;
        
        NinePatchState() {}
        
        NinePatchState(NinePatch ninePatch, Rect padding) {
            this(ninePatch, padding, DEFAULT_DITHER, false);
        }
        
        NinePatchState(NinePatch ninePatch, Rect padding, boolean dither, boolean autoMirror) {
        
            mAutoMirrored = autoMirror;
            mDither = dither;
            mNinePatch = ninePatch;
            mPadding = padding;
        
        }
        
        // Copy constructor
        NinePatchState(NinePatchState state) {
        
            // We don't deep-copy any fields because they are all immutable.
            mAutoMirrored = state.mAutoMirrored;
            mBaseAlpha = state.mBaseAlpha;
            mChangingConfigurations = state.mChangingConfigurations;
            mDither = state.mDither;
            mNinePatch = state.mNinePatch;
            mPadding = state.mPadding;
            mTargetDensity = state.mTargetDensity;
            mThemeAttrs = state.mThemeAttrs;
            mTint = state.mTint;
            mTintMode = state.mTintMode;
        
        }
        
        @Override
        public boolean canApplyTheme() {
            return (mThemeAttrs != null);
        }
        
        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
        
        @Override
        public Drawable newDrawable() {
            return new NinePatchDrawable(this, null, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res) {
            return new NinePatchDrawable(this, res, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res, Theme theme) {
            return new NinePatchDrawable(this, res, theme);
        }
    
    }

}