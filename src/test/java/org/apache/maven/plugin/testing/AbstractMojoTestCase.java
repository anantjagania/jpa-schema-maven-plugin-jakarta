//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.apache.maven.plugin.testing;

import com.google.inject.Module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import org.apache.commons.io.input.XmlStreamReader;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.MojoDescriptorCreator;
import org.apache.maven.model.Plugin;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptorBuilder;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.jupiter.api.Assertions;

public abstract class AbstractMojoTestCase {

    private static final DefaultArtifactVersion MAVEN_VERSION;
    private ComponentConfigurator configurator;
    private PlexusContainer container;
    private Map<String, MojoDescriptor> mojoDescriptors;

    public AbstractMojoTestCase() {
    }

    protected void setUp() throws Exception {
        Assertions.assertTrue(MAVEN_VERSION == null || (new DefaultArtifactVersion("3.2.3")).compareTo(MAVEN_VERSION) < 0,
          "Maven 3.2.4 or better is required");
        this.configurator = (ComponentConfigurator)this.getContainer().lookup(ComponentConfigurator.class, "basic");
        InputStream is = this.getClass().getResourceAsStream("/" + this.getPluginDescriptorLocation());
        XmlStreamReader reader = new XmlStreamReader(is);
        Map<String, Object> contextData = this.container.getContext().getContextData().entrySet()
          .stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
        InterpolationFilterReader interpolationFilterReader =
          new InterpolationFilterReader(new BufferedReader(reader), contextData);
        PluginDescriptor pluginDescriptor = (new PluginDescriptorBuilder()).build(interpolationFilterReader);
        Artifact artifact =
          ((RepositorySystem)this.container.lookup(RepositorySystem.class)).createArtifact(pluginDescriptor.getGroupId(),
            pluginDescriptor.getArtifactId(), pluginDescriptor.getVersion(), ".jar");
        artifact.setFile(this.getPluginArtifactFile());
        pluginDescriptor.setPluginArtifact(artifact);
        pluginDescriptor.setArtifacts(Arrays.asList(artifact));
        Iterator i$ = pluginDescriptor.getComponents().iterator();

        while (i$.hasNext()) {
            ComponentDescriptor<?> desc = (ComponentDescriptor)i$.next();
            this.getContainer().addComponentDescriptor(desc);
        }

        this.mojoDescriptors = new HashMap();
        i$ = pluginDescriptor.getMojos().iterator();

        while (i$.hasNext()) {
            MojoDescriptor mojoDescriptor = (MojoDescriptor)i$.next();
            this.mojoDescriptors.put(mojoDescriptor.getGoal(), mojoDescriptor);
        }

    }

    private File getPluginArtifactFile() throws IOException {
        String pluginDescriptorLocation = this.getPluginDescriptorLocation();
        URL resource = this.getClass().getResource("/" + pluginDescriptorLocation);
        File file = null;
        if (resource != null) {
            if ("file".equalsIgnoreCase(resource.getProtocol())) {
                String path = resource.getPath();
                if (path.endsWith(pluginDescriptorLocation)) {
                    file = new File(path.substring(0, path.length() - pluginDescriptorLocation.length()));
                }
            } else if ("jar".equalsIgnoreCase(resource.getProtocol())) {
                try {
                    URL jarfile = new URL(resource.getPath());
                    if ("file".equalsIgnoreCase(jarfile.getProtocol())) {
                        String path = jarfile.getPath();
                        if (path.endsWith(pluginDescriptorLocation)) {
                            file = new File(path.substring(0, path.length() - pluginDescriptorLocation.length() - 2));
                        }
                    }
                } catch (MalformedURLException var6) {
                }
            }
        }

        if (file == null || !file.exists()) {
            file = new File(getBasedir());
        }

        return file.getCanonicalFile();
    }

    protected InputStream getPublicDescriptorStream() throws Exception {
        return new FileInputStream(new File(this.getPluginDescriptorPath()));
    }

    protected String getPluginDescriptorPath() {
        return getBasedir() + "/target/classes/META-INF/maven/plugin.xml";
    }

    protected String getPluginDescriptorLocation() {
        return "META-INF/maven/plugin.xml";
    }

    protected void setupContainer() {
        ContainerConfiguration cc = this.setupContainerConfiguration();

        try {
            List<Module> modules = new ArrayList();
            this.addGuiceModules(modules);
            this.container = new DefaultPlexusContainer(cc, (Module[])modules.toArray(new Module[modules.size()]));
        } catch (PlexusContainerException var3) {
            var3.printStackTrace();
            Assertions.fail("Failed to create plexus container.");
        }

    }

    protected void addGuiceModules(List<Module> modules) {
    }

    protected ContainerConfiguration setupContainerConfiguration() {
        ClassWorld classWorld = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());
        ContainerConfiguration cc =
          (new DefaultContainerConfiguration()).setClassWorld(classWorld).setClassPathScanning("index").setAutoWiring(true)
            .setName("maven");
        return cc;
    }

    protected PlexusContainer getContainer() {
        if (this.container == null) {
            this.setupContainer();
        }

        return this.container;
    }

    protected Mojo lookupMojo(String goal, String pluginPom) throws Exception {
        return this.lookupMojo(goal, new File(pluginPom));
    }

    protected Mojo lookupEmptyMojo(String goal, String pluginPom) throws Exception {
        return this.lookupEmptyMojo(goal, new File(pluginPom));
    }

    protected Mojo lookupMojo(String goal, File pom) throws Exception {
        File pluginPom = new File(getBasedir(), "pom.xml");
        Xpp3Dom pluginPomDom = Xpp3DomBuilder.build(ReaderFactory.newXmlReader(pluginPom));
        String artifactId = pluginPomDom.getChild("artifactId").getValue();
        String groupId = this.resolveFromRootThenParent(pluginPomDom, "groupId");
        String version = this.resolveFromRootThenParent(pluginPomDom, "version");
        PlexusConfiguration pluginConfiguration = this.extractPluginConfiguration(artifactId, pom);
        return this.lookupMojo(groupId, artifactId, version, goal, pluginConfiguration);
    }

    protected Mojo lookupEmptyMojo(String goal, File pom) throws Exception {
        File pluginPom = new File(getBasedir(), "pom.xml");
        Xpp3Dom pluginPomDom = Xpp3DomBuilder.build(ReaderFactory.newXmlReader(pluginPom));
        String artifactId = pluginPomDom.getChild("artifactId").getValue();
        String groupId = this.resolveFromRootThenParent(pluginPomDom, "groupId");
        String version = this.resolveFromRootThenParent(pluginPomDom, "version");
        return this.lookupMojo(groupId, artifactId, version, goal, (PlexusConfiguration)null);
    }

    protected Mojo lookupMojo(String groupId, String artifactId, String version, String goal,
      PlexusConfiguration pluginConfiguration) throws Exception {
        this.validateContainerStatus();
        Mojo mojo = (Mojo)this.container.lookup(Mojo.ROLE, groupId + ":" + artifactId + ":" + version + ":" + goal);
        LoggerManager loggerManager = (LoggerManager)this.getContainer().lookup(LoggerManager.class);
        Log mojoLogger = new DefaultLog(loggerManager.getLoggerForComponent(Mojo.ROLE));
        mojo.setLog(mojoLogger);
        if (pluginConfiguration != null) {
            ExpressionEvaluator evaluator = new ResolverExpressionEvaluatorStub();
            this.configurator.configureComponent(mojo, pluginConfiguration, evaluator, this.getContainer().getContainerRealm());
        }

        return mojo;
    }

    protected Mojo lookupConfiguredMojo(MavenProject project, String goal) throws Exception {
        return this.lookupConfiguredMojo(this.newMavenSession(project), this.newMojoExecution(goal));
    }

    protected Mojo lookupConfiguredMojo(MavenSession session, MojoExecution execution)
      throws Exception, ComponentConfigurationException {
        MavenProject project = session.getCurrentProject();
        MojoDescriptor mojoDescriptor = execution.getMojoDescriptor();
        Mojo mojo = (Mojo)this.container.lookup(mojoDescriptor.getRole(), mojoDescriptor.getRoleHint());
        ExpressionEvaluator evaluator = new PluginParameterExpressionEvaluator(session, execution);
        Xpp3Dom configuration = null;
        Plugin plugin = project.getPlugin(mojoDescriptor.getPluginDescriptor().getPluginLookupKey());
        if (plugin != null) {
            configuration = (Xpp3Dom)plugin.getConfiguration();
        }

        if (configuration == null) {
            configuration = new Xpp3Dom("configuration");
        }

        configuration = Xpp3Dom.mergeXpp3Dom(configuration, execution.getConfiguration());
        PlexusConfiguration pluginConfiguration = new XmlPlexusConfiguration(configuration);
        if (mojoDescriptor.getComponentConfigurator() != null) {
            this.configurator = (ComponentConfigurator)this.getContainer()
              .lookup(ComponentConfigurator.class, mojoDescriptor.getComponentConfigurator());
        }

        this.configurator.configureComponent(mojo, pluginConfiguration, evaluator, this.getContainer().getContainerRealm());
        return mojo;
    }

    protected MavenSession newMavenSession(MavenProject project) {
        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        MavenExecutionResult result = new DefaultMavenExecutionResult();
        MavenSession session = new MavenSession(this.container, MavenRepositorySystemUtils.newSession(), request, result);
        session.setCurrentProject(project);
        session.setProjects(Arrays.asList(project));
        return session;
    }

    protected MojoExecution newMojoExecution(String goal) {
        MojoDescriptor mojoDescriptor = (MojoDescriptor)this.mojoDescriptors.get(goal);
        Assertions.assertNotNull(mojoDescriptor, String.format("The MojoDescriptor for the goal %s cannot be null.", goal));
        MojoExecution execution = new MojoExecution(mojoDescriptor);
        this.finalizeMojoConfiguration(execution);
        return execution;
    }

    private void finalizeMojoConfiguration(MojoExecution mojoExecution) {
        MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();
        Xpp3Dom executionConfiguration = mojoExecution.getConfiguration();
        if (executionConfiguration == null) {
            executionConfiguration = new Xpp3Dom("configuration");
        }

        Xpp3Dom defaultConfiguration = MojoDescriptorCreator.convert(mojoDescriptor);
        Xpp3Dom finalConfiguration = new Xpp3Dom("configuration");
        if (mojoDescriptor.getParameters() != null) {
            Iterator i$ = mojoDescriptor.getParameters().iterator();

            while (i$.hasNext()) {
                Parameter parameter = (Parameter)i$.next();
                Xpp3Dom parameterConfiguration = executionConfiguration.getChild(parameter.getName());
                if (parameterConfiguration == null) {
                    parameterConfiguration = executionConfiguration.getChild(parameter.getAlias());
                }

                Xpp3Dom parameterDefaults = defaultConfiguration.getChild(parameter.getName());
                parameterConfiguration = Xpp3Dom.mergeXpp3Dom(parameterConfiguration, parameterDefaults, Boolean.TRUE);
                if (parameterConfiguration != null) {
                    parameterConfiguration = new Xpp3Dom(parameterConfiguration, parameter.getName());
                    if (StringUtils.isEmpty(parameterConfiguration.getAttribute("implementation")) &&
                      StringUtils.isNotEmpty(parameter.getImplementation())) {
                        parameterConfiguration.setAttribute("implementation", parameter.getImplementation());
                    }

                    finalConfiguration.addChild(parameterConfiguration);
                }
            }
        }

        mojoExecution.setConfiguration(finalConfiguration);
    }

    protected PlexusConfiguration extractPluginConfiguration(String artifactId, File pom) throws Exception {
        Reader reader = ReaderFactory.newXmlReader(pom);
        Xpp3Dom pomDom = Xpp3DomBuilder.build(reader);
        return this.extractPluginConfiguration(artifactId, pomDom);
    }

    protected PlexusConfiguration extractPluginConfiguration(String artifactId, Xpp3Dom pomDom) throws Exception {
        Xpp3Dom pluginConfigurationElement = null;
        Xpp3Dom buildElement = pomDom.getChild("build");
        if (buildElement != null) {
            Xpp3Dom pluginsRootElement = buildElement.getChild("plugins");
            if (pluginsRootElement != null) {
                Xpp3Dom[] pluginElements = pluginsRootElement.getChildren();
                Xpp3Dom[] arr$ = pluginElements;
                int len$ = pluginElements.length;

                for (int i$ = 0; i$ < len$; ++i$) {
                    Xpp3Dom pluginElement = arr$[i$];
                    String pluginElementArtifactId = pluginElement.getChild("artifactId").getValue();
                    if (pluginElementArtifactId.equals(artifactId)) {
                        pluginConfigurationElement = pluginElement.getChild("configuration");
                        break;
                    }
                }

                if (pluginConfigurationElement == null) {
                    throw new ConfigurationException(
                      "Cannot find a configuration element for a plugin with an artifactId of " + artifactId + ".");
                }
            }
        }

        if (pluginConfigurationElement == null) {
            throw new ConfigurationException(
              "Cannot find a configuration element for a plugin with an artifactId of " + artifactId + ".");
        } else {
            return new XmlPlexusConfiguration(pluginConfigurationElement);
        }
    }

    protected Mojo configureMojo(Mojo mojo, String artifactId, File pom) throws Exception {
        this.validateContainerStatus();
        PlexusConfiguration pluginConfiguration = this.extractPluginConfiguration(artifactId, pom);
        ExpressionEvaluator evaluator = new ResolverExpressionEvaluatorStub();
        this.configurator.configureComponent(mojo, pluginConfiguration, evaluator, this.getContainer().getContainerRealm());
        return mojo;
    }

    protected Mojo configureMojo(Mojo mojo, PlexusConfiguration pluginConfiguration) throws Exception {
        this.validateContainerStatus();
        ExpressionEvaluator evaluator = new ResolverExpressionEvaluatorStub();
        this.configurator.configureComponent(mojo, pluginConfiguration, evaluator, this.getContainer().getContainerRealm());
        return mojo;
    }

    protected Object getVariableValueFromObject(Object object, String variable) throws IllegalAccessException {
        Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses(variable, object.getClass());
        field.setAccessible(true);
        return field.get(object);
    }

    protected Map<String, Object> getVariablesAndValuesFromObject(Object object) throws IllegalAccessException {
        return this.getVariablesAndValuesFromObject(object.getClass(), object);
    }

    protected Map<String, Object> getVariablesAndValuesFromObject(Class<?> clazz, Object object) throws IllegalAccessException {
        Map<String, Object> map = new HashMap();
        Field[] fields = clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        Field[] arr$ = fields;
        int len$ = fields.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            Field field = arr$[i$];
            map.put(field.getName(), field.get(object));
        }

        Class<?> superclass = clazz.getSuperclass();
        if (!Object.class.equals(superclass)) {
            map.putAll(this.getVariablesAndValuesFromObject(superclass, object));
        }

        return map;
    }

    protected void setVariableValueToObject(Object object, String variable, Object value) throws IllegalAccessException {
        Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses(variable, object.getClass());
        field.setAccessible(true);
        field.set(object, value);
    }

    private String resolveFromRootThenParent(Xpp3Dom pluginPomDom, String element) throws Exception {
        Xpp3Dom elementDom = pluginPomDom.getChild(element);
        if (elementDom == null) {
            Xpp3Dom pluginParentDom = pluginPomDom.getChild("parent");
            if (pluginParentDom != null) {
                elementDom = pluginParentDom.getChild(element);
                if (elementDom == null) {
                    throw new Exception("unable to determine " + element);
                } else {
                    return elementDom.getValue();
                }
            } else {
                throw new Exception("unable to determine " + element);
            }
        } else {
            return elementDom.getValue();
        }
    }

    private void validateContainerStatus() throws Exception {
        if (this.getContainer() == null) {
            throw new Exception("container is null, make sure super.setUp() is called");
        }
    }

    static {
        DefaultArtifactVersion version = null;
        String path = "/META-INF/maven/org.apache.maven/maven-core/pom.properties";
        InputStream is = AbstractMojoTestCase.class.getResourceAsStream(path);

        try {
            Properties properties = new Properties();
            if (is != null) {
                properties.load(is);
            }

            String property = properties.getProperty("version");
            if (property != null) {
                version = new DefaultArtifactVersion(property);
            }
        } catch (IOException var8) {
        } finally {
            IOUtil.close(is);
        }

        MAVEN_VERSION = version;
    }

    protected synchronized void teardownContainer() {
        if (null != container) {
            container.dispose();
            container = null;
        }
    }

    protected void tearDown() throws Exception {
        if (null != container) {
            teardownContainer();
        }
    }


    protected String getBasedir() {
        return Lazy.BASEDIR;
    }

    private static final class Lazy {

        static {
            final String path = System.getProperty("basedir");
            BASEDIR = null != path ? path : new File("").getAbsolutePath();
        }

        static final String BASEDIR;
    }
}
