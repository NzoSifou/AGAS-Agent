pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AGAS-Agent"
include(":agent")

// Contrat Manager ↔ Agent : compilé depuis les sources d'AGAS Manager, cloné à côté de ce dossier,
// pour qu'il n'en existe qu'une seule copie.
val managerDir = listOf("../AGAS-Manager", "../AGAS").map { file(it) }
    .firstOrNull { it.resolve("agent-api/build.gradle.kts").isFile }
    ?: error("AGAS Manager introuvable : cloner https://github.com/NzoSifou/AGAS-Manager à côté de ce dossier.")
include(":agent-api")
project(":agent-api").projectDir = managerDir.resolve("agent-api")
