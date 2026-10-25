# ===================================================================
# Quran Radio World - ProGuard / R8 Optimization & Obfuscation Rules
# High-Efficiency Stripping, Code Optimization & Obfuscation
# ===================================================================

# 1. Stack Trace Preservation & Line Number Mapping
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# 2. Aggressive Log & Debug Stripping in Release Builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# 3. Strip Kotlin Runtime Assertions in Release Builds
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
    public static void checkNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
}

# 4. Android Core Components (Instantiated by Android OS via manifest)
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

# 6. AndroidX WorkManager (Targeted Worker reflection keeping; allows full R8 dead-code stripping)
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.example.workers.** {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.example.services.HaramLiveSyncWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.work.impl.WorkDatabase_Impl {
    public <init>();
    *;
}
-dontwarn androidx.work.impl.**

# 7. AndroidX Media3 / ExoPlayer (Audio & Video Streaming)
# Surgical rules: keep entry points and player classes, allow R8 to strip unused codecs/formats
-keep class androidx.media3.session.MediaSessionService { *; }
-keep class androidx.media3.session.MediaLibraryService { *; }
-keep class androidx.media3.session.MediaSessionService$* { *; }
-keep class androidx.media3.exoplayer.ExoPlayer { *; }
-dontwarn androidx.media3.**
-keep class android.media.MediaCodec** { *; }
-keep class com.example.player.** { *; }

# 8. Compose Runtime Stability Preservation
-keepclassmembers class * {
    @androidx.compose.runtime.Stable <fields>;
    @androidx.compose.runtime.Immutable <fields>;
}

# 9. Third-party Library Warnings Suppression
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.google.android.play.core.**
-dontwarn com.google.android.gms.**
-dontwarn androidx.datastore.**
-dontwarn coil.**
-dontwarn kotlinx.coroutines.**
