package com.alexperal.maven.models

enum class MavenPackagingType {
    POM, JAR, PLUGIN, EJB, WAR, EAR, RAR, PAR;

    companion object {
        fun fromString(id: String): MavenPackagingType {
            return when (id.toLowerCase()) {
                "pom" -> POM
                "jar" -> JAR
                "maven-plugin" -> PLUGIN
                "ejb" -> EJB
                "war" -> WAR
                "ear" -> EAR
                "rar" -> RAR
                "par" -> PAR
                else -> throw IllegalArgumentException("Id supplied not recognized: $id")
            }
        }
    }
}
