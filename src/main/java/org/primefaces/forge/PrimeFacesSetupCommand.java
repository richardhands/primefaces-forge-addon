package org.primefaces.forge;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.javaee.servlet.ServletFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

public class PrimeFacesSetupCommand extends AbstractProjectCommand implements UICommand {

    @Inject
    public DependencyInstaller installer;

    @Inject
    private ProjectFactory projectFactory;

    @Inject
    private FacetFactory facetFactory;

    @Inject
    private PrimeFacesFacet primeFacesFacet;

    @Inject
    @WithAttributes(shortName = 'v', label = "PrimeFaces version", type = InputType.DROPDOWN)
    private UISelectOne<String> primefacesVersion;

    public void initializeUI(UIBuilder builder) throws Exception {
        builder.add(primefacesVersion);

        primefacesVersion.setDefaultValue(new Callable<String>() {
            public String call() throws Exception {
                return primeFacesFacet.getDefaultVersion();
            }
        });
        primefacesVersion.setValueChoices(new Callable<Iterable<String>>() {
            public Iterable<String> call() throws Exception {
                return primeFacesFacet.getAvailableVersions();
            }
        });
    }

    public Result execute(UIExecutionContext context) throws Exception {
        primeFacesFacet.setVersion(primefacesVersion.getValue());
        if (!getSelectedProject(context).hasFacet(ServletFacet.class)) {
            return Results.fail("Servlet Facet is not installed. Use 'servlet-setup' to install it.");
        }
        facetFactory.install(getSelectedProject(context), primeFacesFacet);
        return Results.success("Installed PrimeFaces " + primefacesVersion.getValue());
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("PrimeFaces: Setup").category(Categories.create("PrimeFaces"))
            .description("Setup PrimeFaces in your project");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

    @Override
    protected ProjectFactory getProjectFactory() {
        return projectFactory;
    }

}
