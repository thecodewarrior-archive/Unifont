import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileOutputStream

group = "co.thecodewarrior.unifontgui"

dependencies {
    compile(project(":UnifontLib"))
    compile(fileTree("libs") {
        this.includes.add("*.jar")
    })
    compile("commons-net", "commons-net", "3.6")
}


configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
