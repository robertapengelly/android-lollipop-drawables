package robertapengelly.support.graphics.drawable;

import  android.content.res.Resources;
import  android.content.res.TypedArray;
import  android.content.res.XmlResourceParser;
import  android.graphics.BitmapFactory;
import  android.graphics.drawable.ColorDrawable;
import  android.graphics.drawable.Drawable;
import  android.os.Build;
import  android.util.AttributeSet;
import  android.util.Log;
import  android.util.TypedValue;
import  android.util.Xml;

import  java.io.IOException;
import  java.io.InputStream;
import  java.lang.ref.WeakReference;
import  java.util.HashMap;
import  java.util.Map;

import  org.xmlpull.v1.XmlPullParser;
import  org.xmlpull.v1.XmlPullParserException;

public class LollipopDrawablesCompat {

    private static final Map<String, Class<? extends Drawable>> CLASS_MAP = new HashMap<>();
    private static final Object mAccessLock = new Object();
    
    private static final LongSparseArray<WeakReference<Drawable.ConstantState>> sColorDrawableCache = new LongSparseArray<>();
    private static final LongSparseArray<WeakReference<Drawable.ConstantState>> sDrawableCache = new LongSparseArray<>();
    
    static {
        registerDrawable(GradientDrawable.class, "shape");
        registerDrawable(InsetDrawable.class, "inset");
        registerDrawable(RippleDrawable.class, "ripple");
    }
    
    /** Applies the specified theme to this Drawable and its children. */
    public static void applyTheme(Drawable d, Resources.Theme t) {
    
        if (Build.VERSION.SDK_INT >= 21)
            d.applyTheme(t);
        else if (d instanceof LollipopDrawable)
            //noinspection RedundantCast
            ((LollipopDrawable) d).applyTheme(t);
    
    }
    
    private static void cacheDrawable(long key, Drawable drawable, LongSparseArray<WeakReference<Drawable.ConstantState>> caches) {
    
        Drawable.ConstantState cs = drawable.getConstantState();
        
        if (cs == null)
            return;
        
        synchronized (mAccessLock) {
            caches.put(key, new WeakReference<>(cs));
        }
    
    }
    
    public static boolean canApplyTheme(Drawable d) {
    
        if (Build.VERSION.SDK_INT >= 21)
            return d.canApplyTheme();
        else if (d instanceof LollipopDrawable)
            //noinspection RedundantCast
            ((LollipopDrawable) d).canApplyTheme();
        
        return false;
    
    }
    
    /** Create a drawable from file path name. */
    public static Drawable createFromPath(String pathName) {
        return Drawable.createFromPath(pathName);
    }
    
    /**
     * Create a drawable from an inputstream, using the given resources and
     * value to determine density information.
     */
    public static Drawable createFromResourceStream(Resources res, TypedValue value, InputStream is, String srcName) {
        return createFromResourceStream(res, value, is, srcName, null);
    }
    
    /**
     * Create a drawable from an inputstream, using the given resources and
     * value to determine density information.
     */
    public static Drawable createFromResourceStream(Resources res, TypedValue value, InputStream is, String srcName,
        BitmapFactory.Options opts) {
        return Drawable.createFromResourceStream(res, value, is, srcName, opts);
    }
    
    /** Create a drawable from an inputstream. */
    public static Drawable createFromStream(InputStream is, String srcName) {
        return createFromResourceStream(null, null, is, srcName);
    }
    
    /**
     * Create a drawable from an XML document. For more information on how to
     * create resources in XML, see
     * <a href="{@docRoot}guide/topics/resources/drawable-resource.html">Drawable Resources</a>.
     */
    public static Drawable createFromXml(Resources r, XmlPullParser parser) throws XmlPullParserException, IOException {
        return createFromXml(r, parser, null);
    }
    
    /**
     * Create a drawable from an XML document using an optional {@link Resources.Theme}.
     * For more information on how to create resources in XML, see
     * <a href="{@docRoot}guide/topics/resources/drawable-resource.html">Drawable Resources</a>.
     */
    public static Drawable createFromXml(Resources r, XmlPullParser parser, Resources.Theme theme)
        throws XmlPullParserException, IOException {
        
        AttributeSet attrs = Xml.asAttributeSet(parser);
        
        int type;
        
        //noinspection StatementWithEmptyBody
        while (((type = parser.next()) != XmlPullParser.START_TAG) && (type != XmlPullParser.END_DOCUMENT));
        
        if (type != XmlPullParser.START_TAG)
            throw new XmlPullParserException("No start tag found");
        
        Drawable drawable = createFromXmlInner(r, parser, attrs, theme);
        
        if (drawable == null)
            throw new RuntimeException("Unknown initial tag: " + parser.getName());
        
        return drawable;
    
    }
    
    /**
     * Create a drawable from inside an XML document using an optional
     * {@link Resources.Theme}. Called on a parser positioned at a tag in an XML
     * document, tries to create a Drawable from that tag. Returns {@code null}
     * if the tag is not a valid drawable.
     */
    public static Drawable createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs,
        Resources.Theme theme) throws XmlPullParserException, IOException {
        
        Drawable drawable = null;
        final String name = parser.getName();
        
        try {
        
            Class<? extends Drawable> clazz = CLASS_MAP.get(name);
            
            if (clazz != null)
                drawable = clazz.newInstance();
            else if (name.indexOf('.') > 0)
                drawable = (Drawable) Class.forName(name).newInstance();
        
        } catch (Exception e) {
            throw new XmlPullParserException("Error while inflating drawable resource", parser, e);
        }
        
        if (drawable == null) {
        
            if (Build.VERSION.SDK_INT >= 21)
                return Drawable.createFromXmlInner(r, parser, attrs, theme);
            
            return Drawable.createFromXmlInner(r, parser, attrs);
        
        }
        
        if (Build.VERSION.SDK_INT >= 21)
            drawable.inflate(r, parser, attrs, theme);
        else if (drawable instanceof LollipopDrawable)
            //noinspection RedundantCast
            ((LollipopDrawable) drawable).inflate(r, parser, attrs, theme);
        else
            drawable.inflate(r, parser, attrs);
        
        return drawable;
    
    }
    
    private static Drawable getCachedDrawable(LongSparseArray<WeakReference<Drawable.ConstantState>> cache,
        long key, Resources res) {
        
        synchronized (mAccessLock) {
        
            WeakReference<Drawable.ConstantState> wr = cache.get(key);
            
            if (wr != null) {
            
                Drawable.ConstantState entry = wr.get();
                
                if (entry != null)
                    return entry.newDrawable(res);
                else
                    cache.delete(key);
            
            }
        
        }
        
        return null;
    
    }
    
    public static Drawable getDrawable(Resources res, int resid) {
        return getDrawable(res, resid, null);
    }
    
    public static Drawable getDrawable(TypedArray array, int index) {
        return getDrawable(array, index, null);
    }
    
    public static Drawable getDrawable(Resources res, int resid, Resources.Theme theme) {
    
        TypedValue value = new TypedValue();
        res.getValue(resid, value, true);
        
        return loadDrawable(res, value, theme);
    
    }
    
    public static Drawable getDrawable(TypedArray array, int index, Resources.Theme theme) {
    
        TypedValue value = new TypedValue();
        array.getValue(index, value);
        
        return loadDrawable(array.getResources(), value, theme);
    
    }
    
    public static Drawable loadDrawable(Resources res, TypedValue value, Resources.Theme theme)
        throws Resources.NotFoundException {
        
        if ((value == null) || (value.resourceId == 0))
            return null;
        
        final LongSparseArray<WeakReference<Drawable.ConstantState>> cache;
        final boolean isColorDrawable;
        final long key;
        
        if ((value.type >= TypedValue.TYPE_FIRST_COLOR_INT) && (value.type <= TypedValue.TYPE_LAST_COLOR_INT)) {
        
            cache = sColorDrawableCache;
            isColorDrawable = true;
            key = value.data;
        
        } else {
        
            cache = sDrawableCache;
            isColorDrawable = false;
            key = (long) value.assetCookie << 32 | value.data;
        
        }
        
        Drawable dr = getCachedDrawable(cache, key, res);
        
        if (dr != null)
            return dr;
        
        if (isColorDrawable)
            dr = new ColorDrawable(value.data);
        else
            dr = loadDrawableForCookie(value, value.resourceId, res, theme);
        
        if (dr != null) {
        
            dr.setChangingConfigurations(value.changingConfigurations);
            cacheDrawable(key, dr, cache);
        
        }
        
        return dr;
    
    }
    
    private static Drawable loadDrawableForCookie(TypedValue value, int id, Resources res, Resources.Theme theme) {
    
        if (value.string == null)
            throw new Resources.NotFoundException("Resource \"" + res.getResourceName(id) + "\" (" +
                Integer.toHexString(id) + ")  is not a Drawable (color or path): " + value);
        
        String file = value.string.toString();
        
        final Drawable dr;
        
        if (file.endsWith(".xml")) {
        
            try {
            
                XmlResourceParser rp = res.getAssets().openXmlResourceParser(value.assetCookie, file);
                dr = LollipopDrawablesCompat.createFromXml(res, rp, theme);
                
                rp.close();
            
            } catch (Exception e) {
            
                Log.w(LollipopDrawablesCompat.class.getSimpleName(), "Failed to load drawable resource, " +
                    "using a fallback...", e);
                
                if (Build.VERSION.SDK_INT >= 21)
                    return res.getDrawable(value.resourceId, theme);
                
                //noinspection deprecation
                return res.getDrawable(value.resourceId);
            
            }
        
        } else {
        
            try {
            
                InputStream is = res.getAssets().openNonAssetFd(value.assetCookie, file).createInputStream();
                dr = LollipopDrawablesCompat.createFromResourceStream(res, value, is, file, null);
                
                is.close();
            
            } catch (Exception e) {
            
                Log.w(LollipopDrawablesCompat.class.getSimpleName(), "Failed to load drawable resource, " +
                    "using a fallback...", e);
                
                if (Build.VERSION.SDK_INT >= 21)
                    return res.getDrawable(value.resourceId, theme);
                
                //noinspection deprecation
                return res.getDrawable(value.resourceId);
            
            }
        
        }
        
        return dr;
    
    }
    
    public static void registerDrawable(Class<? extends Drawable> clazz, String name) {
    
        if ((name == null) || (clazz == null))
            throw new NullPointerException("Class: " + clazz + ". Name: " + name);
        
        CLASS_MAP.put(name, clazz);
    
    }
    
    public static void unregisterDrawable(String name) {
        CLASS_MAP.remove(name);
    }

}