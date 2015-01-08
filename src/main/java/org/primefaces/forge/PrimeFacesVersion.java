package org.primefaces.forge;

import java.util.Arrays;
import java.util.List;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;

/**
 * @author handsr
 */
public enum PrimeFacesVersion {

    PRIMEFACES_5_1("PrimeFaces 5.1", Arrays.asList(DependencyBuilder.create("org.primefaces:primefaces:5.1")), Arrays
        .asList(DependencyBuilder.create("org.primefaces:primefaces:5.1"))

    ),

    PRIMEFACES_4_0("PrimeFaces 4.0", Arrays.asList(
        DependencyBuilder.create("org.primefaces:primefaces:5.1")), Arrays.asList(DependencyBuilder
        .create("org.primefaces:primefaces:5.1")));

    private List<? extends Dependency> dependencies;
    private List<? extends Dependency> dependencyManagement;
    private String name;

    private PrimeFacesVersion(String name, List<? extends Dependency> deps, List<? extends Dependency> depManagement) {
        this.name = name;
        this.dependencies = deps;
        this.dependencyManagement = depManagement;
    }

    public List<? extends Dependency> getDependencies() {
        return dependencies;
    }

    public List<? extends Dependency> getDependencyManagement() {
        return dependencyManagement;
    }

    @Override
    public String toString() {
        return name;
    }
}
