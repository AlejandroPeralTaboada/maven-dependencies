package com.alexperal.maven.crawler;

import com.alexperal.maven.models.Dependency
import com.alexperal.maven.models.MavenId
import com.alexperal.maven.models.MavenProject
import com.alexperal.maven.parser.MavenParser
import java.nio.file.Path

interface Resolver {
    fun resolve(id: MavenId): Path
}

class Crawler(private val mavenHome: String, private val resolver: (MavenId) -> Path) {
    private val frontier: MutableSet<MavenId> = mutableSetOf()
    val crawled: MutableSet<MavenId> = mutableSetOf()

    fun addSeed(seed: MavenId) {
        frontier.add(seed)
    }

    fun crawlNext() {
        val project = crawl(frontier.first())
        crawled.add(project.id)
        val nodes = project.dependencies.nodes()
        frontier.addAll(computeMissingNodes(nodes))
    }

    private fun computeMissingNodes(nodes: Set<Dependency>): Set<MavenId> {
        return nodes.filter { !crawled.contains(it.mavenId) }.map { it.mavenId }.toSet()
    }

    private fun crawl(seed: MavenId): MavenProject {
        val seedPath = resolver(seed)
        return parseSeed(seedPath)
    }

    private fun parseSeed(seedPath: Path): MavenProject {
        return MavenParser(mavenHome).parsePom(seedPath);
    }


}
