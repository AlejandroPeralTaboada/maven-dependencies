package com.alexperal.maven;

import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenParser {

    private final static Predicate<String> START_MATCHER = Pattern.compile("maven-dependency-plugin:.*:tree .* @ maventool ---").asPredicate();
    private final static Predicate<String> END_MATCHER = Pattern.compile("------------------------------------------------------------------------").asPredicate();
    private final static Pattern DEPENDENCY_PATTERN = Pattern.compile("\\+\\- (.*):(.*):(.*):(.*)$");
    public static String poisonPill = new String();

    public MavenProject parse(BlockingQueue<String> queue) {
        MavenProject mavenProject = new MavenProject();
        String line;
        try {
            boolean started = false;
            while ((line = queue.take()) != poisonPill) {
                if (!started) {
                    started = checkIfLineIsStarted(line);
                } else {
                    if (isEnd(line)) {
                        break;
                    }
                    handleDependency(line, mavenProject);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mavenProject;
    }

    private void handleDependency(String line, MavenProject mavenProject) {
        String dependency = line.replaceFirst("\\[\\w*\\] ", "");
        Matcher matcher = DEPENDENCY_PATTERN.matcher(dependency);
        if (matcher.matches()) {
            String group = matcher.group(1);
            String artifact = matcher.group(2);
            String type = matcher.group(3);
            String version = matcher.group(4);
            mavenProject.addRootDependency(new Dependency(group,artifact,version,type));
        } else {
            System.out.println(dependency);
        }
    }

    private boolean isEnd(String line) {
        return END_MATCHER.test(line);
    }

    private boolean checkIfLineIsStarted(String line) {
        return START_MATCHER.test(line);
    }
}
