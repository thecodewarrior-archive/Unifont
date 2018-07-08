plugins {
    id("kotlinx-serialization") version "0.5.1" apply true
}

val serialization_version = "0.5.1"

dependencies {
    compile("org.jetbrains.kotlinx", "kotlinx-serialization-runtime",  serialization_version)
}

group = "co.thecodewarrior.unifontlib"
