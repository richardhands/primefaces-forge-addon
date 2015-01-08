package org.primefaces.forge;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

public class PrimeFacesInstallFaceletCommand extends AbstractProjectCommand implements UICommand {

    @Inject
    public DependencyInstaller installer;

    @Inject
    private ProjectFactory projectFactory;

    @Inject
    private PrimeFacesFacet primeFacesFacet;

    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);

        if (!project.hasFacet(PrimeFacesFacet.class)) {
            return Results.fail("PrimeFacesFacet is not installed. Use 'primefaces-setup' to get started.");
        }

        UIOutput uiOutput = context.getUIContext().getProvider().getOutput();
        primeFacesFacet.createFaceletFiles(project, uiOutput);
        primeFacesFacet.createPrimeBean(project, uiOutput);
        return Results.success("Installed PrimeFaces facelet");
    }

    public void initializeUI(UIBuilder builder) throws Exception {
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("PrimeFaces: Facelet").category(Categories.create("PrimeFaces"))
            .description("Add a PrimeFaces sample.");
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
