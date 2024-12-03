pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google() // Certifique-se de que este repositório está incluído
        mavenCentral()
    }
}


rootProject.name = "GeoLocalizacaoTeste"
include(":app")
