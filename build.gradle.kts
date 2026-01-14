plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// ==========================================
// FIX FOR debugRuntimeClasspathCopy DEPRECATION
// ==========================================
// Configure all projects to properly handle configurations
allprojects {
    configurations.configureEach {
        // Prevent configurations from acting as both resolution root and variant
        // This fixes the "Configurations should not act as both a resolution root and a variant simultaneously" warning

        // Mark copy configurations as not consumable to fix deprecation warnings
        if (name.contains("RuntimeClasspathCopy", ignoreCase = true)) {
            isCanBeConsumed = false
            isCanBeResolved = true
        }

        // Ensure proper configuration role separation
        if (name.contains("Classpath")) {
            resolutionStrategy {
                // Force consistent versions across all modules
                failOnVersionConflict()
                preferProjectModules()
            }
        }
    }
}

// Additional configuration for subprojects
subprojects {
    // Apply configuration role fixes to all subprojects
    afterEvaluate {
        configurations.configureEach {
            // Mark internal AGP configurations as not consumable
            if (name.matches(Regex(".*RuntimeClasspathCopy.*"))) {
                isCanBeConsumed = false
                isCanBeResolved = true
            }
        }
    }
}
