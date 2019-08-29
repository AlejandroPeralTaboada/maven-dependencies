package com.alexperal.maven.parser

import com.alexperal.maven.models.Dependency
import com.alexperal.maven.models.MavenProject
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.regex.Pattern


class MavenParser(val mavenHome: String) {


    fun parsePom(pomPath: Path): MavenProject {
        val queue = ArrayBlockingQueue<String>(1000)
        Thread {
            val request = DefaultInvocationRequest()
            request.pomFile = pomPath.toFile()
            request.goals = listOf("dependency:tree -Dverbose")
            val invoker = DefaultInvoker()
            invoker.mavenHome = File(mavenHome)
            invoker.setOutputHandler {
                queue.put(it)
            }
            invoker.execute(request)
        }.start()
        return parse(queue)
    }

    fun parseDocument(document: Path): MavenProject {
        val queue = ArrayBlockingQueue<String>(1000)
        val executorService = Executors.newCachedThreadPool()
        executorService.submit {
            try {
                Files.newBufferedReader(document).useLines { it.forEach { it2 -> queue.put(it2) } }
                queue.put(MavenParser.poisonPill)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        executorService.shutdown()
        return parse(queue)


    }

    fun parse(queue: BlockingQueue<String>): MavenProject {
        val mavenProject = MavenProjectBuilder()
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
        return mavenProject.build()
    }

    private fun handleDependency(line: String, mavenProjectBuilder: MavenProjectBuilder, dependencyStack: Stack<Dependency>) {
        val dependency = replaceInfoLine(line)
        when {
            isProject(dependency) -> processProject(dependency, mavenProjectBuilder)
            isDependency(dependency) -> processRootDependency(dependency, mavenProjectBuilder, dependencyStack)
        }
        println(dependency)
    }

    private fun processRootDependency(line: String, mavenProjectBuilder: MavenProjectBuilder, dependencyStack: Stack<Dependency>) {
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
            mavenProjectBuilder.addDependency(fatherDependency, dependency)
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

    private fun processProject(dependency: String, mavenProjectBuilder: MavenProjectBuilder) {
        val matcher = PROJECT_ID_PATTERN.matcher(dependency)
        if (matcher.matches()) {
            val group = matcher.group(1)
            val artifact = matcher.group(2)
            val type = matcher.group(3)
            val version = matcher.group(4)
            mavenProjectBuilder.setId(group, artifact, version, type)
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
