# Proguard rules for Minuteman
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
