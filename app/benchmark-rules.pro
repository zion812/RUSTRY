# Benchmark-specific ProGuard rules for Rooster Platform
# These rules are applied only to benchmark builds for performance testing

# Keep all benchmark-related classes
-keep class androidx.benchmark.** { *; }
-keep class androidx.test.** { *; }

# Keep performance monitoring classes for benchmarking
-keep class com.rio.rustry.utils.PerformanceMonitor { *; }
-keep class com.rio.rustry.utils.MemoryManager { *; }
-keep class com.rio.rustry.utils.DatabaseOptimizer { *; }

# Keep all ViewModels for UI benchmarking
-keep class com.rio.rustry.presentation.viewmodel.** { *; }

# Keep all Composables for UI performance testing
-keep class com.rio.rustry.presentation.screen.** { *; }

# Disable obfuscation for better benchmark profiling
-dontobfuscate

# Keep line numbers for performance profiling
-keepattributes SourceFile,LineNumberTable

# Keep method names for better profiling data
-keepnames class ** { *; }

# Don't optimize away benchmark code
-keep class **Benchmark { *; }
-keep class **BenchmarkTest { *; }

# Keep Hilt components for dependency injection benchmarking
-keep class **_HiltModules { *; }
-keep class **_HiltComponents { *; }

# Keep Room entities and DAOs for database benchmarking
-keep class com.rio.rustry.data.model.** { *; }
-keep class com.rio.rustry.data.local.dao.** { *; }

# Keep repository classes for data layer benchmarking
-keep class com.rio.rustry.data.repository.** { *; }

# Preserve annotations for reflection-based benchmarking
-keepattributes *Annotation*

# Keep enum values for benchmark configuration
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}