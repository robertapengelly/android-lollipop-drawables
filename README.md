# Android Lollipop Drawables

Backported material drawables for use on pre-lollipop devices. Supports Android 2.3 API 9 (GINGERBREAD) and up.

Preview

![lollipopdrawablesexample](https://cloud.githubusercontent.com/assets/5245027/21935050/ce4c0c1e-d9a3-11e6-9506-181e3190cae6.gif)

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
        compile 'com.github.robertapengelly:android-lollipop-drawables:1.0.0'
    }

# Usage

Ripple Drawable<br />
For more information about ripple drawables visit https://developer.android.com/reference/android/graphics/drawable/RippleDrawable.html

ProgressBar drawables

    Styling
    
    Without the Android Support Library add the following styles
    
    Pre-Honycomb devices
    
        <style name="AppTheme" parent="@android:style/Theme.NoTitleBar">
            <item name="colorAccent">@color/accent_material_dark</item> <!-- optional. you can set colorAccent to any other color. -->
            <item name="colorControlActivated">?attr/colorAccent</item> <!-- you can set colorControlActivated to any other color if you don't want to include colorAccent. -->
        </style>
    
    Honycomb and newer
    
        <style name="AppTheme" parent="@android:style/Theme.Holo.NoActionBar">
            <item name="colorAccent">@color/accent_material_dark</item> <!-- optional. you can set colorAccent to any other color. -->
            <item name="colorControlActivated">?attr/colorAccent</item> <!-- you can set colorControlActivated to any other color if you don't want to include colorAccent. -->
        </style>
    
    Starting with Lollipop there are already colorAccent and colorControlActivated defined. You can edit colorAccent or colorControlActivated to customize your views.
    
        <style name="AppTheme" parent="@android:style/Theme.Material.NoActionBar">
            <item name="android:colorAccent">@color/accent_material_dark</item> <!-- optional. you can set colorAccent to any other color. -->
            <item name="android:colorControlActivated">?attr/colorAccent</item> <!-- you can set colorControlActivated to any other color if you don't want to include colorAccent. -->
        </style>
