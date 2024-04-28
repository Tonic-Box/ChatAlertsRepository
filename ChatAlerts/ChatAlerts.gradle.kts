import org.gradle.kotlin.dsl.dependencies

//Major Release - Feature Addition - Bug Fix
version = "1.1.0"

plugins {
    id("java")
}

project.extra["PluginName"] = "Chat Alerts"
project.extra["PluginDescription"] = "Discord OSRS Account Monitor"

dependencies {
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.20")
    annotationProcessor(group = "org.pf4j", name = "pf4j", version = "3.6.0")
}



tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) })

        manifest {
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                "Plugin-Provider" to project.extra["PluginProvider"],
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}

tasks.named<Jar>("jar") {
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    archiveBaseName.set(project.extra["PluginName"].toString())
    archiveVersion.set(version)
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}