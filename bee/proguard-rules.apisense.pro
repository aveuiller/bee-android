# This file defines needed rules to obfuscate code using APISENSE

-keep public class com.apisense.sdk.** { *; }
-keepclassmembernames class com.apisense.sdk.** { *; }
-keep public class io.apisense.** { *; }

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

## Retrofit
-keep class com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-keepattributes Exceptions
-keepattributes Signature

-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-dontwarn okio.**
-dontwarn retrofit.**
-dontwarn rx.**

## Ormlite
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }

## Dagger
-keep class javax.inject.* { *; }

## Rhino
-keep class javax.script.** { *; }
-keep class com.sun.script.javascript.** { *; }
-keep class org.mozilla.javascript.** { *; }
-dontwarn org.mozilla.javascript.**
-dontwarn sun.**
