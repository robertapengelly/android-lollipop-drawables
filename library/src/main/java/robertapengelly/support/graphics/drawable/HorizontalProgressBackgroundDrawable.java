package robertapengelly.support.graphics.drawable;

import  android.content.Context;
import  android.graphics.Canvas;

class HorizontalProgressBackgroundDrawable extends BaseSingleHorizontalProgressDrawable {

    private boolean mShow = true;
    
    HorizontalProgressBackgroundDrawable(Context context) {
        super(context);
    }
    
    @Override
    public void draw(Canvas canvas) {
    
        if (mShow)
            super.draw(canvas);
    
    }
    
    boolean getShowBackground() {
        return mShow;
    }
    
    void setShowBackground(boolean show) {
    
        if (mShow != show) {
        
            mShow = show;
            invalidateSelf();
        
        }
    
    }

}