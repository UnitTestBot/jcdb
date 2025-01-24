dependencies {
    implementation(project(":jacodb-api-jvm"))
    implementation(project(":jacodb-core"))
    implementation(Libs.jooq)

    testImplementation(testFixtures(project(":jacodb-core")))
    testImplementation(testFixtures(project(":jacodb-storage")))
    testImplementation(Libs.kotlin_logging)
    testRuntimeOnly(Libs.guava)
}
