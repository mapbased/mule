/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.embedded.impl;

import static java.lang.System.setProperty;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.mule.runtime.container.api.MuleFoldersUtil.getDomainsFolder;
import static org.mule.runtime.container.api.MuleFoldersUtil.getServicesFolder;
import static org.mule.runtime.core.api.config.MuleProperties.MULE_HOME_DIRECTORY_PROPERTY;
import static org.mule.runtime.module.embedded.impl.SerializationUtils.deserialize;
import org.mule.runtime.core.util.FileUtils;
import org.mule.runtime.deployment.model.api.application.Application;
import org.mule.runtime.deployment.model.api.application.ApplicationDescriptor;
import org.mule.runtime.module.deployment.impl.internal.MuleArtifactResourcesRegistry;
import org.mule.runtime.module.embedded.api.ArtifactInfo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class EmbeddedController {

  private ArtifactInfo artifactInfo;
  private Application application;

  public EmbeddedController(byte[] configuration) throws IOException, ClassNotFoundException {
    artifactInfo = deserialize(configuration);
  }

  public void start() throws IOException {
    setUpEnvironment();
    createApplication();
    application.start();
  }

  private void createApplication() throws IOException {
    MuleArtifactResourcesRegistry artifactResourcesRegistry = new MuleArtifactResourcesRegistry.Builder().build();
    ApplicationDescriptor applicationDescriptor = null;

    artifactResourcesRegistry.getDomainFactory().createArtifact(createDefaultDomainDir());

    application = artifactResourcesRegistry.getApplicationFactory().createAppFrom(applicationDescriptor);
  }

  public void stop() {
    FileUtils.deleteTree(new File(artifactInfo.getContainerBaseFolder().getPath()));
    application.stop();
  }

  private void setUpEnvironment() {
    setProperty(MULE_HOME_DIRECTORY_PROPERTY, artifactInfo.getContainerBaseFolder().getPath());
    getDomainsFolder().mkdirs();
    getServicesFolder().mkdirs();
    URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
    URL[] urLs = urlClassLoader.getURLs();
    for (URL urL : urLs) {
      File dependencyFile = new File(urL.getPath());
      if (dependencyFile.getName().endsWith("zip")) {
        try {
          copyFile(dependencyFile, new File(getServicesFolder(), dependencyFile.getName()));
        } catch (IOException e) {
          throw new RuntimeException("Failure copying services", e);
        }
      }
    }
  }

  private File createDefaultDomainDir() {
    File defaultDomainFolder = new File(new File("domains"), "default");
    if (!defaultDomainFolder.mkdirs()) {
      throw new RuntimeException("Could not create default domain directory in " + defaultDomainFolder.getAbsolutePath());
    }
    return defaultDomainFolder;
  }

}
