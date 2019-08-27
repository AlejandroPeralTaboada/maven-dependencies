package com.alexperal.maven.crawler

import com.alexperal.maven.models.MavenId
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.FileSystem

class CrawlerTest {

    @Test
    fun testCrawler() {
        val fileSystem: FileSystem? = Jimfs.newFileSystem(Configuration.unix());
        fileSystem?.let {
            val crawler = Crawler(fileSystem.getPath("/"))
            val mavenProjectId = MavenId.from("groupId", "artifactId", "version")
            crawler.addSeed(mavenProjectId)
            crawler.crawlNext()
            assertEquals(crawler.crawled, listOf(mavenProjectId))

        }
    }

    @Test
    fun testCrawlerFetching() {
        val fileSystem: FileSystem? = Jimfs.newFileSystem(Configuration.unix());
        fileSystem?.let {
            val crawler = Crawler(fileSystem.getPath("/"))
            val mavenProjectId = MavenId.from("groupId", "artifactId", "version")
            crawler.addSeed(mavenProjectId)
            crawler.crawlNext()
            assertEquals(crawler.crawled, listOf(mavenProjectId))
        }
    }
}