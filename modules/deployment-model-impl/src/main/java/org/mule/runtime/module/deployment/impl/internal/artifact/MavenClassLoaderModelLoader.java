/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.deployment.impl.internal.artifact;

import static java.io.File.separator;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.maven.repository.internal.MavenRepositorySystemUtils.newSession;
import static org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_IGNORE;
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_NEVER;
import static org.eclipse.aether.util.artifact.ArtifactIdUtils.toId;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor.MULE_ARTIFACT_FOLDER;
import static org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor.MULE_PLUGIN_CLASSIFIER;
import static org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor.MULE_PLUGIN_POM;
import static org.mule.runtime.deployment.model.api.plugin.MavenClassLoaderConstants.EXPORTED_PACKAGES;
import static org.mule.runtime.deployment.model.api.plugin.MavenClassLoaderConstants.EXPORTED_RESOURCES;
import static org.mule.runtime.deployment.model.api.plugin.MavenClassLoaderConstants.MAVEN;
import static org.mule.runtime.module.deployment.impl.internal.plugin.MavenUtils.getPomModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor;
import org.mule.runtime.deployment.model.api.plugin.MavenClassLoaderConstants;
import org.mule.runtime.deployment.model.internal.plugin.BundlePluginDependenciesResolver;
import org.mule.runtime.module.artifact.descriptor.ArtifactDescriptorCreateException;
import org.mule.runtime.module.artifact.descriptor.BundleDescriptor;
import org.mule.runtime.module.artifact.descriptor.ClassLoaderModel;
import org.mule.runtime.module.artifact.descriptor.ClassLoaderModel.ClassLoaderModelBuilder;
import org.mule.runtime.module.artifact.descriptor.ClassLoaderModelLoader;
import org.mule.runtime.module.artifact.descriptor.InvalidDescriptorLoaderException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Model;
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.PatternInclusionsDependencyFilter;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.eclipse.aether.util.graph.visitor.PathRecordingDependencyVisitor;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible of returning the {@link BundleDescriptor} of a given plugin's location and also creating a
 * {@link ClassLoaderModel} TODO(fernandezlautaro): MULE-11094 this class is the default implementation for discovering
 * dependencies and URLs, which happens to be Maven based. There could be other ways to look for dependencies and URLs (probably
 * for testing purposes where the plugins are done by hand and without maven) which will imply implementing the jira pointed out
 * in this comment.
 *
 * @since 4.0
 */
public abstract class MavenClassLoaderModelLoader implements ClassLoaderModelLoader {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private DefaultRepositorySystemSession session;
  private RepositorySystem system;

  @Override
  public String getId() {
    return MAVEN;
  }

  /**
   * Given a plugin's location, it will resolve its dependencies on a Maven based mechanism. It will assume there's a
   * {@link ArtifactPluginDescriptor#REPOSITORY} folder to look for the artifacts in it (which includes both JAR files as well as
   * POM ones).
   * <p/>
   * It takes care of the transitive compile and runtime dependencies, from which will take the URLs to add them to the resulting
   * {@link ClassLoaderModel}, and it will also consume all Mule plugin dependencies so that further validations can check whether
   * or not all plugins are loaded in memory before running an application.
   * <p/>
   * Finally, it will also tell the resulting {@link ClassLoaderModel} which packages and/or resources has to export, consuming
   * the attributes from the {@link MuleArtifactLoaderDescriptor#getAttributes()} map.
   *
   * @param artifactFolder {@link File} where the current plugin to work with.
   * @param attributes a set of attributes to work with, where the current implementation of this class will look for
   *        {@link MavenClassLoaderConstants#EXPORTED_PACKAGES} and {@link MavenClassLoaderConstants#EXPORTED_RESOURCES}
   * @return a {@link ClassLoaderModel} loaded with all its dependencies and URLs.
   * @see BundlePluginDependenciesResolver#getArtifactsWithDependencies(List, Set)
   */
  @Override
  public final ClassLoaderModel load(File artifactFolder, Map<String, Object> attributes)
      throws InvalidDescriptorLoaderException {
    final Model model = getPomModel(artifactFolder);
    final ClassLoaderModelBuilder classLoaderModelBuilder = new ClassLoaderModelBuilder();
    classLoaderModelBuilder
        .exportingPackages(new HashSet<>(getAttribute(attributes, EXPORTED_PACKAGES)))
        .exportingResources(new HashSet<>(getAttribute(attributes, EXPORTED_RESOURCES)));
    final DependencyResult dependencyResult = assemblyDependenciesFromPom(artifactFolder, model);
    final PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
    // This adds a ton of things that not always make sense
    dependencyResult.getRoot().accept(nlg);
    loadUrls(artifactFolder, classLoaderModelBuilder, dependencyResult, nlg);
    loadDependencies(classLoaderModelBuilder, dependencyResult, nlg);
    return classLoaderModelBuilder.build();
  }

  protected abstract void loadDependencies(ClassLoaderModelBuilder classLoaderModelBuilder, DependencyResult dependencyResult,
                                           PreorderNodeListGenerator nlg);

  protected abstract void loadUrls(File pluginFolder, ClassLoaderModelBuilder classLoaderModelBuilder,
                                   DependencyResult dependencyResult, PreorderNodeListGenerator nlg);

  /**
   * Dependency validator to keep those that are Mule plugins. TODO(fernandezlautaro): MULE-11095 We will keep only Mule plugins
   * dependencies or
   * org.mule.runtime.deployment.model.internal.plugin.BundlePluginDependenciesResolver.getArtifactsWithDependencies() will fail
   * looking them up.
   *
   * @param dependency to validate
   * @return true if the {@link Dependency} is {@link ArtifactPluginDescriptor#MULE_PLUGIN_CLASSIFIER}, false otherwise
   */
  protected boolean isMulePlugin(Dependency dependency) {
    return MULE_PLUGIN_CLASSIFIER.equals(dependency.getArtifact().getClassifier());
  }

  private DependencyResult assemblyDependenciesFromPom(File pluginFolder, Model model)
      throws InvalidDescriptorLoaderException {

    Artifact defaultArtifact = new DefaultArtifact(model.getGroupId(), model.getArtifactId(),
                                                   null,
                                                   "pom",
                                                   model.getVersion() != null ? model.getVersion()
                                                       : model.getParent().getVersion());

    createRepositorySystem(pluginFolder, defaultArtifact);
    final CollectRequest currentPluginRequest = new CollectRequest();
    try {
      final ArtifactDescriptorResult artifactDescriptorResult =
          system.readArtifactDescriptor(session, new ArtifactDescriptorRequest(defaultArtifact, null, null));
      List<Dependency> dependencies = artifactDescriptorResult.getDependencies();
      List<Dependency> dependenciesWithExclusions = new ArrayList<>();
      dependencies.stream()
          .forEach(dependency -> {
            if ("mule-plugin".equals(dependency.getArtifact().getClassifier())) {
              dependenciesWithExclusions.add(dependency.setExclusions(asList(new Exclusion("*", "*", "*", "*"))));
            } else {
              dependenciesWithExclusions.add(dependency);
            }
          });
      currentPluginRequest.setDependencies(dependenciesWithExclusions);
      currentPluginRequest.setManagedDependencies(artifactDescriptorResult.getManagedDependencies());
      currentPluginRequest.setRepositories(
                                           asList((new RemoteRepository.Builder("http://central.maven.org/maven2/", "default",
                                                                                "http://central.maven.org/maven2/".trim())
                                                                                    .build())));

      final CollectResult collectResult = system.collectDependencies(session, currentPluginRequest);

      final DependencyRequest currentPluginDependenciesRequest = new DependencyRequest();
      currentPluginDependenciesRequest.setFilter(new ScopeDependencyFilter(JavaScopes.TEST, JavaScopes.PROVIDED));
      currentPluginDependenciesRequest.setRoot(collectResult.getRoot());
      currentPluginDependenciesRequest.setCollectRequest(currentPluginRequest);
      final DependencyResult dependencyResult = system.resolveDependencies(session, currentPluginDependenciesRequest);
      return dependencyResult;
    } catch (DependencyResolutionException e) {
      DependencyNode node = e.getResult().getRoot();
      logUnresolvedArtifacts(node, e);
      throw new InvalidDescriptorLoaderException(format("There was an issue solving the dependencies for the plugin [%s]",
                                                        pluginFolder.getAbsolutePath()),
                                                 e);
    } catch (DependencyCollectionException e) {
      throw new InvalidDescriptorLoaderException(format("There was an issue resolving the dependency tree for the plugin [%s]",
                                                        pluginFolder.getAbsolutePath()),
                                                 e);
    } catch (ArtifactDescriptorException e) {
      throw new ArtifactDescriptorCreateException(format("There was an issue resolving the artifact descriptor for the plugin [%s]",
                                                         pluginFolder.getAbsolutePath()),
                                                  e);
    }
  }

  protected boolean isMulePlugin(Artifact artifact) {
    return "mule-plugin".equals(artifact.getClassifier());
  }

  private List<String> getAttribute(Map<String, Object> attributes, String attribute) {
    final Object attributeObject = attributes.getOrDefault(attribute, new ArrayList<String>());
    checkArgument(attributeObject instanceof List, format("The '%s' attribute must be of '%s', found '%s'", attribute,
                                                          List.class.getName(), attributeObject.getClass().getName()));
    return (List<String>) attributeObject;
  }

  private void createRepositorySystem(File pluginFolder, Artifact pluginArtifact) {

    session = newDefaultRepositorySystemSession();
    RepositorySystem repositorySystem = createRepositorySystem();
    session.setLocalRepositoryManager(repositorySystem
        .newLocalRepositoryManager(session, new LocalRepository("/Users/pablolagreca/.m2/repository")));
    session.setOffline(false);
    session.setIgnoreArtifactDescriptorRepositories(true);
    session.setWorkspaceReader(new PomWorkspaceReader(pluginFolder, pluginArtifact));
    system = repositorySystem;
  }

  public RepositorySystem createRepositorySystem() {
    DefaultServiceLocator locator = new DefaultServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    locator.addService(TransporterFactory.class, FileTransporterFactory.class);
    locator.addService(RepositorySystem.class, DefaultRepositorySystem.class);
    locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
    locator.addService(VersionResolver.class, DefaultVersionResolver.class);
    locator.addService(VersionRangeResolver.class, DefaultVersionRangeResolver.class);
    locator.addService(ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class);
    locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {

      @Override
      public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
        exception.printStackTrace();
      }
    });
    return locator.getService(RepositorySystem.class);
  }

  private static DefaultRepositorySystemSession newDefaultRepositorySystemSession() {
    final DefaultRepositorySystemSession session = newSession();
    session.setUpdatePolicy(UPDATE_POLICY_NEVER);
    session.setChecksumPolicy(CHECKSUM_POLICY_IGNORE);
    return session;
  }

  private static RepositorySystem newRepositorySystem(File mavenLocalRepositoryLocation, DefaultRepositorySystemSession session) {
    final RepositorySystem system = newRepositorySystem();
    // We have to set to use a "simple" aether local repository so it will not cache artifacts (enhanced is supported for doing
    // operations such install).
    final LocalRepository localRepo = new LocalRepository(mavenLocalRepositoryLocation, "simple");
    session
        .setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
    return system;
  }

  /**
   * Creates and configures the {@link RepositorySystem} to use for resolving transitive dependencies.
   *
   * @return {@link RepositorySystem}
   */
  private static RepositorySystem newRepositorySystem() {
    /*
     * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the pre populated
     * DefaultServiceLocator, we only MavenXpp3Reader need to register the repository connector and transporter factories.
     */
    final DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    locator.addService(TransporterFactory.class, FileTransporterFactory.class);
    return locator.getService(RepositorySystem.class);
  }

  private void logUnresolvedArtifacts(DependencyNode node, DependencyResolutionException e) {
    List<ArtifactResult> artifactResults = e.getResult().getArtifactResults().stream()
        .filter(artifactResult -> !artifactResult.getExceptions().isEmpty()).collect(toList());

    final List<String> patternInclusion =
        artifactResults.stream().map(artifactResult -> toId(artifactResult.getRequest().getArtifact())).collect(toList());

    PathRecordingDependencyVisitor visitor =
        new PathRecordingDependencyVisitor(new PatternInclusionsDependencyFilter(patternInclusion), node.getArtifact() != null);
    node.accept(visitor);

    visitor.getPaths().stream().forEach(path -> {
      List<DependencyNode> unresolvedArtifactPath =
          path.stream().filter(dependencyNode -> dependencyNode.getArtifact() != null).collect(toList());
      if (!unresolvedArtifactPath.isEmpty()) {
        logger.warn("Dependency path to not resolved artifacts -> " + unresolvedArtifactPath.toString());
      }
    });
  }

  /**
   * Custom implementation of a {@link WorkspaceReader} meant to be tightly used with the plugin mechanism, where the POM file is
   * inside the {@link ArtifactPluginDescriptor#MULE_ARTIFACT_FOLDER}. For any other {@link Artifact} it will return values that
   * will force the dependency mechanism to look for in a different {@link WorkspaceReader}
   *
   * @since 4.0
   */
  private class PomWorkspaceReader implements WorkspaceReader {

    private final File pluginFolder;
    private final Artifact pluginArtifact;
    final WorkspaceRepository workspaceRepository;

    /**
     * @param pluginFolder plugin's folder used to look for the POM file
     * @param pluginArtifact plugin's artifact to compare, so that resolves the file in {@link #findArtifact(Artifact)} when it
     *        matches with the {@link #pluginArtifact}
     */
    PomWorkspaceReader(File pluginFolder, Artifact pluginArtifact) {
      this.pluginFolder = pluginFolder;
      this.pluginArtifact = pluginArtifact;
      this.workspaceRepository = new WorkspaceRepository(format("worskpace-repository-%s", pluginFolder.getName()));
    }

    @Override
    public WorkspaceRepository getRepository() {
      return workspaceRepository;
    }

    @Override
    public File findArtifact(Artifact artifact) {
      if (checkArtifact(artifact)) {
        return new File(pluginFolder, MULE_ARTIFACT_FOLDER + separator + MULE_PLUGIN_POM);
      }
      return null;
    }

    @Override
    public List<String> findVersions(Artifact artifact) {
      if (checkArtifact(artifact)) {
        return singletonList(artifact.getVersion());
      }
      return emptyList();
    }

    private boolean checkArtifact(Artifact artifact) {
      return pluginArtifact.getGroupId().equals(artifact.getGroupId())
          && pluginArtifact.getArtifactId().equals(artifact.getArtifactId())
          && pluginArtifact.getVersion().equals(artifact.getVersion())
          && pluginArtifact.getExtension().equals(artifact.getExtension());
    }
  }
}
