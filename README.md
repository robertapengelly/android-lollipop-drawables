# Android Lollipop Drawables

Backported material drawables for use on pre-lollipop devices. Supports Android 2.3 API 9 (GINGERBREAD) and up.

Preview

![lollipopdrawablesexample](https://cloud.githubusercontent.com/assets/5245027/22297182/73ea5eea-e314-11e6-84e1-f1187133d636.gif)

# Installation

    Step 1. Add the JitPack repository to your build file
    
    Add it in your root build.gradle at the end of repositories:
    
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
         }
    }
    
    Step 2. Add the dependency
    
    dependencies {
        compile 'com.github.robertapengelly:android-lollipop-drawables:1.0.6'
    }

# Usage

AnimatedStateListDrawable

    Defining your AnimatedStateListDrawable (drawable/animated_state_list_drawable_example.xml)
    
        <animated-selector xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:app="http://schemas.android.com/apk/res-auto">
            
            <item android:id="@+id/pressed"
                android:state_pressed="true">
                
                <shape android:shape="rectangle">
                    <corners android:radius="5dp" />
                    <solid android:color="#ffff00ff" />
                    <stroke android:color="#bcbcbc" android:width="1dp" />
                </shape>
            
            </item>
            
            <item android:id="@+id/normal">
            
                <shape android:shape="rectangle">
                    <corners android:radius="5dp" />
                    <solid android:color="#ffff00ff" />
                    <stroke android:color="#bcbcbc" android:width="1dp" />
                </shape>
            
            </item>
            
            <transition app:fromId="@id/normal" app:toId="@id/pressed">
                <animation-list>
                    <item android:duration="15">
                        <shape android:shape="rectangle">
                            <corners android:radius="5dp" />
                            <solid android:color="#40ff00ff" />
                            <stroke android:color="#bcbcbc" android:width="1dp" />
                        </shape>
                    </item>
                    <item android:duration="15">
                        <shape android:shape="rectangle">
                            <corners android:radius="5dp" />
                            <solid android:color="#80ff00ff" />
                            <stroke android:color="#bcbcbc" android:width="1dp" />
                        </shape>
                    </item>
                    <item android:duration="15">
                        <shape android:shape="rectangle">
                            <corners android:radius="5dp" />
                            <solid android:color="#bfff00ff" />
                            <stroke android:color="#bcbcbc" android:width="1dp" />
                        </shape>
                    </item>
                    <item android:duration="15">
                        <shape android:shape="rectangle">
                            <corners android:radius="5dp" />
                            <solid android:color="#ffff00ff" />
                            <stroke android:color="#bcbcbc" android:width="1dp" />
                        </shape>
                    </item>
                </animation-list>
            </transition>
        
        </animated-selector>
    
    Applying drawable to view
    
        import robertapengelly.support.graphics.drawable.LollipopDrawablesCompat;
        
        View view = findViewById(R.id.your_view);
        view.setBackgroundDrawable(LollipopDrawablesCompat.getDrawable(getResources(), R.drawable.animated_state_list_drawable_example, getTheme()));

Ripple Drawable<br />
For more information about ripple drawables visit https://developer.android.com/reference/android/graphics/drawable/RippleDrawable.html

    Styling
    
    Pre-Honycomb devices (values/styles.xml)
    
        <style name="AppTheme" parent="@android:style/Theme.NoTitleBar">
            <item name="colorControlHighlight">?attr/colorAccent</item> <!-- you can set colorControlHighlight to any other color. -->
        </style>
    
    Honycomb and newer (values-v11/styles.xml)
    
        <style name="AppTheme" parent="@android:style/Theme.Holo.NoActionBar">
            <item name="colorControlHighlight">?attr/colorAccent</item> <!-- you can set colorControlHighlight to any other color. -->
        </style>
    
    Starting with Lollipop colorControlHighligh is already defined. You can edit colorControlHighlight to customize your views further. (values-v21/styles.xml)
    
        <style name="AppTheme" parent="@android:style/Theme.Material.NoActionBar">
            <item name="android:colorControlHighlight">@color/colorControlHighlight</item> <!-- you can set colorControlHighlight to any other color. -->
        </style>
    
    Defining your ripple drawales in xml
    
    Pre-Lollipop (drawable/ripple.xml)
    
        <?xml version="1.0" encoding="utf-8" ?>
        <ripple xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:tools="http://schemas.android.com/tools"
            android:color="?attr/colorControlHighlight"
            tools:targetApi="lollipop">
            
            <item android:drawable="@android:color/white" android:id="@android:id/mask" />
        
        </ripple>
    
    Lollipop and newer (drawable-v21/ripple.xml)
    
        <?xml version="1.0" encoding="utf-8" ?>
        <ripple xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:tools="http://schemas.android.com/tools"
            android:color="?android:attr/colorControlHighlight">
            
            <item android:drawable="@android:color/white" android:id="@android:id/mask" />
        
        </ripple>
    
    Applying drawable to view
    
        import robertapengelly.support.graphics.drawable.LollipopDrawable;
        import robertapengelly.support.graphics.drawable.LollipopDrawablesCompat;
        import robertapengelly.support.view.DrawableHotspotTouch;
        
        View view = findViewById(R.id.your_view);
        view.setBackgroundDrawable(LollipopDrawablesCompat.getDrawable(getResources(), R.drawable.ripple, getTheme()));
        view.setClickable(true);
        view.setOnTouchListener(new DrawableHotspotTouch((LollipopDrawable) view.getBackground()));

ProgressBar drawables

    Styling
    
    Pre-Honycomb devices (values/styles.xml)
    
        <style name="AppTheme" parent="@android:style/Theme.NoTitleBar">
            <item name="colorAccent">@color/accent_material_dark</item> <!-- optional. you can set colorAccent to any other color. -->
            <item name="colorControlActivated">?attr/colorAccent</item> <!-- you can set colorControlActivated to any other color if you don't want to include colorAccent. -->
        </style>
    
    Honycomb and newer (values-v11/styles.xml)
    
        <style name="AppTheme" parent="@android:style/Theme.Holo.NoActionBar">
            <item name="colorAccent">@color/accent_material_dark</item> <!-- optional. you can set colorAccent to any other color. -->
            <item name="colorControlActivated">?attr/colorAccent</item> <!-- you can set colorControlActivated to any other color if you don't want to include colorAccent. -->
        </style>
    
    Starting with Lollipop colorAccent and colorControlActivated are already defined. You can edit colorAccent or colorControlActivated to customize your views further. (values-v21/styles.xml)
    
        <style name="AppTheme" parent="@android:style/Theme.Material.NoActionBar">
            <item name="android:colorAccent">@color/accent_material_dark</item> <!-- optional. you can set colorAccent to any other color. -->
            <item name="android:colorControlActivated">@color/controlColorActivated</item> <!-- optional. colorControlActivated already has a value of ?android:attr/colorAccent so you can set colorControlActivated to any other color. -->
        </style>
    
    Adding a ProgressBar widget (layout/activity_main.xml)
    
    CircularProgressBar
    
        <?xml version="1.0" encoding="utf-8" ?>
        <LinearLayout
            xmlns:android="http://schemas.android.com/apk/res/android"
            android:layout_height="match_parent"
            android:layout_width="match_parent"
            android:orientation="vertical">
            
                <ProgressBar
                    android:id="@+id/progressbar"
                    android:indeterminate="true"
                    android:layout_height="wrap_content"
                    android:layout_width="wrap_content" />
        
        </LinearLayout>
    
    HorizontalProgressBar
    
        <?xml version="1.0" encoding="utf-8" ?>
        <LinearLayout
            xmlns:android="http://schemas.android.com/apk/res/android"
            android:layout_height="match_parent"
            android:layout_width="match_parent"
            android:orientation="vertical">
            
                <ProgressBar
                    android:id="@+id/progressbar"
                    android:indeterminate="true"
                    android:layout_height="wrap_content"
                    android:layout_width="match_parent"
                    style="?android:attr/progressBarStyleHorizontal" />
        
        </LinearLayout>
    
    Applying drawable to ProgressBar widget
    
        import robertapengelly.support.graphics.drawable.HorizontalProgressDrawable; // set android:indeterminate="false" in layout file
        
        HorizontalProgressDrawable d = new HorizontalProgressDrawable(this); 
        
        import robertapengelly.support.graphics.drawable.IndeterminateCircularProgressDrawable;
        
        IndeterminateCircularProgressDrawable d = new IndeterminateCircularProgressDrawable(this);
        
        import robertapengelly.support.graphics.drawable.IndeterminateHorizontalProgressDrawable;
        
        IndeterminateHorizontalProgressDrawable d = new IndeterminateHorizontalProgressDrawable(this);
        
        import android.widget.ProgressBar;
        
        ProgressBar progressbar = (ProgressBar) findViewById(R.id.progressbar);
        progressbar.setIndeterminateDrawable(d);
        progressbar.setProgressDrawable(d);
