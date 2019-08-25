package com.alexperal.maven

import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph


class MavenProject {
    private val compileDeps: MutableGraph<Dependency> = GraphBuilder.directed().build()
    var id: MavenProjectId? = null

    fun dependencies(): MutableGraph<Dependency> {
        return compileDeps
    }

    fun addDependency(fatherDependency: Dependency?, dependency: Dependency) {
        this.compileDeps.addNode(dependency)
        fatherDependency?.let { this.compileDeps.putEdge(it, dependency) }
    }
}
