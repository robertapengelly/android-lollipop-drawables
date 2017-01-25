package robertapengelly.support.graphics.drawable;

import  android.graphics.PorterDuff;
import  android.graphics.PorterDuffColorFilter;
import  android.os.Build;
import  android.util.Log;

import  java.lang.reflect.Method;

class DrawableReflectiveUtils {

    private static final ColorFilterLruCache COLOR_FILTER_CACHE = new ColorFilterLruCache(6);
    private static final String TAG = "DrawableReflectiveUtils";
    
    private static SimpleArrayMap<String, Method> sCachedMethods = new SimpleArrayMap<>();
    
    final static Class[] INT_ARG = {int.class};
    
    static PorterDuffColorFilter setColor(PorterDuffColorFilter cf, int color, PorterDuff.Mode mode) {
    
        if (Build.VERSION.SDK_INT < 21) {
        
            // First, lets see if the cache already contains the color filter
            PorterDuffColorFilter filter = COLOR_FILTER_CACHE.get(color, mode);
            
            if (filter == null) {
            
                // Cache miss, so create a color filter and add it to the cache
                filter = new PorterDuffColorFilter(color, mode);
                
                COLOR_FILTER_CACHE.put(color, mode, filter);
            
            }
            
            return filter;
        
        }
        
        /** Otherwise invoke native one. */
        tryInvoke(cf, "setColor", INT_ARG, color);
        return cf;
    
    }
    
    static <T> T tryInvoke(Object target, String methodName, Class<?>[] argTypes, Object... args) {
    
        try {
        
            Method method = sCachedMethods.get(methodName);
            
            if (method != null)
                return (T) method.invoke(target, args);
            
            method = target.getClass().getDeclaredMethod(methodName, argTypes);
            sCachedMethods.put(methodName, method);
            
            return (T) method.invoke(target, args);
        
        } catch (Exception pokemon) {
            Log.e(TAG, "Unable to invoke " + methodName + " on " + target, pokemon);
        }
        
        return null;
    
    }
    
    private static class ColorFilterLruCache extends LruCache<Integer, PorterDuffColorFilter> {
    
        ColorFilterLruCache(int maxSize) {
            super(maxSize);
        }
        
        private static int generateCacheKey(int color, PorterDuff.Mode mode) {
        
            int hashCode = 1;
            
            hashCode = (31 * hashCode + color);
            hashCode = (31 * hashCode + mode.hashCode());
            
            return hashCode;
        
        }
        
        PorterDuffColorFilter get(int color, PorterDuff.Mode mode) {
            return get(generateCacheKey(color, mode));
        }
        
        PorterDuffColorFilter put(int color, PorterDuff.Mode mode, PorterDuffColorFilter filter) {
            return put(generateCacheKey(color, mode), filter);
        }
    
    }

}