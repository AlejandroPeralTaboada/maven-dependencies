package com.alexperal.maven.models

enum class MavenScope {
    COMPILE, PROVIDED, RUNTIME, TEST, SYSTEM, IMPORT;

    companion object {
        fun fromString(id: String): MavenScope {
            return when (id.toUpperCase()) {
                "COMPILE" -> COMPILE
                "PROVIDED" -> PROVIDED
                "RUNTIME" -> RUNTIME
                "TEST" -> TEST
                "SYSTEM" -> SYSTEM
                "IMPORT" -> IMPORT
                else -> throw IllegalArgumentException("Id supplied not recognized: $id")
            }
        }
    }
}