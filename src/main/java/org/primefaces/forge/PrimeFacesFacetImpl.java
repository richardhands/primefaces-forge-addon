package org.primefaces.forge;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.facets.constraints.FacetConstraints;
import org.jboss.forge.addon.javaee.servlet.ServletFacet;
import org.jboss.forge.addon.javaee.servlet.ServletFacet_3_0;
import org.jboss.forge.addon.javaee.servlet.ServletFacet_3_1;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.projects.facets.WebResourcesFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.shrinkwrap.descriptor.api.javaee7.ParamValueType;
import org.jboss.shrinkwrap.descriptor.api.webapp.WebAppCommonDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;

/**
 * @author handsr
 */
@FacetConstraints({ @FacetConstraint(DependencyFacet.class), @FacetConstraint(MetadataFacet.class),
        @FacetConstraint(ProjectFacet.class), @FacetConstraint(ResourcesFacet.class), @FacetConstraint(WebResourcesFacet.class) })
public class PrimeFacesFacetImpl extends PrimeFacesFacet {
    static final String FACES_SERVLET_CLASS = "javax.faces.webapp.FacesServlet";

    public boolean install() {
        installDependencies(version);
        installDescriptor(version);
        return true;
    }

    public boolean isInstalled() {
        DependencyFacet deps = getFaceted().getFacet(DependencyFacet.class);
        if (getFaceted().hasAllFacets(DependencyFacet.class, WebResourcesFacet.class, ServletFacet.class)) {
            for (PrimeFacesVersion version : PrimeFacesVersion.values()) {
                boolean hasVersionDependencies = true;
                for (Dependency dependency : version.getDependencies()) {
                    if (!deps.hasEffectiveDependency(dependency)) {
                        hasVersionDependencies = false;
                        break;
                    }
                }
                if (hasVersionDependencies) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Set the context-params and Servlet definition if they are not yet set.
     *
     * @param version
     */
    private void installDescriptor(final PrimeFacesVersion version) {
        ServletFacet servlet = getFaceted().getFacet(ServletFacet.class);

        if (servlet instanceof ServletFacet_3_0) {
            org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor descriptor = (org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor) servlet
                .getConfig();

            boolean found = false;
            for (org.jboss.shrinkwrap.descriptor.api.javaee6.ParamValueType<org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor> contextParam : descriptor
                .getAllContextParam()) {
                if (contextParam.getParamName().equals("javax.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE")) {
                    found = true;
                }
            }
            if (!found) {
                descriptor.createContextParam().paramName("javax.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE")
                    .paramValue("true");
            }

            descriptor.getOrCreateWelcomeFileList().welcomeFile("faces/index.xhtml");
            ((ServletFacet_3_0) servlet).saveConfig(descriptor);
        } else if (servlet instanceof ServletFacet_3_1) {
            WebAppDescriptor descriptor = (WebAppDescriptor) servlet.getConfig();

            boolean found = false;
            for (ParamValueType<WebAppDescriptor> contextParam : descriptor.getAllContextParam()) {
                if (contextParam.getParamName().equals("javax.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE")) {
                    found = true;
                }
            }
            if (!found) {
                descriptor.createContextParam().paramName("javax.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE")
                    .paramValue("true");
            }

            descriptor.getOrCreateWelcomeFileList().welcomeFile("faces/index.xhtml");
            ((ServletFacet_3_1) servlet).saveConfig(descriptor);
        }
    }

    /**
     * A helper method to determine if the Faces Servlet is defined in the web.xml
     *
     * @param descriptor
     * @return true if the Faces Servlet is defined, false otherwise
     */
    private boolean isFacesServletDefined(final WebAppCommonDescriptor descriptor) {
        return descriptor.exportAsString().contains(FACES_SERVLET_CLASS);
    }

    /**
     * Install the maven dependencies required for PrimeFaces
     *
     * @param version
     */
    private void installDependencies(final PrimeFacesVersion version) {
        installDependencyManagement(version);

        DependencyFacet deps = getFaceted().getFacet(DependencyFacet.class);
        for (Dependency dependency : version.getDependencies()) {
            deps.addDirectDependency(dependency);
        }

    }

    /**
     * Install the primefaces-bom in the pom's dependency management
     *
     * @param version
     */
    private void installDependencyManagement(final PrimeFacesVersion version) {
        if (version == PrimeFacesVersion.PRIMEFACES_5_1) {
            return; // no dep management for 4.5
        }
        DependencyFacet deps = getFaceted().getFacet(DependencyFacet.class);
        for (Dependency dependency : version.getDependencyManagement()) {
            deps.addManagedDependency(dependency);
        }
    }

    public String getDefaultVersion() {
        return PrimeFacesVersion.PRIMEFACES_5_1.name();
    }

    public List<String> getAvailableVersions() {
        List<String> list = new ArrayList<String>();
        for (PrimeFacesVersion v : PrimeFacesVersion.values()) {
            list.add(v.name());
        }
        return list;
    }

    private PrimeFacesVersion version;

    public void setVersion(String version) {
        this.version = PrimeFacesVersion.valueOf(version);
    }

    public String getVersion() {
        return version.name();
    }

    /**
     * Create a simple template file, and a PrimeFaces enabled index file that uses the template
     *
     * @param Project
     * @param UIOutput
     */
    public void createFaceletFiles(Project project, UIOutput uiOutput) {
        DirectoryResource webRoot = project.getFacet(WebResourcesFacet.class).getWebRootDirectory();
        DirectoryResource templateDirectory = webRoot.getOrCreateChildDirectory("templates");
        FileResource<?> templatePage = (FileResource<?>) templateDirectory.getChild("template.xhtml");
        InputStream stream = PrimeFacesFacet.class.getResourceAsStream("/org/primefaces/forge/template.xhtml");
        templatePage.setContents(stream);
        uiOutput.success(uiOutput.out(), String.format(PrimeFacesFacet.SUCCESS_MSG_FMT, "template.xhtml", "file"));

        FileResource<?> indexPage = (FileResource<?>) webRoot.getChild("index.xhtml");
        stream = PrimeFacesFacet.class.getResourceAsStream("/org/primefaces/forge/index.xhtml");
        indexPage.setContents(stream);
        uiOutput.success(uiOutput.out(), String.format(PrimeFacesFacet.SUCCESS_MSG_FMT, "index.xhtml", "file"));

        FileResource<?> forgeIndexPage = (FileResource<?>) webRoot.getChild("index.html");
        String contents;
        // TODO: if (contents.contains("Welcome to Seam Forge")) {
        forgeIndexPage.delete();
    }

    /**
     * Create a simple JSF managed bean to back the PrimeFaces input in the example facelet file
     *
     * @param Project
     * @param UIOutput
     */
    public void createPrimeBean(Project project, UIOutput uiOutput) throws FileNotFoundException {
        JavaSourceFacet source = project.getFacet(JavaSourceFacet.class);
        DirectoryResource sourceRoot = source.getBasePackageDirectory();
        FileResource<?> indexPage = (FileResource<?>) sourceRoot.getChild("PrimeBean.java");
        InputStream stream = PrimeFacesFacet.class.getResourceAsStream("/org/primefaces/forge/PrimeBean.java.txt");
        JavaSource javaSource = Roaster.parse(JavaSource.class, stream);
        // JavaParser.parse(stream);
        String pacakgename = project.getFacet(MetadataFacet.class).getProjectGroupName();
        javaSource.setPackage(pacakgename);
        source.saveJavaSource(javaSource);
        uiOutput.success(uiOutput.out(), String.format(PrimeFacesFacet.SUCCESS_MSG_FMT, "PrimeBean", "class"));
    }
}
