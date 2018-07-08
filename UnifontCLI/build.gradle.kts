import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileOutputStream

group = "co.thecodewarrior.unifontcli"

dependencies {
    compile(project(":UnifontLib"))
    compile("com.github.ajalt", "clikt", "1.2.0")
    compile("commons-net", "commons-net", "3.6")
}


plugins {
    application
}

application {
    mainClassName = "co.thecodewarrior.unifoncli.MainKt"
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName="${project.name}-fat"
    manifest {
        attributes["Main-Class"] = "co.thecodewarrior.unifontcli.MainKt"
    }
    from(configurations.runtime.map({ if(it.isDirectory) it else zipTree(it) }))
    with(tasks["jar"] as CopySpec)
}

val createExecutable = task("createExecutable") {
    dependsOn(fatJar)
    val input = fatJar.outputs.files.singleFile
    val output = project.buildDir.resolve("libs/${project.name}-${project.version}")
    doLast {
        FileOutputStream(output).use { outputStream ->
            exec {
                standardOutput = outputStream
                commandLine("cat", "src/launcher-stub.sh", input.absolutePath)
            }
        }
        exec {
            commandLine("chmod", "+x", output.absolutePath)
        }
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks["build"].dependsOn(createExecutable)
