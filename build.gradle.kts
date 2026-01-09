plugins {
    id("com.android.application") version "7.4.2" apply false
    kotlin("android") version "1.8.22" apply false
    kotlin("plugin.serialization") version "1.8.22" apply false
    kotlin("kapt") version "1.8.22" apply false
    id("com.google.dagger.hilt.android") version "2.46" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
