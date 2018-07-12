import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileOutputStream

group = "co.thecodewarrior.unifontcli"

dependencies {
    compile(project(":UnifontLib"))
    compile("com.github.ajalt", "clikt", "1.2.0")
    compile("commons-net", "commons-net", "3.6")
    compile("me.tongfei", "progressbar", "0.7.0")
}


plugins {
    application
}

application {
    mainClassName = "co.thecodewarrior.unifontcli.MainKt"
    applicationDefaultJvmArgs = listOf(
            "-Djava.awt.headless=true"
    )
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName="${project.name}-fat"
    manifest {
        attributes["Main-Class"] = "co.thecodewarrior.unifontcli.MainKt"
        attributes["Implementation-Title"] = "Unifont Utilities"
        attributes["Implementation-Version"] = version
    }
    from(configurations.runtime.map { if(it.isDirectory) it else zipTree(it) })
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
val runArgs: Any? by project
tasks.withType<JavaExec> {
    val runningDir = File("run/")
    runningDir.mkdirs()
    workingDir = runningDir
    val runArgs = runArgs
    if(runArgs is String && runArgs.isNotEmpty()) {
        args = runArgs.split("\uE000")
    }
}

tasks["build"].dependsOn(createExecutable)
