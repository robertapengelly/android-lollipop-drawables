package robertapengelly.support.graphics.drawable;

import  android.annotation.TargetApi;
import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.Resources.Theme;
import  android.content.res.TypedArray;
import  android.graphics.Bitmap;
import  android.graphics.BitmapFactory;
import  android.graphics.BitmapShader;
import  android.graphics.Canvas;
import  android.graphics.ColorFilter;
import  android.graphics.Matrix;
import  android.graphics.Outline;
import  android.graphics.Paint;
import  android.graphics.PixelFormat;
import  android.graphics.PorterDuff;
import  android.graphics.PorterDuff.Mode;
import  android.graphics.PorterDuffColorFilter;
import  android.graphics.Rect;
import  android.graphics.Shader;
import  android.graphics.Xfermode;
import  android.graphics.drawable.Drawable;
import  android.os.Build;
import  android.util.AttributeSet;
import  android.util.DisplayMetrics;
import  android.util.LayoutDirection;
import  android.util.TypedValue;
import  android.view.Gravity;

import  java.io.InputStream;
import  java.io.IOException;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

import  robertapengelly.support.graphics.Insets;
import  robertapengelly.support.lollipopdrawables.R;

/**
 * A Drawable that wraps a bitmap and can be tiled, stretched, or aligned. You can create a
 * BitmapDrawable from a file path, an input stream, through XML inflation, or from
 * a {@link android.graphics.Bitmap} object.
 *
 * <p>It can be defined in an XML file with the <code>&lt;bitmap></code> element.  For more
 * information, see the guide to <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html">Drawable Resources</a>.</p>
 *
 * <p>Also see the {@link android.graphics.Bitmap} class, which handles the management and
 * transformation of raw bitmap graphics, and should be used when drawing to a
 * {@link android.graphics.Canvas}.</p>
 */
public class BitmapDrawable extends LollipopDrawable {

    private static final int DEFAULT_PAINT_FLAGS = (Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    
    // Constants for {@link android.R.styleable#BitmapDrawable_tileMode}.
    private static final int TILE_MODE_UNDEFINED = -2;
    private static final int TILE_MODE_DISABLED = -1;
    private static final int TILE_MODE_CLAMP = 0;
    private static final int TILE_MODE_REPEAT = 1;
    private static final int TILE_MODE_MIRROR = 2;
    
    private final Rect mDstRect = new Rect();   // #updateDstRectAndInsetsIfDirty() sets this
    
    private boolean mDstRectAndInsetsDirty = true;
    private boolean mMutated;
    
    // These are scaled to match the target density.
    private int mBitmapHeight;
    private int mBitmapWidth;
    
    private int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
    
    private BitmapState mBitmapState;
    
    // Mirroring matrix for using with Shaders
    private Matrix mMirrorMatrix;
    
    /** Optical insets due to gravity. */
    private Insets mOpticalInsets = Insets.NONE;
    
    private PorterDuffColorFilter mTintFilter;
    
    /*package*/ BitmapDrawable() {
        mBitmapState = new BitmapState((Bitmap) null);
    }
    
    /**
     * Create drawable from a bitmap, setting initial target density based on
     * the display metrics of the resources.
     */
    public BitmapDrawable(Resources res, Bitmap bitmap) {
        this(new BitmapState(bitmap), res, null);
        
        mBitmapState.mTargetDensity = mTargetDensity;
    
    }
    
    /** Create a drawable by decoding a bitmap from the given input stream. */
    public BitmapDrawable(Resources res, InputStream is) {
        this(new BitmapState(BitmapFactory.decodeStream(is)), null, null);
        
        mBitmapState.mTargetDensity = mTargetDensity;
        
        if (mBitmapState.mBitmap == null)
            android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + is);
    
    }
    
    /** Create a drawable by opening a given file path and decoding the bitmap. */
    public BitmapDrawable(Resources res, String filepath) {
        this(new BitmapState(BitmapFactory.decodeFile(filepath)), null, null);
        
        mBitmapState.mTargetDensity = mTargetDensity;
        
        if (mBitmapState.mBitmap == null)
            android.util.Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + filepath);
    
    }
    
    /**
     * The one constructor to rule them all. This is called by all public
     * constructors to set the state and initialize local properties.
     */
    private BitmapDrawable(BitmapState state, Resources res, Theme theme) {
    
        if ((theme != null) && state.canApplyTheme()) {
        
            // If we need to apply a theme, implicitly mutate.
            mBitmapState = new BitmapState(state);
            applyTheme(theme);
        
        } else
            mBitmapState = state;
        
        initializeWithState(state, res);
    
    }
    
    @Override
    public void applyTheme(Theme t) {
        super.applyTheme(t);

        final BitmapState state = mBitmapState;
        
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
        return (mBitmapState != null && (mBitmapState.mThemeAttrs != null));
    }
    
    private void computeBitmapSize() {
    
        final Bitmap bitmap = mBitmapState.mBitmap;
        
        if (bitmap != null) {
        
            mBitmapHeight = bitmap.getScaledHeight(mTargetDensity);
            mBitmapWidth = bitmap.getScaledWidth(mTargetDensity);
        
        } else
            mBitmapWidth = (mBitmapHeight = -1);
    
    }
    
    @Override
    public void draw(Canvas canvas) {
    
        final Bitmap bitmap = mBitmapState.mBitmap;
        
        if (bitmap == null)
            return;
        
        final BitmapState state = mBitmapState;
        final Paint paint = state.mPaint;
        
        if (state.mRebuildShader) {
        
            final Shader.TileMode tmx = state.mTileModeX;
            final Shader.TileMode tmy = state.mTileModeY;
            
            if ((tmx == null) && (tmy == null))
                paint.setShader(null);
            else
                paint.setShader(new BitmapShader(bitmap,
                    ((tmx == null) ? Shader.TileMode.CLAMP : tmx),
                        ((tmy == null) ? Shader.TileMode.CLAMP : tmy)));
            
            state.mRebuildShader = false;
        
        }
        
        int restoreAlpha = -1;
        
        if (state.mBaseAlpha != 1.0f) {
        
            final Paint p = getPaint();
            restoreAlpha = p.getAlpha();
            
            p.setAlpha((int) (restoreAlpha * state.mBaseAlpha + 0.5f));
        
        }
        
        final boolean clearColorFilter;
        
        if ((mTintFilter != null) && (paint.getColorFilter() == null)) {
        
            paint.setColorFilter(mTintFilter);
            clearColorFilter = true;
        
        } else
            clearColorFilter = false;
        
        updateDstRectAndInsetsIfDirty();
        
        final boolean needMirroring = needMirroring();
        final Shader shader = paint.getShader();
        
        if (shader == null) {
        
            if (needMirroring) {
            
                canvas.save();
                
                // Mirror the bitmap
                canvas.translate(mDstRect.right - mDstRect.left, 0);
                canvas.scale(-1.0f, 1.0f);
            
            }
            
            canvas.drawBitmap(bitmap, null, mDstRect, paint);
            
            if (needMirroring)
                canvas.restore();
        
        } else {
        
            if (needMirroring) {
            
                // Mirror the bitmap
                updateMirrorMatrix(mDstRect.right - mDstRect.left);
                
                shader.setLocalMatrix(mMirrorMatrix);
                paint.setShader(shader);
            
            } else {
            
                if (mMirrorMatrix != null) {
                
                    mMirrorMatrix = null;
                    
                    shader.setLocalMatrix(new Matrix());
                    paint.setShader(shader);
                
                }
            
            }
            
            canvas.drawRect(mDstRect, paint);
        
        }
        
        if (clearColorFilter)
            paint.setColorFilter(null);
        
        if (restoreAlpha >= 0)
            paint.setAlpha(restoreAlpha);
    
    }
    
    @Override
    public int getAlpha() {
        return mBitmapState.mPaint.getAlpha();
    }
    
    /** Returns the bitmap used by this drawable to render. May be null. */
    public final Bitmap getBitmap() {
        return mBitmapState.mBitmap;
    }
    
    @Override
    public int getChangingConfigurations() {
        return (super.getChangingConfigurations() | mBitmapState.mChangingConfigurations);
    }
    
    @Override
    public ColorFilter getColorFilter() {
        return mBitmapState.mPaint.getColorFilter();
    }
    
    @Override
    public final ConstantState getConstantState() {
    
        mBitmapState.mChangingConfigurations = getChangingConfigurations();
        return mBitmapState;
    
    }
    
    /**
     * Get the gravity used to position/stretch the bitmap within its bounds.
     * See android.view.Gravity
     *
     * @return the gravity applied to the bitmap
     */
    public int getGravity() {
        return mBitmapState.mGravity;
    }
    
    @Override
    public int getIntrinsicHeight() {
        return mBitmapHeight;
    }
    
    @Override
    public int getIntrinsicWidth() {
        return mBitmapWidth;
    }
    
    @Override
    public int getLayoutDirection() {
    
        if (Build.VERSION.SDK_INT >= 23)
            return super.getLayoutDirection();
        
        //noinspection WrongConstant
        return LayoutDirection.LTR;
    
    }
    
    @Override
    public int getOpacity() {
    
        if (mBitmapState.mGravity != Gravity.FILL)
            return PixelFormat.TRANSLUCENT;
        
        final Bitmap bitmap = mBitmapState.mBitmap;
        
        return (((bitmap == null) || bitmap.hasAlpha() || (mBitmapState.mPaint.getAlpha() < 255)) ?
            PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE);
    
    }
    
    public Insets getOpticalInsets() {
    
        updateDstRectAndInsetsIfDirty();
        return mOpticalInsets;
    
    }
    
    @Override
    @TargetApi(21)
    public void getOutline(Outline outline) {
    
        updateDstRectAndInsetsIfDirty();
        
        outline.setRect(mDstRect);
        
        // Only opaque Bitmaps can report a non-0 alpha,
        // since only they are guaranteed to fill their bounds
        boolean opaqueOverShape = ((mBitmapState.mBitmap != null) && !mBitmapState.mBitmap.hasAlpha());
        outline.setAlpha(opaqueOverShape ? (getAlpha() / 255.0f) : 0.0f);
    
    }
    
    /** Returns the paint used to render this drawable. */
    public final Paint getPaint() {
        return mBitmapState.mPaint;
    }
    
    /**
     * Indicates the repeat behavior of this drawable on the X axis.
     *
     * @return {@link android.graphics.Shader.TileMode#CLAMP} if the bitmap does not repeat,
     *         {@link android.graphics.Shader.TileMode#REPEAT} or
     *         {@link android.graphics.Shader.TileMode#MIRROR} otherwise.
     */
    public Shader.TileMode getTileModeX() {
        return mBitmapState.mTileModeX;
    }
    
    /**
     * Indicates the repeat behavior of this drawable on the Y axis.
     *
     * @return {@link android.graphics.Shader.TileMode#CLAMP} if the bitmap does not repeat,
     *         {@link android.graphics.Shader.TileMode#REPEAT} or
     *         {@link android.graphics.Shader.TileMode#MIRROR} otherwise.
     */
    public Shader.TileMode getTileModeY() {
        return mBitmapState.mTileModeY;
    }
    
    public ColorStateList getTint() {
        return mBitmapState.mTint;
    }
    
    public Mode getTintMode() {
        return mBitmapState.mTintMode;
    }
    
    /**
     * Indicates whether anti-aliasing is enabled for this drawable.
     *
     * @return True if anti-aliasing is enabled, false otherwise.
     *
     * @see #setAntiAlias(boolean)
     */
    public boolean hasAntiAlias() {
        return mBitmapState.mPaint.isAntiAlias();
    }
    
    /**
     * Indicates whether the mipmap hint is enabled on this drawable's bitmap.
     *
     * @return True if the mipmap hint is set, false otherwise. If the bitmap
     *         is null, this method always returns false.
     *
     * @see #setMipMap(boolean)
     * @attr ref android.R.styleable#BitmapDrawable_mipMap
     */
    public boolean hasMipMap() {
    
        if (mBitmapState.mBitmap != null)
            if (Build.VERSION.SDK_INT >= 17)
                return mBitmapState.mBitmap.hasMipMap();
        
        return false;
    
    }
    
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        
        final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.BitmapDrawable);
        
        updateStateFromTypedArray(null, a, null);
        verifyState(a);
        
        a.recycle();
    
    }
    
    /**
     * Initializes local dynamic properties from state. This should be called
     * after significant state changes, e.g. from the One True Constructor and
     * after inflating or applying a theme.
     */
    private void initializeWithState(BitmapState state, Resources res) {
    
        if (res != null)
            mTargetDensity = res.getDisplayMetrics().densityDpi;
        else
            mTargetDensity = state.mTargetDensity;
        
        mTintFilter = updateTintFilter(mTintFilter, state.mTint, state.mTintMode);
        
        computeBitmapSize();
    
    }
    
    @Override
    public final boolean isAutoMirrored() {
        return mBitmapState.mAutoMirrored;
    }
    
    @Override
    public boolean isStateful() {
    
        final BitmapState s = mBitmapState;
        return (super.isStateful() || (s.mTint != null && s.mTint.isStateful()));
    
    }
    
    /**
     * A mutable BitmapDrawable still shares its Bitmap with any other Drawable
     * that comes from the same resource.
     *
     * @return This drawable.
     */
    @Override
    public Drawable mutate() {
    
        if (!mMutated && super.mutate() == this) {
        
            mBitmapState = new BitmapState(mBitmapState);
            mMutated = true;
        
        }
        
        return this;
    
    }
    
    private boolean needMirroring() {
    
        if (isAutoMirrored())
            if (Build.VERSION.SDK_INT >= 23)
                //noinspection WrongConstant
                return (getLayoutDirection() == LayoutDirection.RTL);
        
        return false;
    
    }
    
    @Override
    protected void onBoundsChange(Rect bounds) {
    
        mDstRectAndInsetsDirty = true;
        
        final Shader shader = mBitmapState.mPaint.getShader();
        
        if (shader != null) {
        
            if (needMirroring()) {
            
                updateMirrorMatrix(bounds.right - bounds.left);
                
                shader.setLocalMatrix(mMirrorMatrix);
                mBitmapState.mPaint.setShader(shader);
            
            } else {
            
                if (mMirrorMatrix != null) {
                
                    mMirrorMatrix = null;
                    
                    shader.setLocalMatrix(new Matrix());
                    mBitmapState.mPaint.setShader(shader);
                
                }
            
            }
        
        }
    
    }
    
    @Override
    protected boolean onStateChange(int[] stateSet) {
    
        final BitmapState state = mBitmapState;
        
        if (state.mTint != null && state.mTintMode != null) {
        
            mTintFilter = updateTintFilter(mTintFilter, state.mTint, state.mTintMode);
            return true;
        
        }
        
        return false;
    
    }
    
    private static Shader.TileMode parseTileMode(int tileMode) {
    
        switch (tileMode) {
        
            case TILE_MODE_CLAMP:
                return Shader.TileMode.CLAMP;
            case TILE_MODE_REPEAT:
                return Shader.TileMode.REPEAT;
            case TILE_MODE_MIRROR:
                return Shader.TileMode.MIRROR;
            default:
                return null;
        
        }
    
    }
    
    @Override
    public void setAlpha(int alpha) {
    
        final int oldAlpha = mBitmapState.mPaint.getAlpha();
        
        if (alpha != oldAlpha) {
        
            mBitmapState.mPaint.setAlpha(alpha);
            invalidateSelf();
        
        }
    
    }
    
    /**
     * Enables or disables anti-aliasing for this drawable. Anti-aliasing affects
     * the edges of the bitmap only so it applies only when the drawable is rotated.
     *
     * @param aa True if the bitmap should be anti-aliased, false otherwise.
     *
     * @see #hasAntiAlias()
     */
    public void setAntiAlias(boolean aa) {
    
        mBitmapState.mPaint.setAntiAlias(aa);
        invalidateSelf();
    
    }
    
    @Override
    public void setAutoMirrored(boolean mirrored) {
    
        if (mBitmapState.mAutoMirrored != mirrored) {
        
            mBitmapState.mAutoMirrored = mirrored;
            invalidateSelf();
        
        }
    
    }
    
    private void setBitmap(Bitmap bitmap) {
    
        if (mBitmapState.mBitmap != bitmap) {
        
            mBitmapState.mBitmap = bitmap;
            
            computeBitmapSize();
            invalidateSelf();
        
        }
    
    }
    
    @Override
    public void setColorFilter(ColorFilter cf) {
    
        mBitmapState.mPaint.setColorFilter(cf);
        invalidateSelf();
    
    }
    
    @Override
    public void setDither(boolean dither) {
    
        mBitmapState.mPaint.setDither(dither);
        invalidateSelf();
    
    }
    
    @Override
    public void setFilterBitmap(boolean filter) {
    
        mBitmapState.mPaint.setFilterBitmap(filter);
        invalidateSelf();
    
    }
    
    /**
     * Set the gravity used to position/stretch the bitmap within its bounds.
     * See android.view.Gravity
     *
     * @param gravity the gravity
     */
    public void setGravity(int gravity) {
    
        if (mBitmapState.mGravity != gravity) {
        
            mBitmapState.mGravity = gravity;
            mDstRectAndInsetsDirty = true;
            
            invalidateSelf();
        
        }
    
    }
    
    /**
     * Enables or disables the mipmap hint for this drawable's bitmap.
     * See {@link Bitmap#setHasMipMap(boolean)} for more information.
     *
     * If the bitmap is null calling this method has no effect.
     *
     * @param mipMap True if the bitmap should use mipmaps, false otherwise.
     *
     * @see #hasMipMap()
     */
    public void setMipMap(boolean mipMap) {
    
        if (Build.VERSION.SDK_INT <= 17)
            return;
        
        if (mBitmapState.mBitmap != null) {
        
            mBitmapState.mBitmap.setHasMipMap(mipMap);
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
    
        if (mTargetDensity != density) {
        
            mTargetDensity = ((density == 0) ? DisplayMetrics.DENSITY_DEFAULT : density);
            
            if (mBitmapState.mBitmap != null)
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
    
    /**
     * Sets the repeat behavior of this drawable on the X axis. By default, the drawable
     * does not repeat its bitmap. Using {@link android.graphics.Shader.TileMode#REPEAT} or
     * {@link android.graphics.Shader.TileMode#MIRROR} the bitmap can be repeated (or tiled)
     * if the bitmap is smaller than this drawable.
     *
     * @param mode The repeat mode for this drawable.
     *
     * @see #setTileModeY(android.graphics.Shader.TileMode)
     * @see #setTileModeXY(android.graphics.Shader.TileMode, android.graphics.Shader.TileMode)
     * @attr ref android.R.styleable#BitmapDrawable_tileModeX
     */
    public void setTileModeX(Shader.TileMode mode) {
        setTileModeXY(mode, mBitmapState.mTileModeY);
    }
    
    /**
     * Sets the repeat behavior of this drawable on both axis. By default, the drawable
     * does not repeat its bitmap. Using {@link android.graphics.Shader.TileMode#REPEAT} or
     * {@link android.graphics.Shader.TileMode#MIRROR} the bitmap can be repeated (or tiled)
     * if the bitmap is smaller than this drawable.
     *
     * @param xmode The X repeat mode for this drawable.
     * @param ymode The Y repeat mode for this drawable.
     *
     * @see #setTileModeX(android.graphics.Shader.TileMode)
     * @see #setTileModeY(android.graphics.Shader.TileMode)
     */
    public void setTileModeXY(Shader.TileMode xmode, Shader.TileMode ymode) {
    
        final BitmapState state = mBitmapState;
        
        if ((state.mTileModeX != xmode) || (state.mTileModeY != ymode)) {
        
            state.mRebuildShader = true;
            
            state.mTileModeX = xmode;
            state.mTileModeY = ymode;
            
            mDstRectAndInsetsDirty = true;
            invalidateSelf();
        
        }
    
    }
    
    /**
     * Sets the repeat behavior of this drawable on the Y axis. By default, the drawable
     * does not repeat its bitmap. Using {@link android.graphics.Shader.TileMode#REPEAT} or
     * {@link android.graphics.Shader.TileMode#MIRROR} the bitmap can be repeated (or tiled)
     * if the bitmap is smaller than this drawable.
     *
     * @param mode The repeat mode for this drawable.
     *
     * @see #setTileModeX(android.graphics.Shader.TileMode)
     * @see #setTileModeXY(android.graphics.Shader.TileMode, android.graphics.Shader.TileMode)
     * @attr ref android.R.styleable#BitmapDrawable_tileModeY
     */
    public final void setTileModeY(Shader.TileMode mode) {
        setTileModeXY(mBitmapState.mTileModeX, mode);
    }
    
    @Override
    public void setTintList(ColorStateList tint) {
    
        mBitmapState.mTint = tint;
        mTintFilter = updateTintFilter(mTintFilter, tint, mBitmapState.mTintMode);
        
        invalidateSelf();
    
    }
    
    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
    
        mBitmapState.mTintMode = tintMode;
        mTintFilter = updateTintFilter(mTintFilter, mBitmapState.mTint, tintMode);
        
        invalidateSelf();
    
    }
    
    public void setXfermode(Xfermode xfermode) {
    
        mBitmapState.mPaint.setXfermode(xfermode);
        invalidateSelf();
    
    }
    
    private void updateDstRectAndInsetsIfDirty() {
    
        if (mDstRectAndInsetsDirty) {
        
            if (mBitmapState.mTileModeX == null && mBitmapState.mTileModeY == null) {
            
                final int layoutDirection = getLayoutDirection();
                final Rect bounds = getBounds();
                
                if (Build.VERSION.SDK_INT >= 17)
                    Gravity.apply(mBitmapState.mGravity, mBitmapWidth, mBitmapHeight, bounds, mDstRect, layoutDirection);
                else
                    Gravity.apply(mBitmapState.mGravity, mBitmapWidth, mBitmapHeight, bounds, mDstRect);
                
                final int bottom = (bounds.bottom - mDstRect.bottom);
                final int left = (mDstRect.left - bounds.left);
                final int right = (bounds.right - mDstRect.right);
                final int top = (mDstRect.top - bounds.top);
                
                mOpticalInsets = Insets.of(left, top, right, bottom);
            
            } else {
            
                copyBounds(mDstRect);
                mOpticalInsets = Insets.NONE;
            
            }
        
        }
        
        mDstRectAndInsetsDirty = false;
    
    }
    
    private void updateMirrorMatrix(float dx) {
    
        if (mMirrorMatrix == null)
            mMirrorMatrix = new Matrix();
        
        mMirrorMatrix.setTranslate(dx, 0);
        mMirrorMatrix.preScale(-1.0f, 1.0f);
    
    }
    
    /** Updates the constant state from the values in the typed array. */
    private void updateStateFromTypedArray(Theme theme, TypedArray a, TypedValue[] values) throws XmlPullParserException {
    
        final BitmapState state = mBitmapState;
        final Resources r = a.getResources();
        
        // Account for any configuration changes.
        state.mChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        state.mThemeAttrs = TypedArrayCompat.extractThemeAttrs(a);
        
        final int srcResId = a.getResourceId(R.styleable.BitmapDrawable_android_src, 0);
        
        if (srcResId != 0) {
        
            final Bitmap bitmap = BitmapFactory.decodeResource(r, srcResId);
            
            if (bitmap == null)
                throw new XmlPullParserException(a.getPositionDescription() + ": <bitmap> requires a valid src attribute");
            
            state.mBitmap = bitmap;
        
        }
        
        state.mTargetDensity = r.getDisplayMetrics().densityDpi;
        
        boolean defMipMap = false;
        
        if (Build.VERSION.SDK_INT >= 17)
            if (state.mBitmap != null)
                defMipMap = state.mBitmap.hasMipMap();
        
        setMipMap(a.getBoolean(R.styleable.BitmapDrawable_android_mipMap, defMipMap));
        
        state.mAutoMirrored = a.getBoolean(R.styleable.BitmapDrawable_android_autoMirrored, state.mAutoMirrored);
        state.mBaseAlpha = a.getFloat(R.styleable.BitmapDrawable_android_alpha, state.mBaseAlpha);
        
        final int tintMode = a.getInt(R.styleable.BitmapDrawable_android_tintMode, -1);
        
        if (tintMode != -1)
            state.mTintMode = parseTintMode(tintMode, Mode.SRC_IN);
        
        final ColorStateList tint = TypedArrayCompat.getColorStateList(theme, a, values,
            R.styleable.BitmapDrawable_android_tint);
        
        if (tint != null)
            state.mTint = tint;
        
        final Paint paint = mBitmapState.mPaint;
        paint.setAntiAlias(a.getBoolean(R.styleable.BitmapDrawable_android_antialias, paint.isAntiAlias()));
        paint.setDither(a.getBoolean(R.styleable.BitmapDrawable_android_dither, paint.isDither()));
        paint.setFilterBitmap(a.getBoolean(R.styleable.BitmapDrawable_android_filter, paint.isFilterBitmap()));
        
        setGravity(a.getInt(R.styleable.BitmapDrawable_android_gravity, state.mGravity));
        
        final int tileMode = a.getInt(R.styleable.BitmapDrawable_android_tileMode, TILE_MODE_UNDEFINED);
        
        if (tileMode != TILE_MODE_UNDEFINED) {
        
            final Shader.TileMode mode = parseTileMode(tileMode);
            setTileModeXY(mode, mode);
        
        }
        
        final int tileModeX = a.getInt(R.styleable.BitmapDrawable_android_tileModeX, TILE_MODE_UNDEFINED);
        
        if (tileModeX != TILE_MODE_UNDEFINED)
            setTileModeX(parseTileMode(tileModeX));
        
        final int tileModeY = a.getInt(R.styleable.BitmapDrawable_android_tileModeY, TILE_MODE_UNDEFINED);
        
        if (tileModeY != TILE_MODE_UNDEFINED)
            setTileModeY(parseTileMode(tileModeY));
        
        // Update local properties.
        initializeWithState(state, r);
    
    }
    
    /**
     * Ensures all required attributes are set.
     *
     * @throws XmlPullParserException if any required attributes are missing
     */
    private void verifyState(TypedArray a) throws XmlPullParserException {
    
        final BitmapState state = mBitmapState;
        
        if (state.mBitmap == null)
            throw new XmlPullParserException(a.getPositionDescription() + ": <bitmap> requires a valid src attribute");
    
    
    }
    //TODO: Format code
    final static class BitmapState extends ConstantState {
        final Paint mPaint;
        // Values loaded during inflation.
        TypedValue[] mThemeAttrs = null;
        Bitmap mBitmap = null;
        ColorStateList mTint = null;
        Mode mTintMode = DEFAULT_TINT_MODE;
        int mGravity = Gravity.FILL;
        float mBaseAlpha = 1.0f;
        Shader.TileMode mTileModeX = null;
        Shader.TileMode mTileModeY = null;
        int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
        boolean mAutoMirrored = false;
        int mChangingConfigurations;
        boolean mRebuildShader;
        BitmapState(Bitmap bitmap) {
            mBitmap = bitmap;
            mPaint = new Paint(DEFAULT_PAINT_FLAGS);
        }
        BitmapState(BitmapState bitmapState) {
            mBitmap = bitmapState.mBitmap;
            mTint = bitmapState.mTint;
            mTintMode = bitmapState.mTintMode;
            mThemeAttrs = bitmapState.mThemeAttrs;
            mChangingConfigurations = bitmapState.mChangingConfigurations;
            mGravity = bitmapState.mGravity;
            mTileModeX = bitmapState.mTileModeX;
            mTileModeY = bitmapState.mTileModeY;
            mTargetDensity = bitmapState.mTargetDensity;
            mBaseAlpha = bitmapState.mBaseAlpha;
            mPaint = new Paint(bitmapState.mPaint);
            mRebuildShader = bitmapState.mRebuildShader;
            mAutoMirrored = bitmapState.mAutoMirrored;
        }
        @Override
        public boolean canApplyTheme() {
            return mThemeAttrs != null;
        }
        public Bitmap getBitmap() {
            return mBitmap;
        }
        @Override
        public Drawable newDrawable() {
            return new BitmapDrawable(this, null, null);
        }
        @Override
        public Drawable newDrawable(Resources res) {
            return new BitmapDrawable(this, res, null);
        }
        @Override
        public Drawable newDrawable(Resources res, Theme theme) {
            return new BitmapDrawable(this, res, theme);
        }
        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }

}