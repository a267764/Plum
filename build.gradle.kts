plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.12.3"
}

group = "com.sakurawald"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://maven.aliyun.com/repository/central")
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("net.sourceforge.htmlunit:htmlunit:2.50.0")

    // TODO: find and add silk4j
}