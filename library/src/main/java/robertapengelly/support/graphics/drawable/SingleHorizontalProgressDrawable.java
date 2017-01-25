package robertapengelly.support.graphics.drawable;

import  android.content.Context;
import  android.graphics.Canvas;
import  android.graphics.Paint;

class SingleHorizontalProgressDrawable extends BaseSingleHorizontalProgressDrawable {

    private static final int LEVEL_MAX = 10000;
    
    SingleHorizontalProgressDrawable(Context context) {
        super(context);
    }
    
    @Override
    protected void onDrawRect(Canvas canvas, Paint paint) {
    
        int level = getLevel();
        
        if (level == 0)
            return;
        
        int saveCount = canvas.save();
        canvas.scale((float) level / LEVEL_MAX, 1, RECT_BOUND.left, 0);
        
        super.onDrawRect(canvas, paint);
        canvas.restoreToCount(saveCount);
    
    }
    
    @Override
    protected boolean onLevelChange(int level) {
    
        invalidateSelf();
        return true;
    
    }

}