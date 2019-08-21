package com.alexperal.maven;

import lombok.Data;

@Data
public class Dependency {
    private final String group;
    private final String artifact;
    private final String version;
    private final String type;
}