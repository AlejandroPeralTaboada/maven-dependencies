package com.alexperal.maven.parser

import com.alexperal.maven.models.Dependency
import com.alexperal.maven.models.MavenId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors


internal class MavenParserTest {

    @Test
    fun testParseOutputRootDependencies() {
        executeMavenParserFromFile {
            val mavenParser = MavenParser("")
            val project = mavenParser.parse(it)
            val dependencies = project.dependencies
            assertEquals(4, dependencies.nodes().filter { dependencies.predecessors(it).size == 0 }.size)
        }
    }

    @Test
    fun testParseProjectId() {
        executeMavenParserFromFile {
            val mavenParser = MavenParser("")
            val project = mavenParser.parse(it)
            assertEquals(MavenId.from("com.alexperal.maventool", "maventool", "1.0-SNAPSHOT"), project.id)
        }
    }

    @Test
    fun testParseFirstLevelDependencies() {
        executeMavenParserFromFile {
            val rootDependency = Dependency("org.apache.maven.shared", "maven-invoker", "3.0.1", "jar", "compile")
            val mavenParser = MavenParser("")
            val project = mavenParser.parse(it)
            val dependencies = project.dependencies
            assertEquals(2, dependencies.successors(rootDependency).size)
        }
    }

    @Test
    fun testParseConflicts() {
        executeMavenParserFromFile {
            val conflictNode = Dependency("commons-beanutils", "commons-beanutils", "1.9.2", "jar", "compile")
            val mavenParser = MavenParser("")
            val project = mavenParser.parse(it)
            val dependencies = project.dependencies
            assertTrue(dependencies.nodes().any { it == conflictNode })
        }
    }

    @Test
    fun testLastDependency() {
        executeMavenParserFromFile {
            val parentNode = Dependency("org.junit.platform", "junit-platform-commons", "1.5.1", "jar", "test")
            val childNode = Dependency("org.apiguardian", "apiguardian-api", "1.1.0", "jar", "test")
            val mavenParser = MavenParser("")
            val project = mavenParser.parse(it)
            val dependencies = project.dependencies
            assertTrue(dependencies.predecessors(childNode).any { it == parentNode })
        }
    }

    private fun executeMavenParserFromFile(consumer: (BlockingQueue<String>) -> Unit) {
        val outputExample = MavenParserTest::class.java.getResourceAsStream("/outputExample")
        val queue = ArrayBlockingQueue<String>(1000)
        val executorService = Executors.newCachedThreadPool()
        var task = executorService.submit {
            try {
                InputStreamReader(outputExample).buffered().useLines { it.forEach { it2 -> queue.put(it2) } }
                queue.put(MavenParser.poisonPill)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        consumer.invoke(queue)
        task.get()
        executorService.shutdown()
    }

    @Test
    fun parseDocument() {
        val resource = MavenParserTest::class.java.getResource("/outputExample").toURI()
        val mavenParser = MavenParser("")
        val project = mavenParser.parseDocument(Path.of(resource))
        assertEquals(MavenId.from("com.alexperal.maventool", "maventool", "1.0-SNAPSHOT"), project.id)

    }
}