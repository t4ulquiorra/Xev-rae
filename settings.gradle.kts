pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            url = uri("https://raw.githubusercontent.com/bravepipeproject/maven-repo/master/repository")
        }
    }
}

rootProject.name = "Xev-rae"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

// Core modules
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:domain")
include(":core:media")
include(":core:network")
include(":core:crashlytics")
include(":core:crashlytics-noop")

// Feature modules
include(":feature:home")
include(":feature:player")
include(":feature:search")
include(":feature:library")
include(":feature:settings")
include(":feature:login")

// Service modules
include(":service:ytmusic-scraper")
include(":service:spotify")
include(":service:ai")
include(":service:lyrics")
include(":service:kizzy")
include(":service:listen-together")
