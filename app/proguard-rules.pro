# Add project specific ProGuard rules here.
# You can control the set of inputs and library keep options here.
# The file is based upon the official ProGuard documentation.

# Optimizations can be turned on at the project level
# These may incrementally improve performance.
# Note: Do not use more than one optimization pass.
# -optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
# -optimizationpasses 5

# Preserve Google Maps SDK classes
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }

# Preserve classes that are serialized
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
