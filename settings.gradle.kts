pluginManagement {
    repositories {
        google()              // ✅ must be here
        mavenCentral()        // ✅ must be here
        gradlePluginPortal()  // ✅ must be here
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "JPVerbFlashcards"
include(":app")
