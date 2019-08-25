package com.alexperal.maven

data class MavenProjectId(private val group: String, private val artifact: String,
                     private val version: String, private val type: String) {
}