package com.alexperal.maven.models

data class Dependency(val mavenId: MavenId, val packagingType: MavenPackagingType, val scope: MavenScope) {

    constructor(groupId: String, artifactId: String, version: String,
                packaging: String, scope: String) : this(MavenId(GroupId(groupId), ArtifactId(artifactId),
            Version(version)), MavenPackagingType.fromString(packaging), MavenScope.fromString(scope))
}