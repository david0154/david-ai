// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
}

// ✅ ADDED: Deep clean task to fix build cache issues
tasks.register("deepClean", Delete::class) {
    delete(rootProject.buildDir)
    delete("$rootProject.projectDir/.gradle")
    delete("$rootProject.projectDir/build")
    
    // Clean all subproject build dirs
    subprojects.forEach { subproject ->
        delete(subproject.buildDir)
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