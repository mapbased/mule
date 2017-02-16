/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.module.embedded.api;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

public class ContainerInfo implements Serializable {

  private final String version;
  private final URL containerBaseFolder;
  private final List<URL> services;

  public ContainerInfo(String version, URL containerBaseFolder, List<URL> services) {
    this.version = version;
    this.containerBaseFolder = containerBaseFolder;
    this.services = services;
  }

  public String getVersion() {
    return version;
  }

  public URL getContainerBaseFolder() {
    return containerBaseFolder;
  }

  public List<URL> getServices() {
    return services;
  }
}
