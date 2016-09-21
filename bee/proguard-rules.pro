-keepattributes Exceptions,Signature,*Annotation*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * {
  public <init>(android.content.Context);
}

##############
# zbar specific
-keep class net.sourceforge.zbar.** { *; }
# End zbar
##############

##############
# apisense specific
-include proguard-rules.apisense.pro
# End apisense
##############

# Removing Verbose and Debug Logs
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
}
