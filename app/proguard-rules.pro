# SuperShade ProGuard rules

# Shizuku & cross-process UserService bound by class name
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-keep class com.supershade.shizuku.ShadeCommanderService { *; }
-keep class com.supershade.shizuku.IShadeCommander* { *; }

# HiddenApiBypass
-keep class org.lsposed.hiddenapibypass.** { *; }

# Keep stack trace line numbers and Kotlin serialization metadata
-keepattributes *Annotation*, InnerClasses, SourceFile, LineNumberTable
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer {
    static **$$serializer INSTANCE;
}
