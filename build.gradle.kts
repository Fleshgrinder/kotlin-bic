import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    idea
    `maven-publish`
    kotlin("jvm") version "1.3.40"
    id("org.jetbrains.dokka") version "0.9.18"
}

description = "ISO 9362:2014 Business Identifier Codes"
group = "com.fleshgrinder.kotlin"
version = "0.1.0"

repositories { jcenter() }

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib") {
        version { prefer("latest.release") }
    }

    val junitBom = platform("org.junit:junit-bom:5.4.2")
    testImplementation(junitBom)
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly(junitBom)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.apply {
    wrapper { distributionType = Wrapper.DistributionType.ALL }

    idea {
        module {
            isDownloadJavadoc = false
            isDownloadSources = System.getenv("CI") != null
        }
    }

    compileKotlin {
        kotlinOptions {
            allWarningsAsErrors = true
        }
    }

    compileTestKotlin {
        kotlinOptions {
            allWarningsAsErrors = true
            jvmTarget = "1.8"
        }
    }

    test {
        useJUnitPlatform()
        testLogging { events("passed", "skipped", "failed") }
    }

    withType<DokkaTask>().configureEach {
        dependsOn("clean${name.capitalize()}")
        samples = listOf("$rootDir/src/test/kotlin")
    }

    dokka {
        includes = listOf("PACKAGE.md", "README.md")
        outputDirectory = "$buildDir/docs"
        outputFormat = "html"
    }

    val docs by registering(Copy::class) {
        val dokka = dokka.get()
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Copies the Dokka generated documentation into the `/docs` directory for GitHub publishing."
        dependsOn("cleanDocs", dokka)

        val hljs = """./style.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@9.13.1/build/styles/atom-one-dark.min.css">
<script src="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@9.13.1/build/highlight.min.js"></script>
<script src="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@9.13.1/build/languages/kotlin.min.js"></script>
<script>hljs.initHighlightingOnLoad();</script>"""

        from("${dokka.outputDirectory}/style.css")
        from("${dokka.outputDirectory}/${dokka.moduleName}") {
            exclude("index-outline.html", "package-list")
            filter { it.replace("../style.css\">", hljs)}
        }
        into("$rootDir/docs")
    }

    // region ------------------------------------------------------------------ publishing

    withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    remove(javadoc)
    val javadoc by registering(DokkaTask::class) {
        includes = listOf("PACKAGE.md")
        outputDirectory = "$buildDir/javadoc"
        outputFormat = "javadoc"
    }

    val javadocJar by registering(Jar::class) {
        group = "build"
        description = "Assembles a jar archive containing the source documentation."
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    val sourcesJar by registering(Jar::class) {
        group = "build"
        description = "Assembles a jar archive containing the source files."
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allJava)
    }

    publishing {
        repositories {
            val user = cfgOrNull("com.bintray.user")
            val key = cfgOrNull("com.bintray.key")

            if (user == null && key == null) {
                maven(uri("$buildDir/local-maven-repository"))
            } else {
                maven(uri("https://api.bintray.com/maven/$user/$group/$name/;publish=1")) {
                    name = "bintray"
                    credentials {
                        username = user
                        password = key
                    }
                }
            }
        }

        publications {
            register("mavenJava", MavenPublication::class) {
                from(project.components["java"])
                artifact(sourcesJar.get())
                artifact(javadocJar.get())
            }
        }
    }

    // endregion --------------------------------------------------------------- publishing
}

// region ---------------------------------------------------------------------- utils

fun env(name: String): String? = System.getenv(name.replace('.', '_').toUpperCase())
fun cfg(name: String) = env(name) ?: project.property(name) as String
fun cfgOrNull(name: String) = env(name) ?: project.findProperty(name) as String?

// endregion ------------------------------------------------------------------- utils
