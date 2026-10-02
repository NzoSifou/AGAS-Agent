import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

/**
 * AGAS Agent est un APK, mais il n'est jamais installé : AGAS Manager le télécharge, vérifie sa
 * signature et charge son code. Il n'embarque que son propre code et ses règles ; le contrat
 * (agent-api) et la bibliothèque Kotlin sont fournis par le Manager à l'exécution.
 */

/** Signature de release : `keystore.properties` à la racine (hors dépôt), même clé que le Manager. */
val releaseSigning = rootProject.file("keystore.properties").takeIf { it.exists() }?.let { file ->
    Properties().apply { file.inputStream().use { load(it) } }
}

/** Version du contrat contre laquelle l'Agent est compilé, lue dans AgentApi.kt (source unique). */
val agentApiVersion: String = project(":agent-api").file("src/main/java/fr/nzosifou/agas/agent/api/AgentApi.kt")
    .readText()
    .let { Regex("""const val VERSION = (\d+)""").find(it)?.groupValues?.get(1) }
    ?: error("Version du contrat introuvable dans AgentApi.kt")

android {
    namespace = "fr.nzosifou.agas.agent"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "fr.nzosifou.agas.agent"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
        manifestPlaceholders["agentApiVersion"] = agentApiVersion
    }

    signingConfigs {
        if (releaseSigning != null) {
            create("release") {
                storeFile = file(releaseSigning.getProperty("storeFile"))
                storePassword = releaseSigning.getProperty("storePassword")
                keyAlias = releaseSigning.getProperty("keyAlias")
                keyPassword = releaseSigning.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Pas de R8 : il renommerait les classes que le Manager charge par leur nom.
            optimization {
                enable = false
            }
            if (releaseSigning != null) signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        // Les classes Android (ex. Point) renvoient des valeurs par défaut dans les tests JVM.
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    compileOnly(project(":agent-api"))
    compileOnly(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.junit)
    testImplementation(libs.json)
}
