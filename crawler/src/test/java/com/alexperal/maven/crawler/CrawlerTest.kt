package com.alexperal.maven.crawler

import com.alexperal.maven.models.MavenId
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.Paths

class CrawlerTest {

    @Test
    fun testCrawler() {
        val crawler = Crawler("D:\\Programas\\Maven\\3.6.1") { Paths.get(CrawlerTest::class.java.getResource("/simplepom.xml").toURI()); }
        val mavenProjectId = MavenId.from("com.alexperal.maventool", "maventool", "1.0-SNAPSHOT")
        crawler.addSeed(mavenProjectId)
        crawler.crawlNext()
        assertEquals(crawler.crawled, setOf(mavenProjectId))
    }

    @Test
    fun testCrawlerFetching() {
        val fileSystem: FileSystem? = Jimfs.newFileSystem(Configuration.unix());
        fileSystem?.let {
            val crawler = Crawler("") { Path.of("") }
            val mavenProjectId = MavenId.from("groupId", "artifactId", "version")
            crawler.addSeed(mavenProjectId)
            crawler.crawlNext()
            assertEquals(crawler.crawled, listOf(mavenProjectId))
        }
    }
}