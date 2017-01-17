package robertapengelly.support.graphics.drawable;

import  android.annotation.TargetApi;
import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.TypedArray;
import  android.content.res.Resources.Theme;
import  android.graphics.Canvas;
import  android.graphics.Color;
import  android.graphics.ColorFilter;
import  android.graphics.DashPathEffect;
import  android.graphics.LinearGradient;
import  android.graphics.Outline;
import  android.graphics.Paint;
import  android.graphics.Path;
import  android.graphics.PixelFormat;
import  android.graphics.RadialGradient;
import  android.graphics.Rect;
import  android.graphics.RectF;
import  android.graphics.Shader;
import  android.graphics.SweepGradient;
import  android.graphics.drawable.Drawable;
import  android.util.AttributeSet;
import  android.util.Log;
import  android.util.TypedValue;

import  java.io.IOException;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

import  robertapengelly.support.lollipopdrawables.R;

/**
 * A Drawable with a color gradient for buttons, backgrounds, etc.
 *
 * <p>It can be defined in an XML file with the <code>&lt;shape></code> element. For more
 * information, see the guide to <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html">Drawable Resources</a>.</p>
 *
 * @attr ref android.R.styleable#GradientDrawable_visible
 * @attr ref android.R.styleable#GradientDrawable_shape
 * @attr ref android.R.styleable#GradientDrawable_innerRadiusRatio
 * @attr ref android.R.styleable#GradientDrawable_innerRadius
 * @attr ref android.R.styleable#GradientDrawable_thicknessRatio
 * @attr ref android.R.styleable#GradientDrawable_thickness
 * @attr ref android.R.styleable#GradientDrawable_useLevel
 * @attr ref android.R.styleable#GradientDrawableSize_android_width
 * @attr ref android.R.styleable#GradientDrawableSize_android_height
 * @attr ref android.R.styleable#GradientDrawableGradient_android_startColor
 * @attr ref android.R.styleable#GradientDrawableGradient_android_centerColor
 * @attr ref android.R.styleable#GradientDrawableGradient_android_endColor
 * @attr ref android.R.styleable#GradientDrawableGradient_android_useLevel
 * @attr ref android.R.styleable#GradientDrawableGradient_android_angle
 * @attr ref android.R.styleable#GradientDrawableGradient_android_type
 * @attr ref android.R.styleable#GradientDrawableGradient_android_centerX
 * @attr ref android.R.styleable#GradientDrawableGradient_android_centerY
 * @attr ref android.R.styleable#GradientDrawableGradient_android_gradientRadius
 * @attr ref android.R.styleable#GradientDrawableSolid_color
 * @attr ref android.R.styleable#GradientDrawableStroke_android_width
 * @attr ref android.R.styleable#GradientDrawableStroke_android_color
 * @attr ref android.R.styleable#GradientDrawableStroke_android_dashWidth
 * @attr ref android.R.styleable#GradientDrawableStroke_android_dashGap
 * @attr ref android.R.styleable#GradientDrawablePadding_android_left
 * @attr ref android.R.styleable#GradientDrawablePadding_android_top
 * @attr ref android.R.styleable#GradientDrawablePadding_android_right
 * @attr ref android.R.styleable#GradientDrawablePadding_android_bottom
 */
public class GradientDrawable extends LollipopDrawable {

    /** Shape is a rectangle, possibly with rounded corners. */
    public static final int RECTANGLE = 0;
    
    /** Shape is an ellipse. */
    public static final int OVAL = 1;
    
    /** Shape is a line. */
    public static final int LINE = 2;
    
    /** Shape is a ring. */
    public static final int RING = 3;
    
    /** Gradient is linear (default). */
    public static final int LINEAR_GRADIENT = 0;
    
    /** Gradient is circular. */
    public static final int RADIAL_GRADIENT = 1;
    
    /** Gradient is a sweep. */
    public static final int SWEEP_GRADIENT  = 2;
    
    /** Radius is in pixels. */
    private static final int RADIUS_TYPE_PIXELS = 0;
    
    /** Radius is a fraction of the base size. */
    private static final int RADIUS_TYPE_FRACTION = 1;
    
    /** Radius is a fraction of the bounds size. */
    private static final int RADIUS_TYPE_FRACTION_PARENT = 2;
    
    private static final float DEFAULT_INNER_RADIUS_RATIO = 3.0f;
    private static final float DEFAULT_THICKNESS_RATIO = 9.0f;
    
    private final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mPath = new Path();
    private final RectF mRect = new RectF();
    
    private boolean mGradientIsDirty;   // internal state
    private boolean mMutated;
    private boolean mPathIsDirty = true;
    
    /** Current gradient radius, valid when {@link #mGradientIsDirty} is false. */
    private float mGradientRadius;
    
    private int mAlpha = 0xFF;  // modified by the caller
    
    private ColorFilter mColorFilter;   // optional, set by the caller
    private GradientState mGradientState;
    private Paint mLayerPaint;    // internal, used if we use saveLayer()
    private Paint mStrokePaint;   // optional, set by the caller
    private Path mRingPath;
    private Rect mPadding;
    
    public GradientDrawable() {
        this(new GradientState(Orientation.TOP_BOTTOM, null), null);
    }
    
    /**
     * Creates a new themed GradientDrawable based on the specified constant state.
     *
     * <p>The resulting drawable is guaranteed to have a new constant state.</p>
     *
     * @param state Constant state from which the drawable inherits
     * @param theme Theme to apply to the drawable
     */
    private GradientDrawable(GradientState state, Theme theme) {
    
        if ((theme != null) && (state.canApplyTheme())) {
        
            // If we need to apply a theme, implicitly mutate.
            mGradientState = new GradientState(state);
            applyTheme(theme);
        
        } else
            mGradientState = state;
        
        initializeWithState(state);
        
        mGradientIsDirty = true;
        mMutated = false;
    
    }
    
    /**
     * Create a new gradient drawable given an orientation and an array
     * of colors for the gradient.
     */
    public GradientDrawable(Orientation orientation, int[] colors) {
        this(new GradientState(orientation, colors), null);
    }
    
    @Override
    public void applyTheme(Theme t) {
        super.applyTheme(t);

        final GradientState state = mGradientState;
        
        if ((state == null) || (state.mThemeAttrs == null))
            return;
        
        try {
            updateStateFromTypedArray(t, null, mGradientState.mThemeAttrs);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
        
        applyThemeChildElements(t);
        
        state.computeOpacity();
    
    }
    
    private void applyThemeChildElements(Theme t) {
    
        final GradientState st = mGradientState;
        
        try {
        
            if (st.mAttrSize != null)
                updateGradientDrawableSize(t, null, st.mAttrSize);
            
            if (st.mAttrGradient != null)
                updateGradientDrawableGradient(null, t, null, st.mAttrGradient);
            
            if (st.mAttrSolid != null)
                updateGradientDrawableSolid(t, null, st.mAttrSolid);
            
            if (st.mAttrStroke != null)
                updateGradientDrawableStroke(t, null, st.mAttrStroke);
            
            if (st.mAttrCorners != null)
                updateDrawableCorners(t, null, st.mAttrCorners);
            
            if (st.mAttrPadding != null)
                updateGradientDrawablePadding(t, null, st.mAttrPadding);
        
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    
    }
    
    private void buildPathIfDirty() {
    
        final GradientState st = mGradientState;
        
        if (mPathIsDirty) {
        
            ensureValidRect();
            
            mPath.reset();
            mPath.addRoundRect(mRect, st.mRadiusArray, Path.Direction.CW);
            
            mPathIsDirty = false;
        
        }
    
    }
    
    private Path buildRing(GradientState st) {
    
        if ((mRingPath != null) && (!st.mUseLevelForShape || !mPathIsDirty))
            return mRingPath;
        
        mPathIsDirty = false;
        float sweep = (st.mUseLevelForShape ? (360.0f * getLevel() / 10000.0f) : 360f);
        
        RectF bounds = new RectF(mRect);
        
        float x = (bounds.width() / 2.0f);
        float y = (bounds.height() / 2.0f);
        
        float thickness = ((st.mThickness != -1) ? st.mThickness : bounds.width() / st.mThicknessRatio);
        
        // inner radius
        float radius = ((st.mInnerRadius != -1) ? st.mInnerRadius : bounds.width() / st.mInnerRadiusRatio);
        
        RectF innerBounds = new RectF(bounds);
        innerBounds.inset((x - radius), (y - radius));
        
        bounds = new RectF(innerBounds);
        bounds.inset(-thickness, -thickness);
        
        if (mRingPath == null)
            mRingPath = new Path();
        else
            mRingPath.reset();
        
        final Path ringPath = mRingPath;
        
        // arcTo treats the sweep angle mod 360, so check for that, since we
        // think 360 means draw the entire oval
        if ((sweep < 360) && (sweep > -360)) {
        
            ringPath.setFillType(Path.FillType.EVEN_ODD);
            
            // inner top
            ringPath.moveTo(x + radius, y);
            
            // outer top
            ringPath.lineTo(x + radius + thickness, y);
            
            // outer arc
            ringPath.arcTo(bounds, 0.0f, sweep, false);
            
            // inner arc
            ringPath.arcTo(innerBounds, sweep, -sweep, false);
            
            ringPath.close();
        
        } else {
        
            // add the entire ovals
            ringPath.addOval(bounds, Path.Direction.CW);
            ringPath.addOval(innerBounds, Path.Direction.CCW);
        
        }
        
        return ringPath;
    
    }
    
    @Override
    public boolean canApplyTheme() {
    
        final GradientState st = mGradientState;
        return ((st != null) && ((st.mThemeAttrs != null) || (st.mAttrSize != null)
            || (st.mAttrGradient != null) || (st.mAttrSolid != null)
                || (st.mAttrStroke != null) || (st.mAttrCorners != null)
                    || (st.mAttrPadding != null)));
    
    }
    
    @Override
    public void draw(Canvas canvas) {
    
        if (!ensureValidRect())
            // nothing to draw
            return;
        
        // remember the alpha values, in case we temporarily overwrite them
        // when we modulate them with mAlpha
        final int prevFillAlpha = mFillPaint.getAlpha();
        final int prevStrokeAlpha = mStrokePaint != null ? mStrokePaint.getAlpha() : 0;
        
        // compute the modulate alpha values
        final int currFillAlpha = modulateAlpha(prevFillAlpha);
        final int currStrokeAlpha = modulateAlpha(prevStrokeAlpha);
        
        final boolean haveFill = (currFillAlpha > 0);
        final boolean haveStroke = ((currStrokeAlpha > 0) && (mStrokePaint != null) &&
            (mStrokePaint.getStrokeWidth() > 0));
        
        final GradientState st = mGradientState;
        
        /*  we need a layer iff we're drawing both a fill and stroke, and the
            stroke is non-opaque, and our shapetype actually supports
            fill+stroke. Otherwise we can just draw the stroke (if any) on top
            of the fill (if any) without worrying about blending artifacts.
         */
         final boolean useLayer = (haveStroke && haveFill && (st.mShape != LINE) &&
             (currStrokeAlpha < 255) && ((mAlpha < 255) || (mColorFilter != null)));
        
        /*  Drawing with a layer is slower than direct drawing, but it
            allows us to apply paint effects like alpha and colorfilter to
            the result of multiple separate draws. In our case, if the user
            asks for a non-opaque alpha value (via setAlpha), and we're
            stroking, then we need to apply the alpha AFTER we've drawn
            both the fill and the stroke.
        */
        if (useLayer) {
        
            if (mLayerPaint == null)
                mLayerPaint = new Paint();
            
            mLayerPaint.setAlpha(mAlpha);
            mLayerPaint.setColorFilter(mColorFilter);
            mLayerPaint.setDither(st.mDither);
            
            float rad = mStrokePaint.getStrokeWidth();
            canvas.saveLayer((mRect.left - rad), (mRect.top - rad), (mRect.right + rad), (mRect.bottom + rad),
                mLayerPaint, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
            
            // don't perform the filter in our individual paints
            // since the layer will do it for us
            mFillPaint.setColorFilter(null);
            mStrokePaint.setColorFilter(null);
        
        } else {
        
            /*  if we're not using a layer, apply the dither/filter to our
                individual paints
            */
            mFillPaint.setAlpha(currFillAlpha);
            mFillPaint.setColorFilter(mColorFilter);
            mFillPaint.setDither(st.mDither);
            
            if ((mColorFilter != null) && (st.mColorStateList == null))
                mFillPaint.setColor(mAlpha << 24);
            
            if (haveStroke) {
            
                mStrokePaint.setAlpha(currStrokeAlpha);
                mStrokePaint.setColorFilter(mColorFilter);
                mStrokePaint.setDither(st.mDither);
            
            }
        
        }
        
        switch (st.mShape) {
        
            case OVAL:
                canvas.drawOval(mRect, mFillPaint);
                
                if (haveStroke)
                    canvas.drawOval(mRect, mStrokePaint);
                
                break;
            case LINE:
                RectF r = mRect;
                float y = r.centerY();
                
                if (haveStroke)
                    canvas.drawLine(r.left, y, r.right, y, mStrokePaint);
                
                break;
            case RECTANGLE:
                if (st.mRadiusArray != null) {
                
                    buildPathIfDirty();
                    canvas.drawPath(mPath, mFillPaint);
                    
                    if (haveStroke)
                        canvas.drawPath(mPath, mStrokePaint);
                
                } else if (st.mRadius > 0.0f) {
                
                    // since the caller is only giving us 1 value, we will force
                    // it to be square if the rect is too small in one dimension
                    // to show it. If we did nothing, Skia would clamp the rad
                    // independently along each axis, giving us a thin ellipse
                    // if the rect were very wide but not very tall
                    float rad = Math.min(st.mRadius, Math.min(mRect.width(), mRect.height()) * 0.5f);
                    canvas.drawRoundRect(mRect, rad, rad, mFillPaint);
                    
                    if (haveStroke)
                        canvas.drawRoundRect(mRect, rad, rad, mStrokePaint);
                
                } else {
                
                    if ((mFillPaint.getColor() != 0) || (mColorFilter != null) || (mFillPaint.getShader() != null))
                        canvas.drawRect(mRect, mFillPaint);
                    
                    if (haveStroke)
                        canvas.drawRect(mRect, mStrokePaint);
                
                }
                
                break;
            case RING:
                Path path = buildRing(st);
                canvas.drawPath(path, mFillPaint);
                
                if (haveStroke)
                    canvas.drawPath(path, mStrokePaint);
                
                break;
        
        }
        
        if (useLayer)
            canvas.restore();
        else {
        
            mFillPaint.setAlpha(prevFillAlpha);
            
            if (haveStroke)
                mStrokePaint.setAlpha(prevStrokeAlpha);
        
        }
    
    }
    
    /**
     * This checks mGradientIsDirty, and if it is true, recomputes both our drawing
     * rectangle (mRect) and the gradient itself, since it depends on our
     * rectangle too.
     *
     * @return true if the resulting rectangle is not empty, false otherwise
     */
    private boolean ensureValidRect() {
    
        if (mGradientIsDirty) {
        
            mGradientIsDirty = false;
            
            
            float inset = 0;
            Rect bounds = getBounds();
            
            if (mStrokePaint != null)
                inset = mStrokePaint.getStrokeWidth() * 0.5f;
            
            mRect.set((bounds.left + inset), (bounds.top + inset), (bounds.right - inset), (bounds.bottom - inset));
            
            final GradientState st = mGradientState;
            final int[] colors = st.mColors;
            
            if (colors != null) {
            
                float x0, x1, y0, y1;
                RectF r = mRect;
                
                if (st.mGradient == LINEAR_GRADIENT) {
                
                    final float level = (st.mUseLevel ? (getLevel() / 10000.0f) : 1.0f);
                    
                    switch (st.mOrientation) {
                    
                        case TOP_BOTTOM:
                            x0 = r.left;
                            x1 = x0;
                            y0 = r.top;
                            y1 = (level * r.bottom);
                            break;
                        case TR_BL:
                            x0 = r.right;
                            x1 = (level * r.left);
                            y0 = r.top;
                            y1 = (level * r.bottom);
                            break;
                        case RIGHT_LEFT:
                            x0 = r.right;
                            x1 = (level * r.left);
                            y0 = r.top;
                            y1 = y0;
                            break;
                        case BR_TL:
                            x0 = r.right;
                            x1 = (level * r.left);
                            y0 = r.bottom;
                            y1 = (level * r.top);
                            break;
                        case BOTTOM_TOP:
                            x0 = r.left;
                            x1 = x0;
                            y0 = r.bottom;
                            y1 = (level * r.top);
                            break;
                        case BL_TR:
                            x0 = r.left;
                            x1 = (level * r.right);
                            y0 = r.bottom;
                            y1 = (level * r.top);
                            break;
                        case LEFT_RIGHT:
                            x0 = r.left;
                            x1 = (level * r.right);
                            y0 = r.top;
                            y1 = y0;
                            break;
                        default:/* TL_BR */
                            x0 = r.left;
                            x1 = (level * r.right);
                            y0 = r.top;
                            y1 = (level * r.bottom);
                            break;
                    
                    }
                    
                    mFillPaint.setShader(new LinearGradient(x0, y0, x1, y1, colors, st.mPositions, Shader.TileMode.CLAMP));
                
                } else if (st.mGradient == RADIAL_GRADIENT) {
                
                    x0 = (r.left + (r.right - r.left) * st.mCenterX);
                    y0 = (r.top + (r.bottom - r.top) * st.mCenterY);
                    
                    float radius = st.mGradientRadius;
                    
                    if (st.mGradientRadiusType == RADIUS_TYPE_FRACTION)
                        radius *= Math.min(st.mWidth, st.mHeight);
                    else if (st.mGradientRadiusType == RADIUS_TYPE_FRACTION_PARENT)
                        radius *= Math.min(r.width(), r.height());
                    
                    if (st.mUseLevel)
                        radius *= (getLevel() / 10000.0f);
                    
                    mGradientRadius = radius;
                    
                    if (radius == 0)
                        // We can't have a shader with zero radius, so let's
                        // have a very, very small radius.
                        radius = 0.001f;
                    
                    mFillPaint.setShader(new RadialGradient(x0, y0, radius, colors, null, Shader.TileMode.CLAMP));
                
                } else if (st.mGradient == SWEEP_GRADIENT) {
                
                    x0 = (r.left + (r.right - r.left) * st.mCenterX);
                    y0 = (r.top + (r.bottom - r.top) * st.mCenterY);
                    
                    float[] tempPositions = null;
                    int[] tempColors = colors;
                    
                    if (st.mUseLevel) {
                    
                        tempColors = st.mTempColors;
                        
                        final int length = colors.length;
                        
                        if ((tempColors == null) || (tempColors.length != (length + 1)))
                            tempColors = st.mTempColors = new int[length + 1];
                        
                        System.arraycopy(colors, 0, tempColors, 0, length);
                        
                        tempColors[length] = colors[length - 1];
                        tempPositions = st.mTempPositions;
                        
                        final float fraction = (1.0f / (length - 1));
                        
                        if ((tempPositions == null) || (tempPositions.length != (length + 1)))
                            tempPositions = st.mTempPositions = new float[length + 1];
                        
                        final float level = (getLevel() / 10000.0f);
                        
                        for (int i = 0; i < length; ++i)
                            tempPositions[i] = (i * fraction * level);
                        
                        tempPositions[length] = 1.0f;
                    
                    }
                    
                    mFillPaint.setShader(new SweepGradient(x0, y0, tempColors, tempPositions));
                
                }
                
                // If we don't have a solid color, the alpha channel must be
                // maxed out so that alpha modulation works correctly.
                if (st.mColorStateList == null)
                    mFillPaint.setColor(Color.BLACK);
            
            }
        
        }
        
        return !mRect.isEmpty();
    
    }
    
    @Override
    public int getAlpha() {
        return mAlpha;
    }
    
    @Override
    public int getChangingConfigurations() {
        return (super.getChangingConfigurations() | mGradientState.mChangingConfigurations);
    }
    
    @Override
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }
    
    @Override
    public ConstantState getConstantState() {
    
        mGradientState.mChangingConfigurations = getChangingConfigurations();
        return mGradientState;
    
    }
    
    private static float getFloatOrFraction(TypedArray a, int index, float defaultValue) {
    
        TypedValue tv = a.peekValue(index);
        float v = defaultValue;
        
        if (tv != null) {
        
            boolean vIsFraction = (tv.type == TypedValue.TYPE_FRACTION);
            v = (vIsFraction ? tv.getFraction(1.0f, 1.0f) : tv.getFloat());
        
        }
        
        return v;
    
    }
    
    /**
     * Returns the radius of the gradient in pixels. The radius is valid only
     * when the gradient type is set to {@link #RADIAL_GRADIENT}.
     *
     * @return Radius in pixels.
     */
    public float getGradientRadius() {
    
        if (mGradientState.mGradient != RADIAL_GRADIENT)
            return 0;
        
        ensureValidRect();
        return mGradientRadius;
    
    }
    
    @Override
    public int getIntrinsicHeight() {
        return mGradientState.mHeight;
    }
    
    @Override
    public int getIntrinsicWidth() {
        return mGradientState.mWidth;
    }
    
    /** Returns the orientation of the gradient defined in this drawable. */
    public Orientation getOrientation() {
        return mGradientState.mOrientation;
    }
    
    @Override
    public int getOpacity() {
        return (((mAlpha == 255) && mGradientState.mOpaqueOverBounds && isOpaqueForState()) ?
            PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT);
    }
    
    @Override
    @TargetApi(21)
    public void getOutline(Outline outline) {
    
        final GradientState st = mGradientState;
        final Rect bounds = getBounds();
        
        // only report non-zero alpha if shape being drawn is opaque
        outline.setAlpha(st.mOpaqueOverShape && isOpaqueForState() ? (mAlpha / 255.0f) : 0.0f);
        
        switch (st.mShape) {
        
            case RECTANGLE:
                if (st.mRadiusArray != null) {
                
                    buildPathIfDirty();
                    outline.setConvexPath(mPath);
                    
                    return;
                
                }
                
                float rad = 0;
                
                if (st.mRadius > 0.0f)
                    // clamp the radius based on width & height, matching behavior in draw()
                    rad = Math.min(st.mRadius, (Math.min(bounds.width(), bounds.height()) * 0.5f));
                
                outline.setRoundRect(bounds, rad);
                return;
            case OVAL:
                outline.setOval(bounds);
                return;
            case LINE:
                // Hairlines (0-width stroke) must have a non-empty outline for
                // shadows to draw correctly, so we'll use a very small width.
                final float halfStrokeWidth = ((mStrokePaint == null) ? 0.0001f : mStrokePaint.getStrokeWidth() * 0.5f);
                final float centerY = bounds.centerY();
                
                final int bottom = (int) Math.ceil(centerY + halfStrokeWidth);
                final int top = (int) Math.floor(centerY - halfStrokeWidth);
                
                outline.setRect(bounds.left, top, bounds.right, bottom);
                return;
            default:
                // TODO: support more complex shapes
        
        }
    
    }
    
    @Override
    public boolean getPadding(Rect padding) {
    
        if (mPadding != null) {
        
            padding.set(mPadding);
            return true;
        
        } else
            return super.getPadding(padding);
    
    }
    
    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        
        final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawable);
        super.inflateWithAttributes(a, R.styleable.GradientDrawable_android_visible);
        
        updateStateFromTypedArray(null, a, null);
        a.recycle();
        
        inflateChildElements(r, parser, attrs, theme);
        mGradientState.computeOpacity();
    
    }
    
    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
        throws XmlPullParserException, IOException {
        
        TypedArray a;
        
        final int innerDepth = (parser.getDepth() + 1);
        int depth, type;
        
        while (((type = parser.next()) != XmlPullParser.END_DOCUMENT)
            && (((depth = parser.getDepth()) >= innerDepth) || (type != XmlPullParser.END_TAG))) {
            
            if (type != XmlPullParser.START_TAG)
                continue;
            
            if (depth > innerDepth)
                continue;
            
            String name = parser.getName();
            
            if (name.equals("size")) {
            
                a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableSize);
                updateGradientDrawableSize(null, a, null);
                a.recycle();
            
            } else if (name.equals("gradient")) {
            
                a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableGradient);
                updateGradientDrawableGradient(r, null, a, null);
                a.recycle();
            
            } else if (name.equals("solid")) {
            
                a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableSolid);
                updateGradientDrawableSolid(null, a, null);
                a.recycle();
            
            } else if (name.equals("stroke")) {
            
                a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableStroke);
                updateGradientDrawableStroke(null, a, null);
                a.recycle();
            
            } else if (name.equals("corners")) {
            
                a = obtainAttributes(r, theme, attrs, R.styleable.DrawableCorners);
                updateDrawableCorners(null, a, null);
                a.recycle();
            
            } else if (name.equals("padding")) {
            
                a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawablePadding);
                updateGradientDrawablePadding(null, a, null);
                a.recycle();
            
            } else
                Log.w("drawable", "Bad element under <shape>: " + name);
        
        }
    
    }
    
    private void initializeWithState(GradientState state) {
    
        if (state.mColorStateList != null) {
        
            final int[] currentState = getState();
            
            final int stateColor = state.mColorStateList.getColorForState(currentState, 0);
            mFillPaint.setColor(stateColor);
        
        } else if (state.mColors == null)
            // If we don't have a solid color and we don't have a gradient,
            // the app is stroking the shape, set the color to the default
            // value of state.mSolidColor
            mFillPaint.setColor(0);
        else
            // Otherwise, make sure the fill alpha is maxed out.
            mFillPaint.setColor(Color.BLACK);
        
        mPadding = state.mPadding;
        
        if (state.mStrokeWidth >= 0) {
        
            mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mStrokePaint.setStrokeWidth(state.mStrokeWidth);
            mStrokePaint.setStyle(Paint.Style.STROKE);
            
            if (state.mStrokeColorStateList != null) {
            
                final int[] currentState = getState();
                
                final int strokeStateColor = state.mStrokeColorStateList.getColorForState(currentState, 0);
                mStrokePaint.setColor(strokeStateColor);
            
            }
            
            if (state.mStrokeDashWidth != 0.0f) {
            
                final DashPathEffect e = new DashPathEffect(new float[] { state.mStrokeDashWidth, state.mStrokeDashGap }, 0);
                mStrokePaint.setPathEffect(e);
            
            }
        
        }
    
    }
    
    static boolean isOpaque(int color) {
        return ((color >> 24) & 0xff) == 0xff;
    }
    
    private boolean isOpaqueForState() {
    
        if ((mGradientState.mStrokeWidth >= 0) && (mStrokePaint != null) && !isOpaque(mStrokePaint.getColor()))
            return false;
        
        if (!isOpaque(mFillPaint.getColor()))
            return false;
        
        return true;
    
    }
    
    @Override
    public boolean isStateful() {
    
        final GradientState s = mGradientState;
        return (super.isStateful()
            || ((s.mColorStateList != null) && s.mColorStateList.isStateful())
                || ((s.mStrokeColorStateList != null) && s.mStrokeColorStateList.isStateful()));
    
    }
    
    private int modulateAlpha(int alpha) {
    
        int scale = mAlpha + (mAlpha >> 7);
        return alpha * scale >> 8;
    
    }
    
    @Override
    public Drawable mutate() {
    
        if (!mMutated && (super.mutate() == this)) {
        
            mGradientState = new GradientState(mGradientState);
            initializeWithState(mGradientState);
            
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
    protected void onBoundsChange(Rect r) {
        super.onBoundsChange(r);
        
        mGradientIsDirty = true;
        mPathIsDirty = true;
        mRingPath = null;
    
    }
    
    @Override
    protected boolean onLevelChange(int level) {
        super.onLevelChange(level);
        
        mGradientIsDirty = true;
        mPathIsDirty = true;
        
        invalidateSelf();
        return true;
    
    }
    
    @Override
    protected boolean onStateChange(int[] stateSet) {
    
        boolean invalidateSelf = false;
        
        final GradientState s = mGradientState;
        final ColorStateList stateList = s.mColorStateList;
        
        if (stateList != null) {
        
            final int newColor = stateList.getColorForState(stateSet, 0);
            final int oldColor = mFillPaint.getColor();
            
            if (oldColor != newColor) {
            
                mFillPaint.setColor(newColor);
                invalidateSelf = true;
            
            }
        
        }
        
        final Paint strokePaint = mStrokePaint;
        
        if (strokePaint != null) {
        
            final ColorStateList strokeStateList = s.mStrokeColorStateList;
            
            if (strokeStateList != null) {
            
                final int newStrokeColor = strokeStateList.getColorForState(stateSet, 0);
                final int oldStrokeColor = strokePaint.getColor();
                
                if (oldStrokeColor != newStrokeColor) {
                
                    strokePaint.setColor(newStrokeColor);
                    invalidateSelf = true;
                
                }
            
            }
        
        }
        
        if (invalidateSelf) {
        
            invalidateSelf();
            return true;
        
        }
        
        return false;
    
    }
    
    @Override
    public void setAlpha(int alpha) {
    
        if (alpha != mAlpha) {
        
            mAlpha = alpha;
            invalidateSelf();
        
        }
    
    }
    
    /**
     * <p>Changes this drawable to use a single color instead of a gradient.</p>
     *
     * <p><strong>Note</strong>: changing color will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing the color.</p>
     *
     * @param argb The color used to fill the shape
     *
     * @see #mutate()
     * @see #setColors(int[])
     */
    public void setColor(int argb) {
    
        mGradientState.setColorStateList(ColorStateList.valueOf(argb));
        mFillPaint.setColor(argb);
        
        invalidateSelf();
    
    }
    
    /**
     * Changes this drawable to use a single color state list instead of a
     * gradient. Calling this method with a null argument will clear the color
     * and is equivalent to calling {@link #setColor(int)} with the argument
     * {@link Color#TRANSPARENT}.
     *
     * <p><strong>Note</strong>: changing color will affect all instances of a
     * drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing the color.</p>
     *
     * @param colorStateList The color state list used to fill the shape
     *
     * @see #mutate()
     */
    public void setColor(ColorStateList colorStateList) {
    
        mGradientState.setColorStateList(colorStateList);
        
        final int color;
        
        if (colorStateList == null)
            color = Color.TRANSPARENT;
        else {
        
            final int[] stateSet = getState();
            color = colorStateList.getColorForState(stateSet, 0);
        
        }
        
        mFillPaint.setColor(color);
        invalidateSelf();
    
    }
    
    @Override
    public void setColorFilter(ColorFilter cf) {
    
        if (cf != mColorFilter) {
        
            mColorFilter = cf;
            invalidateSelf();
        
        }
    
    }
    
    /**
     * <p>Sets the colors used to draw the gradient. Each color is specified as an
     * ARGB integer and the array must contain at least 2 colors.</p>
     *
     * <p><strong>Note</strong>: changing orientation will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing the orientation.</p>
     *
     * @param colors 2 or more ARGB colors
     *
     * @see #mutate()
     * @see #setColor(int)
     */
    public void setColors(int[] colors) {
    
        mGradientState.setColors(colors);
        mGradientIsDirty = true;
        
        invalidateSelf();
    
    }
    
    /**
     * <p>Specify radii for each of the 4 corners. For each corner, the array
     * contains 2 values, <code>[X_radius, Y_radius]</code>. The corners are ordered
     * top-left, top-right, bottom-right, bottom-left. This property
     * is honored only when the shape is of type {@link #RECTANGLE}.</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param radii 4 pairs of X and Y radius for each corner, specified in pixels.
     *              The length of this array must be >= 8
     *
     * @see #mutate()
     * @see #setCornerRadii(float[])
     * @see #setShape(int)
     */
    public void setCornerRadii(float[] radii) {
    
        mGradientState.setCornerRadii(radii);
        mPathIsDirty = true;
        
        invalidateSelf();
    
    }
    
    /**
     * <p>Specify radius for the corners of the gradient. If this is > 0, then the
     * drawable is drawn in a round-rectangle, rather than a rectangle. This property
     * is honored only when the shape is of type {@link #RECTANGLE}.</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param radius The radius in pixels of the corners of the rectangle shape
     *
     * @see #mutate()
     * @see #setCornerRadii(float[])
     * @see #setShape(int)
     */
    public void setCornerRadius(float radius) {
    
        mGradientState.setCornerRadius(radius);
        mPathIsDirty = true;
        
        invalidateSelf();
    
    }
    
    @Override
    public void setDither(boolean dither) {
    
        if (dither != mGradientState.mDither) {
        
            mGradientState.mDither = dither;
            invalidateSelf();
        
        }
    
    }
    
    /**
     * <p>Sets the center location of the gradient. The radius is honored only when
     * the gradient type is set to {@link #RADIAL_GRADIENT} or {@link #SWEEP_GRADIENT}.</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param x The x coordinate of the gradient's center
     * @param y The y coordinate of the gradient's center
     *
     * @see #mutate()
     * @see #setGradientType(int)
     */
    public void setGradientCenter(float x, float y) {
    
        mGradientState.setGradientCenter(x, y);
        mGradientIsDirty = true;
        
        invalidateSelf();
    
    }
    
    /**
     * <p>Sets the radius of the gradient. The radius is honored only when the
     * gradient type is set to {@link #RADIAL_GRADIENT}.</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param gradientRadius The radius of the gradient in pixels
     *
     * @see #mutate()
     * @see #setGradientType(int)
     */
    public void setGradientRadius(float gradientRadius) {
    
        mGradientState.setGradientRadius(gradientRadius, TypedValue.COMPLEX_UNIT_PX);
        mGradientIsDirty = true;
        
        invalidateSelf();
    
    }
    
    /**
     * <p>Sets the type of gradient used by this drawable..</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param gradient The type of the gradient: {@link #LINEAR_GRADIENT},
     *                 {@link #RADIAL_GRADIENT} or {@link #SWEEP_GRADIENT}
     *
     * @see #mutate()
     */
    public void setGradientType(int gradient) {
    
        mGradientState.setGradientType(gradient);
        mGradientIsDirty = true;
        
        invalidateSelf();
    
    }
    
    /**
     * <p>Changes the orientation of the gradient defined in this drawable.</p>
     *
     * <p><strong>Note</strong>: changing orientation will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing the orientation.</p>
     *
     * @param orientation The desired orientation (angle) of the gradient
     *
     * @see #mutate()
     */
    public void setOrientation(Orientation orientation) {
    
        mGradientState.mOrientation = orientation;
        mGradientIsDirty = true;
        
        invalidateSelf();
    
    }
    
    public void setPadding(int left, int top, int right, int bottom) {
    
        final GradientState st = mGradientState;
        
        if (st.mPadding == null)
            st.mPadding = new Rect();
        
        final Rect pad = st.mPadding;
        pad.set(left, top, right, bottom);
        
        mPadding = pad;
    
    }
    
    /**
     * <p>Sets the type of shape used to draw the gradient.</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param shape The desired shape for this drawable: {@link #LINE},
     *              {@link #OVAL}, {@link #RECTANGLE} or {@link #RING}
     *
     * @see #mutate()
     */
    public void setShape(int shape) {
    
        mPathIsDirty = true;
        mRingPath = null;
        
        mGradientState.setShape(shape);
        invalidateSelf();
    
    }
    
    /**
     * <p>Sets the size of the shape drawn by this drawable.</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param width  The width of the shape used by this drawable
     * @param height The height of the shape used by this drawable
     *
     * @see #mutate()
     * @see #setGradientType(int)
     */
    public void setSize(int width, int height) {
    
        mGradientState.setSize(width, height);
        mPathIsDirty = true;
        
        invalidateSelf();
    
    }
    
    /**
     * <p>Set the stroke width and color for the drawable. If width is zero,
     * then no stroke is drawn.</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param width The width in pixels of the stroke
     * @param color The color of the stroke
     *
     * @see #mutate()
     * @see #setStroke(int, int, float, float)
     */
    public void setStroke(int width, int color) {
        setStroke(width, color, 0, 0);
    }
    
    /**
     * <p>Set the stroke width and color state list for the drawable. If width
     * is zero, then no stroke is drawn.</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param width The width in pixels of the stroke
     * @param colorStateList The color state list of the stroke
     *
     * @see #mutate()
     * @see #setStroke(int, ColorStateList, float, float)
     */
    public void setStroke(int width, ColorStateList colorStateList) {
        setStroke(width, colorStateList, 0, 0);
    }
    
    /**
     * <p>Set the stroke width and color for the drawable. If width is zero,
     * then no stroke is drawn. This method can also be used to dash the stroke.</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param width     The width in pixels of the stroke
     * @param color     The color of the stroke
     * @param dashWidth The length in pixels of the dashes, set to 0 to disable dashes
     * @param dashGap   The gap in pixels between dashes
     *
     * @see #mutate()
     * @see #setStroke(int, int)
     */
    public void setStroke(int width, int color, float dashWidth, float dashGap) {
    
        mGradientState.setStroke(width, ColorStateList.valueOf(color), dashWidth, dashGap);
        setStrokeInternal(width, color, dashWidth, dashGap);
    
    }
    
    /**
     * <p>Set the stroke width and color state list for the drawable. If width
     * is zero, then no stroke is drawn. This method can also be used to dash
     * the stroke.</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param width The width in pixels of the stroke
     * @param colorStateList The color state list of the stroke
     * @param dashWidth The length in pixels of the dashes, set to 0 to disable dashes
     * @param dashGap The gap in pixels between dashes
     *
     * @see #mutate()
     * @see #setStroke(int, ColorStateList)
     */
    public void setStroke(int width, ColorStateList colorStateList, float dashWidth, float dashGap) {
    
        mGradientState.setStroke(width, colorStateList, dashWidth, dashGap);
        
        final int color;
        
        if (colorStateList == null)
            color = Color.TRANSPARENT;
        else {
        
            final int[] stateSet = getState();
            color = colorStateList.getColorForState(stateSet, 0);
        
        }
        
        setStrokeInternal(width, color, dashWidth, dashGap);
    
    }
    
    private void setStrokeInternal(int width, int color, float dashWidth, float dashGap) {
    
        if (mStrokePaint == null)  {
        
            mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mStrokePaint.setStyle(Paint.Style.STROKE);
        
        }
        
        mStrokePaint.setColor(color);
        mStrokePaint.setStrokeWidth(width);
        
        DashPathEffect e = null;
        
        if (dashWidth > 0)
            e = new DashPathEffect(new float[] { dashWidth, dashGap }, 0);
        
        mStrokePaint.setPathEffect(e);
        invalidateSelf();
    
    }
    
    /**
     * <p>Sets whether or not this drawable will honor its <code>level</code>
     * property.</p>
     *
     * <p><strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     * {@link #mutate()} before changing this property.</p>
     *
     * @param useLevel True if this drawable should honor its level, false otherwise
     *
     * @see #mutate()
     * @see #setLevel(int)
     * @see #getLevel()
     */
    public void setUseLevel(boolean useLevel) {
    
        mGradientState.mUseLevel = useLevel;
        mGradientIsDirty = true;
        
        invalidateSelf();
    
    }
    
    private void updateDrawableCorners(Theme theme, TypedArray a, TypedValue[] values) {
    
        final GradientState st = mGradientState;
        
        // Account for any configuration changes.
        st.mChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        st.mAttrCorners = TypedArrayCompat.extractThemeAttrs(a);
        
        final int radius = a.getDimensionPixelSize(R.styleable.DrawableCorners_android_radius, (int) st.mRadius);
        setCornerRadius(radius);
        
        // TODO: Update these to be themeable.
        final int bottomLeftRadius = a.getDimensionPixelSize(R.styleable.DrawableCorners_android_bottomLeftRadius,
            radius);
        final int bottomRightRadius = a.getDimensionPixelSize(R.styleable.DrawableCorners_android_bottomRightRadius,
            radius);
        final int topLeftRadius = a.getDimensionPixelSize(R.styleable.DrawableCorners_android_topLeftRadius,
            radius);
        final int topRightRadius = a.getDimensionPixelSize(R.styleable.DrawableCorners_android_topRightRadius,
            radius);
        
        if ((bottomLeftRadius != radius) || (topLeftRadius != radius) ||
            (topRightRadius != radius) || (bottomRightRadius != radius))
                // The corner radii are specified in clockwise order (see Path.addRoundRect())
                setCornerRadii(new float[] {
                    topLeftRadius, topLeftRadius, topRightRadius, topRightRadius,
                        bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius
                });
    
    }
    
    private void updateGradientDrawableGradient(Resources r, Theme theme, TypedArray a, TypedValue[] values)
        throws XmlPullParserException {
        
        final GradientState st = mGradientState;
        
        // Account for any configuration changes.
        st.mChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        st.mAttrGradient = TypedArrayCompat.extractThemeAttrs(a);
        
        st.mCenterX = getFloatOrFraction(a, R.styleable.GradientDrawableGradient_android_centerX, st.mCenterX);
        st.mCenterY = getFloatOrFraction(a, R.styleable.GradientDrawableGradient_android_centerY, st.mCenterY);
        st.mGradient = a.getInt(R.styleable.GradientDrawableGradient_android_type, st.mGradient);
        st.mUseLevel = a.getBoolean(R.styleable.GradientDrawableGradient_android_useLevel, st.mUseLevel);
        
        // TODO: Update these to be themeable.
        final int startColor = a.getColor(R.styleable.GradientDrawableGradient_android_startColor, 0);
        
        final boolean hasCenterColor = a.hasValue(R.styleable.GradientDrawableGradient_android_centerColor);
        final int centerColor = a.getColor(R.styleable.GradientDrawableGradient_android_centerColor, 0);
        
        final int endColor = a.getColor(R.styleable.GradientDrawableGradient_android_endColor, 0);
        
        if (hasCenterColor) {
        
            st.mColors = new int[3];
            st.mColors[0] = startColor;
            st.mColors[1] = centerColor;
            st.mColors[2] = endColor;
            
            st.mPositions = new float[3];
            st.mPositions[0] = 0.0f;
            
            // Since 0.5f is default value, try to take the one that isn't 0.5f
            st.mPositions[1] = st.mCenterX != 0.5f ? st.mCenterX : st.mCenterY;
            st.mPositions[2] = 1f;
        
        } else {
        
            st.mColors = new int[2];
            st.mColors[0] = startColor;
            st.mColors[1] = endColor;
        
        }
        
        if (st.mGradient == LINEAR_GRADIENT) {
        
            int angle = (int) a.getFloat(R.styleable.GradientDrawableGradient_android_angle, st.mAngle);
            angle %= 360;
            
            if ((angle % 45) != 0)
                throw new XmlPullParserException(a.getPositionDescription()
                    + "<gradient> tag requires 'angle' attribute to be a multiple of 45");
            
            st.mAngle = angle;
            
            switch (angle) {
            
                case 0:
                    st.mOrientation = Orientation.LEFT_RIGHT;
                    break;
                case 45:
                    st.mOrientation = Orientation.BL_TR;
                    break;
                case 90:
                    st.mOrientation = Orientation.BOTTOM_TOP;
                    break;
                case 135:
                    st.mOrientation = Orientation.BR_TL;
                    break;
                case 180:
                    st.mOrientation = Orientation.RIGHT_LEFT;
                    break;
                case 225:
                    st.mOrientation = Orientation.TR_BL;
                    break;
                case 270:
                    st.mOrientation = Orientation.TOP_BOTTOM;
                    break;
                case 315:
                    st.mOrientation = Orientation.TL_BR;
                    break;
            
            }
        
        } else {
        
            final TypedValue tv = a.peekValue(R.styleable.GradientDrawableGradient_android_gradientRadius);
            
            if (tv != null) {
            
                final float radius;
                final int radiusType;
                
                if (tv.type == TypedValue.TYPE_FRACTION) {
                
                    radius = tv.getFraction(1.0f, 1.0f);
                    
                    final int unit = (tv.data >> TypedValue.COMPLEX_UNIT_SHIFT)
                        & TypedValue.COMPLEX_UNIT_MASK;
                    
                    if (unit == TypedValue.COMPLEX_UNIT_FRACTION_PARENT)
                        radiusType = RADIUS_TYPE_FRACTION_PARENT;
                    else
                        radiusType = RADIUS_TYPE_FRACTION;
                
                } else {
                
                    radius = tv.getDimension(r.getDisplayMetrics());
                    radiusType = RADIUS_TYPE_PIXELS;
                
                }
                
                st.mGradientRadius = radius;
                st.mGradientRadiusType = radiusType;
            
            } else if (st.mGradient == RADIAL_GRADIENT)
                throw new XmlPullParserException(a.getPositionDescription()
                    + "<gradient> tag requires 'gradientRadius' attribute with radial type");
        
        }
    
    }
    
    private void updateGradientDrawablePadding(Theme theme, TypedArray a, TypedValue[] values) {
    
        final GradientState st = mGradientState;
        
        // Account for any configuration changes.
        st.mChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        st.mAttrPadding = TypedArrayCompat.extractThemeAttrs(a);
        
        if (st.mPadding == null)
            st.mPadding = new Rect();
        
        final Rect pad = st.mPadding;
        pad.set(a.getDimensionPixelOffset(R.styleable.GradientDrawablePadding_android_left, pad.left),
                a.getDimensionPixelOffset(R.styleable.GradientDrawablePadding_android_top, pad.top),
                a.getDimensionPixelOffset(R.styleable.GradientDrawablePadding_android_right, pad.right),
                a.getDimensionPixelOffset(R.styleable.GradientDrawablePadding_android_bottom, pad.bottom));
        
        mPadding = pad;
    
    }
    
    private void updateGradientDrawableSize(Theme theme, TypedArray a, TypedValue[] values) {
    
        final GradientState st = mGradientState;
        
        // Account for any configuration changes.
        st.mChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        st.mAttrSize = TypedArrayCompat.extractThemeAttrs(a);
        
        st.mHeight = a.getDimensionPixelSize(R.styleable.GradientDrawableSize_android_height, st.mHeight);
        st.mWidth = a.getDimensionPixelSize(R.styleable.GradientDrawableSize_android_width, st.mWidth);
    
    }
    
    private void updateGradientDrawableSolid(Theme theme, TypedArray a, TypedValue[] values) {
    
        final GradientState st = mGradientState;
        
        // Account for any configuration changes.
        st.mChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        st.mAttrSolid = TypedArrayCompat.extractThemeAttrs(a);
        
        final ColorStateList colorStateList = a.getColorStateList(R.styleable.GradientDrawableSolid_android_color);
        
        if (colorStateList != null)
            setColor(colorStateList);
    
    }
    
    private void updateGradientDrawableStroke(Theme theme, TypedArray a, TypedValue[] values) {
    
        final GradientState st = mGradientState;
        
        // Account for any configuration changes.
        st.mChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        st.mAttrStroke = TypedArrayCompat.extractThemeAttrs(a);
        
        // We have an explicit stroke defined, so the default stroke width
        // must be at least 0 or the current stroke width.
        final int defaultStrokeWidth = Math.max(0, st.mStrokeWidth);
        final int width = a.getDimensionPixelSize(R.styleable.GradientDrawableStroke_android_width, defaultStrokeWidth);
        
        final float dashWidth = a.getDimension(R.styleable.GradientDrawableStroke_android_dashWidth, st.mStrokeDashWidth);
        ColorStateList colorStateList = a.getColorStateList(R.styleable.GradientDrawableStroke_android_color);
        
        if (colorStateList == null)
            colorStateList = st.mStrokeColorStateList;
        
        if (dashWidth != 0.0f) {
        
            final float dashGap = a.getDimension(R.styleable.GradientDrawableStroke_android_dashGap, st.mStrokeDashGap);
            setStroke(width, colorStateList, dashWidth, dashGap);
        
        } else
            setStroke(width, colorStateList);
    
    }
    
    /** Initializes the constant state from the values in the typed array. */
    private void updateStateFromTypedArray(Theme theme, TypedArray a, TypedValue[] values) throws XmlPullParserException {
    
        final GradientState state = mGradientState;
        
        // Account for any configuration changes.
        state.mChangingConfigurations |= TypedArrayCompat.getChangingConfigurations(a);
        
        // Extract the theme attributes, if any.
        state.mThemeAttrs = TypedArrayCompat.extractThemeAttrs(a);
        
        state.mDither = a.getBoolean(R.styleable.GradientDrawable_android_dither, state.mDither);
        state.mShape = a.getInt(R.styleable.GradientDrawable_android_shape, state.mShape);
        
        if (state.mShape == RING) {
        
            state.mInnerRadius = a.getDimensionPixelSize(R.styleable.GradientDrawable_android_innerRadius,
                state.mInnerRadius);
            
            if (state.mInnerRadius == -1)
                state.mInnerRadiusRatio = a.getFloat(R.styleable.GradientDrawable_android_innerRadiusRatio,
                    state.mInnerRadiusRatio);
            
            state.mThickness = a.getDimensionPixelSize(R.styleable.GradientDrawable_android_thickness,
                state.mThickness);
            
            if (state.mThickness == -1)
                state.mThicknessRatio = a.getFloat(R.styleable.GradientDrawable_android_thicknessRatio,
                    state.mThicknessRatio);
            
            state.mUseLevelForShape = a.getBoolean(R.styleable.GradientDrawable_android_useLevel,
                state.mUseLevelForShape);
        
        }
    
    }
    
    final static class GradientState extends ConstantState {
    
        private boolean mOpaqueOverBounds;
        private boolean mOpaqueOverShape;
        private boolean mUseLevel;
        private boolean mUseLevelForShape;
        
        private float mCenterX = 0.5f;
        private float mCenterY = 0.5f;
        private float mGradientRadius = 0.5f;
        
        private int mGradientRadiusType = RADIUS_TYPE_PIXELS;
        
        boolean mDither = false;
        
        float mInnerRadiusRatio = DEFAULT_INNER_RADIUS_RATIO;
        float mRadius = 0.0f; // use this if mRadiusArray is null
        float mStrokeDashGap = 0.0f;
        float mStrokeDashWidth = 0.0f;
        float mThicknessRatio = DEFAULT_THICKNESS_RATIO;
        
        float[] mTempPositions; // no need to copy
        float[] mPositions;
        float[] mRadiusArray = null;
        
        int mAngle = 0;
        int mChangingConfigurations;
        int mGradient = LINEAR_GRADIENT;
        int mHeight = -1;
        int mInnerRadius = -1;
        int mShape = RECTANGLE;
        int mStrokeWidth = -1; // if >= 0 use stroking.
        int mThickness = -1;
        int mWidth = -1;
        
        int[] mColors;
        int[] mTempColors; // no need to copy
        
        ColorStateList mColorStateList;
        ColorStateList mStrokeColorStateList;
        
        Orientation mOrientation;
        Rect mPadding = null;
        
        TypedValue[] mAttrCorners;
        TypedValue[] mAttrGradient;
        TypedValue[] mAttrPadding;
        TypedValue[] mAttrSize;
        TypedValue[] mAttrSolid;
        TypedValue[] mAttrStroke;
        TypedValue[] mThemeAttrs;
        
        GradientState(GradientState state) {
        
            mAngle = state.mAngle;
            mChangingConfigurations = state.mChangingConfigurations;
            mColorStateList = state.mColorStateList;
            mGradient = state.mGradient;
            mOrientation = state.mOrientation;
            mShape = state.mShape;
            
            if (state.mColors != null)
                mColors = state.mColors.clone();
            
            if (state.mPositions != null)
                mPositions = state.mPositions.clone();
            
            mRadius = state.mRadius;
            
            mStrokeColorStateList = state.mStrokeColorStateList;
            mStrokeDashGap = state.mStrokeDashGap;
            mStrokeDashWidth = state.mStrokeDashWidth;
            mStrokeWidth = state.mStrokeWidth;
            
            if (state.mRadiusArray != null)
                mRadiusArray = state.mRadiusArray.clone();
            
            if (state.mPadding != null)
                mPadding = new Rect(state.mPadding);
            
            mAttrCorners = state.mAttrCorners;
            mAttrGradient = state.mAttrGradient;
            mAttrPadding = state.mAttrPadding;
            mAttrSize = state.mAttrSize;
            mAttrSolid = state.mAttrSolid;
            mAttrStroke = state.mAttrStroke;
            mCenterX = state.mCenterX;
            mCenterY = state.mCenterY;
            mDither = state.mDither;
            mGradientRadius = state.mGradientRadius;
            mGradientRadiusType = state.mGradientRadiusType;
            mHeight = state.mHeight;
            mInnerRadius = state.mInnerRadius;
            mInnerRadiusRatio = state.mInnerRadiusRatio;
            mOpaqueOverBounds = state.mOpaqueOverBounds;
            mOpaqueOverShape = state.mOpaqueOverShape;
            mThemeAttrs = state.mThemeAttrs;
            mThicknessRatio = state.mThicknessRatio;
            mThickness = state.mThickness;
            mUseLevel = state.mUseLevel;
            mUseLevelForShape = state.mUseLevelForShape;
            mWidth = state.mWidth;
        
        }
        
        GradientState(Orientation orientation, int[] colors) {
        
            mOrientation = orientation;
            setColors(colors);
        
        }
        
        @Override
        public boolean canApplyTheme() {
            return (mThemeAttrs != null);
        }
        
        private void computeOpacity() {
        
            mOpaqueOverBounds = false;
            mOpaqueOverShape = false;
            
            if (mColors != null)
                for (int i = 0; i < mColors.length; ++i)
                    if (!isOpaque(mColors[i]))
                        return;
            
            // An unfilled shape is not opaque over bounds or shape
            if ((mColors == null) && (mColorStateList == null))
                return;
            
            // Colors are opaque, so opaqueOverShape=true,
            mOpaqueOverShape = true;
            
            // and opaqueOverBounds=true if shape fills bounds
            mOpaqueOverBounds = ((mShape == RECTANGLE) && (mRadius <= 0) && (mRadiusArray == null);
        
        }
        
        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
        
        @Override
        public Drawable newDrawable() {
            return new GradientDrawable(this, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res) {
            return new GradientDrawable(this, null);
        }
        
        @Override
        public Drawable newDrawable(Resources res, Theme theme) {
            return new GradientDrawable(this, theme);
        }
        
        void setColorStateList(ColorStateList colorStateList) {
        
            mColors = null;
            mColorStateList = colorStateList;
            
            computeOpacity();
        
        }
        
        void setColors(int[] colors) {
        
            mColors = colors;
            mColorStateList = null;
            
            computeOpacity();
        
        }
        
        void setCornerRadii(float[] radii) {
        
            mRadiusArray = radii;
            
            if (radii == null)
                mRadius = 0;
        
        }
        
        void setCornerRadius(float radius) {
        
            if (radius < 0)
                radius = 0;
            
            mRadius = radius;
            mRadiusArray = null;
        
        }
        
        void setGradientCenter(float x, float y) {
        
            mCenterX = x;
            mCenterY = y;
        
        }
        
        void setGradientRadius(float gradientRadius, int type) {
        
            mGradientRadius = gradientRadius;
            mGradientRadiusType = type;
        
        }
        
        void setGradientType(int gradient) {
            mGradient = gradient;
        }
        
        void setShape(int shape) {
        
            mShape = shape;
            computeOpacity();
        
        }
        
        void setStroke(int width, ColorStateList colorStateList, float dashWidth, float dashGap) {
        
            mStrokeColorStateList = colorStateList;
            mStrokeDashWidth = dashWidth;
            mStrokeDashGap = dashGap;
            mStrokeWidth = width;
            
            computeOpacity();
        
        }
        
        void setSize(int width, int height) {
        
            mHeight = height;
            mWidth = width;
        
        }
    
    }
    
    /** Controls how the gradient is oriented relative to the drawable's bounds. */
    public enum Orientation {
    
        /** draw the gradient from the top to the bottom. */
        TOP_BOTTOM,
        
        /** draw the gradient from the top-right to the bottom-left. */
        TR_BL,
        
        /** draw the gradient from the right to the left. */
        RIGHT_LEFT,
        
        /** draw the gradient from the bottom-right to the top-left. */
        BR_TL,
        
        /** draw the gradient from the bottom to the top. */
        BOTTOM_TOP,
        
        /** draw the gradient from the bottom-left to the top-right. */
        BL_TR,
        
        /** draw the gradient from the left to the right. */
        LEFT_RIGHT,
        
        /** draw the gradient from the top-left to the bottom-right. */
        TL_BR,
    
    }

}