# 代码混淆压缩比，在0~7之间
-optimizationpasses 5

#google推荐算法
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
# 避免混淆Annotation、内部类、泛型、匿名类
-keepattributes *Annotation*,InnerClasses,Signature,EnclosingMethod,Exceptions
# 重命名抛出异常时的文件名称
-renamesourcefileattribute Source
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# 保持自定义控件类不被混淆
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public <init>(android.content.Context, android.util.AttributeSet, int, int);
    public void set*(...);
    *** get*();
}

# 保留R下面的资源
-keepclassmembers class **.R$* {
    public static <fields>;
}
# 保留四大组件，自定义的Application等这些类不被混淆
# -keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}
# 保留本地native方法不被混淆
-keepclasseswithmembers class * {
    native <methods>;
}

# 保留Parcelable序列化类不被混淆
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class * implements java.io.Serializable {
   static final long serialVersionUID;
   private static final java.io.ObjectStreamField[]   serialPersistentFields;
   private void writeObject(java.io.ObjectOutputStream);
   private void readObject(java.io.ObjectInputStream);
   java.lang.Object writeReplace();
   java.lang.Object readResolve();
}

#assume no side effects:删除android.util.Log输出的日志
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# Keep setting classes for SettingsSaver
-keep class com.huanchengfly.tieba.post.ui.models.settings.* { *; }

# Keep navigation destination
-keep public class * extends com.huanchengfly.tieba.post.ui.page.Destination

# WebView
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
}

-keep class com.huanchengfly.tieba.post.models.** { *; }
-keep class com.huanchengfly.tieba.post.api.models.** { *; }

-keep class * implements com.google.gson.JsonDeserializer {
    *;
}

# uCrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

# kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-keep,allowobfuscation,allowshrinking class com.huanchengfly.tieba.post.api.retrofit.ApiResult
-keep,allowobfuscation,allowshrinking class * extends com.huanchengfly.tieba.post.api.retrofit.ApiResult

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-keepclassmembers,allowobfuscation class * {
 @com.google.gson.annotations.SerializedName <fields>;
}

# 荣耀随心握 SDK：它引用的 HWExtDeviceManager / SystemPropertiesEx / HwFoldScreenManagerEx
# 都是荣耀框架里的隐藏类，标准 android.jar 没有，R8 会报 Missing class；
# 真机由系统提供，非荣耀机型根本走不到那条分支
-dontwarn com.hihonor.android.**
# 只 dontwarn 会留下坑：R8 不认得那条继承链，会把 SDK 里事件监听父类的方法一起改名，
# 框架按原名派发 → 正式版 AbstractMethodError。框架回调全凭方法名匹配，整包 keep。
-keep class com.hihonor.smartgripkit.** { *; }
-keep class * extends com.hihonor.smartgripkit.SmartGripEventListener { *; }
