/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.embedded.impl;

import static org.mule.runtime.module.embedded.impl.SerializationUtils.deserialize;
import org.mule.runtime.deployment.model.api.application.Application;
import org.mule.runtime.deployment.model.api.application.ApplicationDescriptor;
import org.mule.runtime.module.deployment.impl.internal.MuleArtifactResourcesRegistry;
import org.mule.runtime.module.embedded.api.ArtifactInfo;

import java.io.File;
import java.io.IOException;

public class EmbeddedController {

  private ArtifactInfo artifactInfo;
  private Application application;

  public EmbeddedController(byte[] configuration) throws IOException, ClassNotFoundException {
    artifactInfo = deserialize(configuration);
  }

  public void start() throws IOException {
    MuleArtifactResourcesRegistry artifactResourcesRegistry = new MuleArtifactResourcesRegistry.Builder().build();
    ApplicationDescriptor applicationDescriptor = null;

    artifactResourcesRegistry.getDomainFactory().createArtifact(createDefaultDomainDir());

    application = artifactResourcesRegistry.getApplicationFactory().createAppFrom(applicationDescriptor);
    application.start();
  }

  private File createDefaultDomainDir() {
    File defaultDomainFolder = new File(new File("domains"), "default");
    if (!defaultDomainFolder.mkdirs()) {
      throw new RuntimeException("Could not create default domain directory in " + defaultDomainFolder.getAbsolutePath());
    }
    return defaultDomainFolder;
  }

  public void stop() {
    application.stop();
  }

}
