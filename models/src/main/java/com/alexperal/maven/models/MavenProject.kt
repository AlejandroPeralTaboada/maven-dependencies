package com.alexperal.maven.models

import com.google.common.graph.Graph

data class MavenProject(val id: MavenId, val packagingType: MavenPackagingType, val dependencies: Graph<Dependency>) {
}
