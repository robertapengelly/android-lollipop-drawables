package robertapengelly.support.graphics.drawable;

import  android.content.Context;
import  android.content.res.ColorStateList;
import  android.content.res.Resources;
import  android.content.res.TypedArray;
import  android.graphics.Color;
import  android.graphics.drawable.Drawable;
import  android.os.Build;
import  android.util.TypedValue;

import  robertapengelly.support.lollipopdrawables.R;

class TypedArrayCompat {

    private static final int[] TEMP_ARRAY = new int[1];
    private static final ColorStateList[] DEFAULT_COLOR_STATE_LISTS = new ColorStateList[8];
    
    static boolean HAS_DEFAULT_COLOR_STATE_LISTS = false;
    
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
    
    static ColorStateList getColorStateList(Resources res, int attr, Resources.Theme theme) {
    
        if (Build.VERSION.SDK_INT >= 23)
            return res.getColorStateList(attr, theme);
        
        //noinspection deprecation
        return res.getColorStateList(attr);
    
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
        
        try {
        
            if (a != null)
                return a.getColorStateList(index);
        
        } catch (Exception ex) {
        
            String res = a.getString(index);
            
            if (res != null) {
            
                final int[] attrs = new int[] {
                    R.attr.colorPrimary,
                    R.attr.colorPrimaryDark,
                    R.attr.colorAccent,
                    R.attr.colorControlNormal,
                    R.attr.colorControlActivated,
                    R.attr.colorControlHighlight,
                    R.attr.colorButtonNormal,
                    R.attr.colorEdgeEffect
                };
                
                int resid = Integer.valueOf(res.substring(1));
                
                for (int i = 0; i < attrs.length; ++i)
                    if (attrs[i] == resid)
                        return DEFAULT_COLOR_STATE_LISTS[i];
            
            }
        
        }
        
        return null;
    
    }
    
    @SuppressWarnings("ResourceType")
    static void getDefaultColorStateLists(Context context) {
    
        if (Build.VERSION.SDK_INT >= 21) {
        
            final int[] attrs = new int[] {
                android.R.attr.colorPrimary,
                android.R.attr.colorPrimaryDark,
                android.R.attr.colorAccent,
                android.R.attr.colorControlNormal,
                android.R.attr.colorControlActivated,
                android.R.attr.colorControlHighlight,
                android.R.attr.colorButtonNormal,
                android.R.attr.colorEdgeEffect
            };
            
            TypedArray a = context.obtainStyledAttributes(attrs);
            
            try {
            
                DEFAULT_COLOR_STATE_LISTS[0] = a.getColorStateList(0);
                DEFAULT_COLOR_STATE_LISTS[1] = a.getColorStateList(1);
                DEFAULT_COLOR_STATE_LISTS[2] = a.getColorStateList(2);
                DEFAULT_COLOR_STATE_LISTS[3] = a.getColorStateList(3);
                DEFAULT_COLOR_STATE_LISTS[4] = a.getColorStateList(4);
                DEFAULT_COLOR_STATE_LISTS[5] = a.getColorStateList(5);
                DEFAULT_COLOR_STATE_LISTS[6] = a.getColorStateList(6);
                DEFAULT_COLOR_STATE_LISTS[7] = a.getColorStateList(7);
            
            } finally {
                a.recycle();
            }
        
        }
        
        TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.colorBackground });
        
        int themeColorBackground  = a.getColor(0, 0);
        
        a.recycle();
        
        // If the theme colorBackground is light, use our own light color, otherwise dark
        final float[] hsv = new float[3];
        Color.colorToHSV(themeColorBackground, hsv);
        
        final int[] attrs = new int[] {
            R.attr.colorPrimary,
            R.attr.colorPrimaryDark,
            R.attr.colorAccent,
            R.attr.colorControlNormal,
            R.attr.colorControlActivated,
            R.attr.colorControlHighlight,
            R.attr.colorButtonNormal,
            R.attr.colorEdgeEffect
        };
        
        Resources res = context.getResources();
        Resources.Theme theme = context.getTheme();
        
        for (int i = 0; i < attrs.length; ++i) {
        
            if (DEFAULT_COLOR_STATE_LISTS[i] == null) {
            
                if ((attrs[i] == R.attr.colorPrimary) || (attrs[i] == R.attr.colorEdgeEffect)) {
                
                    if (hsv[2] > 0.5f)
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.primary_material_light, theme);
                    else
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.primary_material_dark, theme);
                
                } else if (attrs[i] == R.attr.colorPrimaryDark) {
                
                    if (hsv[2] > 0.5f)
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.primary_dark_material_light, theme);
                    else
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.primary_dark_material_dark, theme);
                
                } else if ((attrs[i] == R.attr.colorAccent) || (attrs[i] == R.attr.colorControlActivated)) {
                
                    if (hsv[2] > 0.5f)
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.accent_material_light, theme);
                    else
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.accent_material_dark, theme);
                
                } else if (attrs[i] == R.attr.colorControlNormal) {
                
                    if (hsv[2] > 0.5f)
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.secondary_text_material_light, theme);
                    else
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.secondary_text_material_dark, theme);
                
                } else if (attrs[i] == R.attr.colorControlHighlight) {
                
                    if (hsv[2] > 0.5f)
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.ripple_material_light, theme);
                    else
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.ripple_material_dark, theme);
                
                } else if (attrs[i] == R.attr.colorButtonNormal) {
                
                    if (hsv[2] > 0.5f)
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.btn_default_material_light, theme);
                    else
                        DEFAULT_COLOR_STATE_LISTS[i] = getColorStateList(res, R.color.btn_default_material_dark, theme);
                
                }
            
            }
        
        }
        
        HAS_DEFAULT_COLOR_STATE_LISTS = true;
    
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