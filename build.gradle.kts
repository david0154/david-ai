plugins {
    id("com.android.application") version "8.1.0" apply false
    kotlin("android") version "1.9.0" apply false
    kotlin("kapt") version "1.9.0" apply false
    id("dagger.hilt.android.plugin") version "2.46" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
