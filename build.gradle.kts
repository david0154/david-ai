// Top-level build file where you can add configuration options common to all sub-projects/modules.
// ✅ Updated to Gradle 8.5 compatible versions for ZERO WARNINGS
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50.1" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}

// ✅ FIXED: Deep clean task using layout.buildDirectory instead of buildDir
tasks.register("deepClean", Delete::class) {
    delete(layout.buildDirectory.asFile.get())
    delete("$rootDir/.gradle")
    
    // Clean all subproject build dirs
    subprojects.forEach { subproject ->
        delete(subproject.layout.buildDirectory.asFile.get())
        delete("${subproject.projectDir}/build")
        delete("${subproject.projectDir}/.gradle")
        
        // Clean Kotlin compile caches
        delete("${subproject.projectDir}/build/kotlin")
        delete("${subproject.projectDir}/build/tmp/kotlin-classes")
    }
    
    doLast {
        println("✅ Deep clean complete! Now run: ./gradlew build")
        println("This fixes:")
        println("  - VoiceDownloadManager redeclaration phantom errors")
        println("  - Stale build cache issues")
        println("  - Kotlin compile conflicts")
    }
}
