rootProject.name = "jacodb"

plugins {
    id("com.gradle.develocity") version("3.18.2")
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.1.11"
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
    }
}

gitHooks {
    preCommit {
        from(file("pre-commit"))
    }
    createHooks(true)
}

include("jacodb-api-common")
include("jacodb-api-jvm")
include("jacodb-api-storage")
include("jacodb-core")
include("jacodb-storage")
include("jacodb-analysis")
include("jacodb-examples")
include("jacodb-benchmarks")
include("jacodb-cli")
include("jacodb-approximations")
include("jacodb-taint-configuration")
include("jacodb-ets")
include("jacodb-panda-static")
