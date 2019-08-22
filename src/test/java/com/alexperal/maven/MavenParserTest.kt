package com.alexperal.maven

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors


internal class MavenParserTest {

    @Test
    fun testParseOutput() {
        executeMavenParserFromFile {
            val mavenParser = MavenParser()
            val project = mavenParser.parse(it)
            //assertEquals(MavenProjectId("com.com.alexperal.maventool", "maventool", "jar", "1.0-SNAPSHOT"), project.id)
            assertEquals(3, project.compileDeps().size)
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
}