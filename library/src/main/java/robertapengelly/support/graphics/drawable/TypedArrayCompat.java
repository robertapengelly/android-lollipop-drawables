package robertapengelly.support.graphics.drawable;

import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.TypedArray;
import  android.graphics.drawable.Drawable;
import  android.os.Build;
import  android.util.TypedValue;

class TypedArrayCompat {

    private static final int[] TEMP_ARRAY = new int[1];
    
    /**
     * Extracts theme attributes from a typed array for later resolution using
     * Resources.Theme#resolveAttributes(int[], int[]).
     * Removes the entries from the typed array so that subsequent calls to typed
     * getters will return the default value without crashing.
     *
     * @return an array of length {@link TypedArray#getIndexCount()} populated with theme
     *         attributes, or null if there are no theme attributes in the typed array.
     */
    static TypedValue[] extractThemeAttrs(TypedArray array) {
    
        final int N = array.length();
        TypedValue[] values = null;
        
        for (int index = 0; index < N; ++index) {
        
            final TypedValue value = array.peekValue(index);
            
            if ((value == null) || (value.type != TypedValue.TYPE_ATTRIBUTE))
                continue;
            
            final int attr = value.data;
            
            if (attr == 0)
                continue;
            
            if (values == null)
                values = new TypedValue[N];
            
            values[index] = value;
        
        }
        
        return values;
    
    }
    
    /**
     * Return a mask of the configuration parameters for which the values in
     * this typed array may change.
     *
     * @return Returns a mask of the changing configuration parameters, as
     *         defined by {@link android.content.pm.ActivityInfo}.
     * @see android.content.pm.ActivityInfo
     */
    static int getChangingConfigurations(TypedArray array) {
    
        if (Build.VERSION.SDK_INT >= 21)
            return array.getChangingConfigurations();
        
        return 0;
    
    }
    
    /**
     * Retrieve the ColorStateList for the attribute at <var>index</var>.
     * The value may be either a single solid color or a reference to
     * a color or complex {@link ColorStateList} description.
     *
     * @param index Index of attribute to retrieve.
     * @return ColorStateList for the attribute, or null if not defined.
     */
    static ColorStateList getColorStateList(Resources.Theme theme, TypedArray a, TypedValue[] values, int index) {
    
        if ((values != null) && (theme != null)) {
        
            TypedValue v = values[index];
            
            if (v.type == TypedValue.TYPE_ATTRIBUTE) {
            
                TEMP_ARRAY[0] = v.data;
                TypedArray tmp = theme.obtainStyledAttributes(null, TEMP_ARRAY, 0, 0);
                
                try {
                    return tmp.getColorStateList(0);
                } finally {
                    tmp.recycle();
                }
            
            }
        
        }
        
        if (a != null)
            return a.getColorStateList(index);
        
        return null;
    
    }
    
    /**
     * Retrieve a dimensional unit attribute at <var>index</var> for use
     * as an offset in raw pixels.  This is the same as
     * {@link TypedArray#getDimension}, except the returned value is converted to
     * integer pixels for you.  An offset conversion involves simply
     * truncating the base value to an integer.
     *
     * <p>Retrieve from extracted first if no value than tries from {@link TypedArray}</p>
     *
     * @param index Index of attribute to retrieve.
     * @param def   Value to return if the attribute is not defined or
     *              not a resource.
     * @return Attribute dimension value multiplied by the appropriate
     *         metric and truncated to integer pixels, or defValue if not defined.
     * @see TypedArray#getDimension
     * @see TypedArray#getDimensionPixelSize
     */
    static int getDimensionPixelOffset(Resources.Theme theme, TypedArray a, TypedValue[] values,
        int index, int def) {
        
        if ((values != null) && (theme != null)) {
        
            TypedValue v = values[index];
            
            if (v.type == TypedValue.TYPE_ATTRIBUTE) {
            
                TEMP_ARRAY[0] = v.data;
                TypedArray tmp = theme.obtainStyledAttributes(null, TEMP_ARRAY, 0, 0);
                
                try {
                    return tmp.getDimensionPixelOffset(0, def);
                } finally {
                    tmp.recycle();
                }
            
            }
        
        }
        
        if (a != null)
            return a.getDimensionPixelOffset(index, def);
        
        return def;
    
    }
    
    /**
     * Retrieve the Drawable for the attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     * @return Drawable for the attribute, or null if not defined.
     */
    static Drawable getDrawable(Resources.Theme theme, TypedArray a, TypedValue[] values, int index) {
    
        if ((values != null) && (theme != null)) {
        
            TypedValue v = values[index];
            
            if (v.type == TypedValue.TYPE_ATTRIBUTE) {
            
                TEMP_ARRAY[0] = v.data;
                TypedArray tmp = theme.obtainStyledAttributes(null, TEMP_ARRAY, 0, 0);
                
                try {
                    return tmp.getDrawable(0);
                } finally {
                    tmp.recycle();
                }
            
            }
        
        }
        
        if (a != null)
            return LollipopDrawablesCompat.getDrawable(a, index, theme);
        
        return null;
    
    }
    
    /**
     * Retrieve the resource identifier for the attribute at
     * <var>index</var>.  Note that attribute resource as resolved when
     * the overall {@link TypedArray} object is retrieved.  As a
     * result, this function will return the resource identifier of the
     * final resource value that was found, <em>not</em> necessarily the
     * original resource that was specified by the attribute.
     *
     * @param index Index of attribute to retrieve.
     * @param def   Value to return if the attribute is not defined or
     *              not a resource.
     * @return Attribute resource identifier, or defValue if not defined.
     */
    static int getResourceId(Resources.Theme theme, TypedArray a, TypedValue[] values, int index, int def) {
    
        if ((values != null) && (theme != null)) {
        
            TypedValue v = values[index];
            
            if (v.type == TypedValue.TYPE_ATTRIBUTE) {
            
                TEMP_ARRAY[0] = v.data;
                TypedArray tmp = theme.obtainStyledAttributes(null, TEMP_ARRAY, 0, 0);
                
                try {
                    return tmp.getResourceId(0, def);
                } finally {
                    tmp.recycle();
                }
            
            }
        
        }
        
        if (a != null)
            return a.getResourceId(index, def);
        
        return def;
    
    }

}