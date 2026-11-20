# Keep model classes
-keep class com.uragon.tv.MediaItem { *; }
-keep class com.uragon.tv.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }

# Media3
-keep class androidx.media3.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
