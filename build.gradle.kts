plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta10"
    id("io.freefair.lombok") version "8.10.2" apply true
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "org.icarus"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// 统一所有 JVM 进程的编码，解决 Windows 中文乱码
tasks.withType<JavaExec> {
    jvmArgs("-Dfile.encoding=UTF-8")
}
tasks.withType<Test> {
    jvmArgs("-Dfile.encoding=UTF-8")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    maven("https://api.modrinth.com/maven")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly("org.slf4j:slf4j-api:2.0.16")
    compileOnly("maven.modrinth:image-previewer:2.1.0")

    implementation("com.alibaba.fastjson2:fastjson2:2.0.53")
    implementation("de.exlll:configlib-paper:4.8.1")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("com.h2database:h2:2.3.232")
}

lombok { version.set("1.18.36") }

tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to project.version) }
}

tasks.shadowJar {
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

tasks.build { dependsOn(tasks.shadowJar) }
tasks.runServer { minecraftVersion("1.21.10") }
artifacts { add("archives", tasks.shadowJar) }
