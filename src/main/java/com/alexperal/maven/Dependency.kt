package com.alexperal.maven

data class Dependency(private val group: String, private val artifact: String, private val version: String, private val type: String, private val scope: String) {
}