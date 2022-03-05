plugins {
    java
    id("io.freefair.lombok") version "6.1.0"
}

group = "cc.towerdefence.minestom.lobby"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal() // In maven local: openmatch.frontend
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.ZakShearman:Minestom:9c98fe0f23")
    implementation("cc.towerdefence.openmatch:frontend:1.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}