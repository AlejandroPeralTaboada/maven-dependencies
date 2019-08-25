package com.alexperal.maven

import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.regex.Pattern

class MavenParser {

    fun parse(queue: BlockingQueue<String>): MavenProject {
        val mavenProject = MavenProject()
        val dependencyStack = Stack<Dependency>()
        var started = false
        var line: String = queue.take()
        while (line !== poisonPill) {
            if (!started) {
                started = checkIfLineIsStarted(line)
            } else {
                if (isEnd(line)) {
                    break
                }
                handleDependency(line, mavenProject, dependencyStack)
            }
            line = queue.take();
        }
        return mavenProject
    }

    private fun handleDependency(line: String, mavenProject: MavenProject, dependencyStack: Stack<Dependency>) {
        val dependency = replaceInfoLine(line)
        when {
            isProject(dependency) -> processProject(dependency, mavenProject)
            isDependency(dependency) -> processRootDependency(dependency, mavenProject, dependencyStack)
        }
        println(dependency)
    }

    private fun processRootDependency(line: String, mavenProject: MavenProject, dependencyStack: Stack<Dependency>) {
        val matcher = DEPENDENCY_PATTERN.matcher(line)
        if (matcher.matches()) {
            val depthLevel = dependencyDepthLevel(matcher.group(1), line)
            var lastDependencyDepth = dependencyStack.size
            val dependency = getDependency(line)
            while (depthLevel < lastDependencyDepth) {
                dependencyStack.pop()
                lastDependencyDepth = dependencyStack.size
            }
            val fatherDependency = if (depthLevel == 0) null else dependencyStack.peek()
            mavenProject.addDependency(fatherDependency, dependency)
            dependencyStack.push(dependency)
            if (depthLevel > lastDependencyDepth) {
                throw IllegalArgumentException()
            }
        }
    }

    private fun getDependency(dependency: String): Dependency {
        val conflictMatcher = OMITTED_DEPENDENCY_PATTERN.matcher(dependency);
        val matcher = if (conflictMatcher.matches()) {
            conflictMatcher
        } else {
            val matcher = DEPENDENCY_PATTERN.matcher(dependency);
            matcher.matches()
            matcher
        }
        val group = matcher.group(2)
        val artifact = matcher.group(3)
        val type = matcher.group(4)
        val version = matcher.group(5)
        val scope = matcher.group(6)
        return Dependency(group, artifact, version, type, scope)

    }

    private fun dependencyDepthLevel(depthLevel: String, line: String) = if (line.startsWith(" ")) ((depthLevel.count { it == ' ' } - 1) / 2) else (depthLevel.count { it == ' ' } / 2)

    private fun isDependency(dependency: String): Boolean {
        return !isProject(dependency)
    }

    private fun processProject(dependency: String, mavenProject: MavenProject) {
        val matcher = PROJECT_ID_PATTERN.matcher(dependency)
        if (matcher.matches()) {
            val group = matcher.group(1)
            val artifact = matcher.group(2)
            val type = matcher.group(3)
            val version = matcher.group(4)
            mavenProject.id = MavenProjectId(group, artifact, version, type)
        }
    }

    private fun isProject(dependency: String): Boolean {
        return !(dependency.startsWith('+') || dependency.startsWith('\\') || dependency.startsWith(' ') || dependency.startsWith('|'))
    }

    private fun replaceInfoLine(line: String) = line.replaceFirst("\\[\\w*\\] ".toRegex(), "")

    private fun isEnd(line: String): Boolean {
        return END_MATCHER.test(line)
    }

    private fun checkIfLineIsStarted(line: String): Boolean {
        return START_MATCHER.test(line)
    }

    companion object {

        private val START_MATCHER = Pattern.compile("maven-dependency-plugin:.*:tree .* @ maventool ---").asPredicate()
        private val END_MATCHER = Pattern.compile("------------------------------------------------------------------------").asPredicate()
        private val PROJECT_ID_PATTERN = Pattern.compile("(.*):(.*):(.*):(.*)$")
        private val DEPENDENCY_PATTERN = Pattern.compile("(.*) (.*):(.*):(.*):(.*):(.*)$")
        private val OMITTED_DEPENDENCY_PATTERN = Pattern.compile("(.*) \\((.*):(.*):(.*):(.*):(.*) - (.*)\\)$")
        var poisonPill = String()
    }
}
