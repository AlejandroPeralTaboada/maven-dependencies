package com.alexperal.maven;

import java.util.ArrayList;
import java.util.List;

public class MavenProject {
    private List<Dependency> compileDeps = new ArrayList<>();
    private MavenProjectId id;

    public List<Dependency> compileDeps() {
        return compileDeps;
    }

    public void addRootDependency(Dependency dependency) {
        this.compileDeps.add(dependency);
    }

    public MavenProjectId getId() {
        return this.id;
    }
}
