import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    id("com.ncorti.ktfmt.gradle") version "0.11.0"
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // TODO JTW separate artifacts for coroutines/rx support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

ktfmt {
    googleStyle() // 2-space indentation
}

java {
    // Publish Sources
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {

            groupId = "com.github.JeffWright"
            version = "0.5.1"
            artifactId = "logsugar"

            from(components["java"])
        }
    }
}
