package robertapengelly.support.graphics.drawable;

import  android.content.Context;
import  android.content.res.TypedArray;
import  android.graphics.Canvas;
import  android.graphics.Paint;
import  android.graphics.RectF;

import  robertapengelly.support.animation.Animator;

/** A backported {@code Drawable} for indeterminate horizontal {@code ProgressBar}. */
public class IndeterminateHorizontalProgressDrawable extends BaseIndeterminateProgressDrawable {

    private static final int PADDED_INTRINSIC_HEIGHT_DP = 16;
    private static final int PROGRESS_INTRINSIC_HEIGHT_DP = 4;
    
    private static final RectF RECT_BOUND = new RectF(-180, -1, 180, 1);
    private static final RectF RECT_PADDED_BOUND = new RectF(-180, -4, 180, 4);
    private static final RectF RECT_PROGRESS = new RectF(-144, -1, 144, 1);
    
    private static final RectTransformX RECT_1_TRANSFORM_X = new RectTransformX(-522.6f, 0.1f);
    private static final RectTransformX RECT_2_TRANSFORM_X = new RectTransformX(-197.6f, 0.1f);
    
    private float mBackgroundAlpha;
    private int mPaddedIntrinsicHeight;
    private int mProgressIntrinsicHeight;
    private boolean mShowBackground = true;
    
    private RectTransformX mRect1TransformX = new RectTransformX(RECT_1_TRANSFORM_X);
    private RectTransformX mRect2TransformX = new RectTransformX(RECT_2_TRANSFORM_X);
    
    /**
     * Create a new {@code IndeterminateHorizontalProgressDrawable}.
     *
     * @param context the {@code Context} for retrieving style information.
     */
    public IndeterminateHorizontalProgressDrawable(Context context) {
        super(context);
        
        float density = context.getResources().getDisplayMetrics().density;
        
        mPaddedIntrinsicHeight = Math.round(PADDED_INTRINSIC_HEIGHT_DP * density);
        mProgressIntrinsicHeight = Math.round(PROGRESS_INTRINSIC_HEIGHT_DP * density);
        
        mBackgroundAlpha = getFloatFromAttrRes(android.R.attr.disabledAlpha, context);
        
        mAnimators = new Animator[] {
            Animators.createIndeterminateHorizontalRect1(mRect1TransformX),
            Animators.createIndeterminateHorizontalRect2(mRect2TransformX)
        };
    
    }
    
    private static void drawBackgroundRect(Canvas canvas, Paint paint) {
        canvas.drawRect(RECT_BOUND, paint);
    }
    
    private static void drawProgressRect(Canvas canvas, RectTransformX transformX, Paint paint) {
    
        int saveCount = canvas.save();
        
        canvas.translate(transformX.mTranslateX, 0);
        canvas.scale(transformX.mScaleX, 1);
        
        canvas.drawRect(RECT_PROGRESS, paint);    
        canvas.restoreToCount(saveCount);
    
    }
    
    private float getFloatFromAttrRes(int attrRes, Context context) {
    
        TypedArray a = context.obtainStyledAttributes(new int[] {attrRes});
        
        try {
            return a.getFloat(0, 0);
        } finally {
            a.recycle();
        }
    
    }
    
    /** {@inheritDoc} */
    @Override
    public int getIntrinsicHeight() {
        return (mUseIntrinsicPadding ? mPaddedIntrinsicHeight : mProgressIntrinsicHeight);
    }
    
    /**
     * Get whether this drawable is showing a background. The default is {@code true}.
     *
     * @return Whether this drawable is showing a background.
     */
    public boolean getShowBackground() {
        return mShowBackground;
    }
    
    @Override
    protected void onDraw(Canvas canvas, int width, int height, Paint paint) {
    
        if (mUseIntrinsicPadding) {
        
            canvas.scale((width / RECT_PADDED_BOUND.width()), (height / RECT_PADDED_BOUND.height()));
            canvas.translate((RECT_PADDED_BOUND.width() / 2), (RECT_PADDED_BOUND.height() / 2));
        
        } else {
        
            canvas.scale((width / RECT_BOUND.width()), (height / RECT_BOUND.height()));
            canvas.translate((RECT_BOUND.width() / 2), (RECT_BOUND.height() / 2));
        
        }
        
        if (mShowBackground) {
        
            paint.setAlpha(Math.round(mAlpha * mBackgroundAlpha));
            drawBackgroundRect(canvas, paint);
            
            paint.setAlpha(mAlpha);
        
        }
        
        drawProgressRect(canvas, mRect2TransformX, paint);
        drawProgressRect(canvas, mRect1TransformX, paint);
    
    }
    
    @Override
    protected void onPreparePaint(Paint paint) {
        paint.setStyle(Paint.Style.FILL);
    }
    
    /**
     * Set whether this drawable should show a background. The default is {@code true}.
     *
     * @param show Whether background should be shown.
     */
    public void setShowBackground(boolean show) {
    
        if (mShowBackground != show) {
        
            mShowBackground = show;
            invalidateSelf();
        
        }
    
    }
    
    private static class RectTransformX {
    
        float mTranslateX;
        float mScaleX;
        
        RectTransformX(RectTransformX that) {
        
            mTranslateX = that.mTranslateX;
            mScaleX = that.mScaleX;
        
        }
        
        RectTransformX(float translateX, float scaleX) {
        
            mTranslateX = translateX;
            mScaleX = scaleX;
        
        }
        
        @SuppressWarnings("unused")
        public void setScaleX(float scaleX) {
            mScaleX = scaleX;
        }
        
        @SuppressWarnings("unused")
        public void setTranslateX(float translateX) {
            mTranslateX = translateX;
        }
    
    }

}