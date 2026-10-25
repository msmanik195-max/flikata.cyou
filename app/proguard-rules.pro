# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve Moshi JSON models while obfuscating everything else
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.JsonClass *;
}

# Obfuscate line numbers and source file names
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Strip Logcat debug and verbose logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
