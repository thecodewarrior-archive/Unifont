pluginManagement({
    repositories {
        gradlePluginPortal()
        maven("https://kotlin.bintray.com/kotlinx")
    }
//    resolutionStrategy {
//        eachPlugin {
//            when (requested.id.id) {
//                "kotlinx-serialization"->{
//                    useModule("org.jetbrains.kotlinx:kotlinx-gradle-serialization-plugin:${requested.version}")
//                }
//            }
//        }
//    }
})


include("UnifontLib", "UnifontCLI")
