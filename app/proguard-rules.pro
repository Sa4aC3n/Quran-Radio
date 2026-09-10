# ===================================================================
# Quran Radio World - ProGuard / R8 Optimization & Obfuscation Rules
# Tailored specifically for actual libraries and code in this project
# ===================================================================

# 1. Stack Trace Preservation & Line Number Mapping
# Enables readable Crashlytics and Google Play Console stack traces while allowing full obfuscation
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# 2. R8 Optimization Settings
# Optimization and inlining are enabled by default in r8 / proguard-android-optimize.txt
# (Note: -repackageclasses is strictly avoided to prevent breaking Room & WorkManager reflection)

# 3. Strip Debug & Verbose Logs in Release Builds
# Eliminates Log.d, Log.v, Log.i bytecode and format strings from release binaries
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# 4. Android Core Components (Instantiated by Android OS via reflection/manifest)
-keep public class com.example.QuranRadioApplication {
    public <init>();
}
-keep public class com.example.MainActivity {
    public <init>();
}
-keep public class com.example.player.QuranMediaService {
    public <init>();
}
-keep public class com.example.services.AdhanPlaybackService {
    public <init>();
}
-keep public class com.example.services.AdhanAlarmReceiver {
    public <init>();
}
-keep public class com.example.services.IqamahAlarmReceiver {
    public <init>();
}
-keep public class com.example.services.HaramNotificationActionReceiver {
    public <init>();
}
-keep public class com.example.services.BootReceiver {
    public <init>();
}

# 5. AndroidX Startup (App Initializers)
-keep class androidx.startup.** { *; }
-keep class * extends androidx.startup.Initializer {
    public <init>();
}

# 6. AndroidX WorkManager & Room (Fixes NoSuchMethodException WorkDatabase_Impl.<init>)
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }
-keep class androidx.work.impl.WorkDatabase_Impl {
    public <init>();
    public <init>(...);
    *;
}
-keep class * extends androidx.work.impl.WorkDatabase {
    public <init>();
    *;
}
-keep class * extends androidx.room.RoomDatabase {
    public <init>();
    *;
}
-keep class androidx.room.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.example.workers.** {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.example.services.HaramLiveSyncWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.impl.**
-dontwarn androidx.room.paging.**

# 7. AndroidX Media3 / ExoPlayer (Background Radio, Live TV & Audio Playback)
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep class android.media.MediaCodec** { *; }
-keep class com.example.player.** { *; }

# 7. OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**

# 8. Google Play In-App Updates & Play Services
-dontwarn com.google.android.play.core.**
-dontwarn com.google.android.gms.**

# 9. AndroidX DataStore & Preferences
-dontwarn androidx.datastore.**

# 10. Coil Image Loading
-dontwarn coil.**

# 11. KotlinX Coroutines
-dontwarn kotlinx.coroutines.**
