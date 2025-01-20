plugins {
  kotlin("jvm") version "2.1.0"
  application
}

repositories {
  mavenCentral()
}

sourceSets.main {
  java.srcDirs("src")
  resources.srcDir("resources")
}

application {
  mainClass = "ProjectGenerator"
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xmulti-dollar-interpolation")
  }
}
