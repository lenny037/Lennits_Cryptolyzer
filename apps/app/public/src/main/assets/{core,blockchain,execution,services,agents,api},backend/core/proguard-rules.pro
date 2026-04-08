# LENNITS_CRYPTOLYZER ProGuard Rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class com.lennit.cryptolyzer.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class com.google.gson.** { *; }
-dontwarn javax.annotation.**
