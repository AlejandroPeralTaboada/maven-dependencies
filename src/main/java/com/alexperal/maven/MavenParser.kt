package com.alexperal.maven

import java.util.concurrent.BlockingQueue
import java.util.regex.Pattern

class MavenParser {

    fun parse(queue: BlockingQueue<String>): MavenProject {
        val mavenProject = MavenProject()
        try {
            var started = false
            var line: String = queue.take()
            while (line !== poisonPill) {
                if (!started) {
                    started = checkIfLineIsStarted(line)
                } else {
                    if (isEnd(line)) {
                        break
                    }
                    handleDependency(line, mavenProject)
                }
                line = queue.take();
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return mavenProject
    }

    private fun handleDependency(line: String, mavenProject: MavenProject) {
        val dependency = line.replaceFirst("\\[\\w*\\] ".toRegex(), "")
        val matcher = DEPENDENCY_PATTERN.matcher(dependency)
        if (matcher.matches()) {
            val group = matcher.group(1)
            val artifact = matcher.group(2)
            val type = matcher.group(3)
            val version = matcher.group(4)
            mavenProject.addRootDependency(Dependency(group, artifact, version, type))
        } else {
            println(dependency)
        }
    }

    private fun isEnd(line: String): Boolean {
        return END_MATCHER.test(line)
    }

    private fun checkIfLineIsStarted(line: String): Boolean {
        return START_MATCHER.test(line)
    }

    companion object {

        private val START_MATCHER = Pattern.compile("maven-dependency-plugin:.*:tree .* @ maventool ---").asPredicate()
        private val END_MATCHER = Pattern.compile("------------------------------------------------------------------------").asPredicate()
        private val DEPENDENCY_PATTERN = Pattern.compile("\\+\\- (.*):(.*):(.*):(.*)$")
        var poisonPill = String()
    }
}
