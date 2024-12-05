dependencies {
    api(project(":jacodb-api-jvm"))
    api(project(":jacodb-core"))

    implementation(Libs.slf4j_simple)
    implementation(Libs.soot_utbot_fork)
    implementation(Libs.kotlinx_coroutines_reactor)
}

tasks.create<JavaExec>("runJacoDBPerformanceAnalysis") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.jacodb.examples.analysis.PerformanceMetricsKt")
}

tasks.create<JavaExec>("runSootPerformanceAnalysis") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.jacodb.examples.analysis.SootPerformanceMetricsKt")
}
