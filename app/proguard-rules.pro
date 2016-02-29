# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Include external jars
#-injars      libs
#-libraryjars libs/android-support-v4.jar

# Skip on Android to save time
#inherited -dontpreverify

# Use 5 step of optimization
#skip according to default file comment -optimizationpasses 5

# When not preverifing in a case-insensitive filing system, such as Windows. This tool will unpack your processed jars (if using windows you should then use).
#inherited -dontusemixedcaseclassnames

# Hold onto the mapping.text file, it can be used to unobfuscate stack traces in the developer console using the retrace tool
-printmapping mapping.txt

# Keep line numbers so they appear in the stack trace of the develeper console
#-keepattributes SourceFile,LineNumberTable
-keepattributes LineNumberTable

# The -optimizations option disables some arithmetic simplifications that Dalvik 1.0 and 1.5 can't handle.
#skip according to default file comment -optimizations !code/simplification/arithmetic

# Activities, services and broadcast receivers are specified in the manifest file so they won't be automatically included
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View
-keep public class * extends android.preference.Preference

# Custom view components might be accessed from your layout files
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
#inherited    public void set*(...);
}

# Event handlers can be specified in the layout files e.g. android:onClick="nextButton_onClick", I borrowed this method name notation from .NET
#inherited -keepclassmembers class * extends android.app.Activity {
#    public void *_*(android.view.View);
#}

# Parcelable implementations are accessed by introspection
#inherited -keepclassmembers class * implements android.os.Parcelable {
#    static android.os.Parcelable$Creator CREATOR;
#}

# Uncomment if using Serializable
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# You might want to keep your annotations
#inherited -keepattributes *Annotation*

# Don't parse support library (some of them inherited)
#-keep class android.support.** { *; }
#-keep interface android.support.** { *; }
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }

# Keep httpclientandroidlib not obfuscated
#-keep class ch.boye.httpclientandroidlib.** { *; }
#-keep interface ch.boye.httpclientandroidlib.** { *; }

# Make sure that Google Analytics doesn't get removed
-keep class com.google.analytics.** { *; }
-keep class com.google.android.** { *; }
-keep class com.google.tagmanager.** { *; }

#inherited -keepattributes *Annotation*


#ACRA specifics
# Restore some Source file names and restore approximate line numbers in the stack traces,
# otherwise the stack traces are pretty useless
-keepattributes SourceFile,LineNumberTable

# ACRA needs "annotations" so add this...
# Note: This may already be defined in the default "proguard-android-optimize.txt"
# file in the SDK. If it is, then you don't need to duplicate it. See your
# "project.properties" file to get the path to the default "proguard-android-optimize.txt".
#-keepattributes *Annotation*

# keep this class so that logging will show 'ACRA' and not a obfuscated name like 'a'.
# Note: if you are removing log messages elsewhere in this file then this isn't necessary
-keep class org.acra.ACRA {
    *;
}

# keep this around for some enums that ACRA needs
-keep class org.acra.ReportingInteractionMode {
    *;
}

-keepnames class org.acra.sender.HttpSender$** {
    *;
}

-keepnames class org.acra.ReportField {
    *;
}

# keep this otherwise it is removed by ProGuard
-keep public class org.acra.ErrorReporter
{
    public void addCustomData(java.lang.String,java.lang.String);
    public void putCustomData(java.lang.String,java.lang.String);
    public void removeCustomData(java.lang.String);
}

# keep this otherwise it is removed by ProGuard
-keep public class org.acra.ErrorReporter
{
    public void handleSilentException(java.lang.Throwable);
}

# EventBus
-keepclassmembers class ** {
    public void onEvent*(**);
}

# Only required if you use AsyncExecutor
#keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
#    (java.lang.Throwable);
#}
#
