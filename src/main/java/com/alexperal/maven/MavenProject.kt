package com.alexperal.maven

import java.util.ArrayList


class MavenProject {
    private val compileDeps = ArrayList<Dependency>()
    val id: MavenProjectId? = null

    fun compileDeps(): List<Dependency> {
        return compileDeps
    }

    fun addRootDependency(dependency: Dependency) {
        this.compileDeps.add(dependency)
    }
}
