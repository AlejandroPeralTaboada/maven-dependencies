package com.alexperal.maven.models

data class MavenId(val groupId: GroupId, val artifactId: ArtifactId, val version: Version) {

    companion object {
        fun from(groupId: String, artifactId: String, version: String) = MavenId(GroupId(groupId), ArtifactId(artifactId),
                Version(version))
    }
}

