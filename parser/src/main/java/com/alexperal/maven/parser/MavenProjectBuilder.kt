package com.alexperal.maven.parser

import com.alexperal.maven.models.*
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import com.google.common.graph.MutableGraph


class MavenProjectBuilder {

    private data class MavenProjectId(val group: String, val artifact: String,
                                      val version: String, val type: String) {
    }

    private val compileDeps: MutableGraph<Dependency> = GraphBuilder.directed().build()
    private var id: MavenProjectId? = null

    fun dependencies(): MutableGraph<Dependency> {
        return compileDeps
    }

    fun addDependency(fatherDependency: Dependency?, dependency: Dependency) {
        this.compileDeps.addNode(dependency)
        fatherDependency?.let { this.compileDeps.putEdge(it, dependency) }
    }

    fun build(): MavenProject {
        val id = id
        return if (id != null) {
            val dependencies: Graph<Dependency> = ImmutableGraph.copyOf(this.compileDeps);
            MavenProject(MavenId(GroupId(id.group), ArtifactId(id.artifact), Version(id.version)), MavenPackagingType.fromString(id.type), dependencies)
        } else {
            throw IllegalArgumentException("Id null")
        }

    }

    fun setId(group: String, artifact: String, version: String, type: String) {
        id=MavenProjectId(group, artifact, version, type)
    }
}
