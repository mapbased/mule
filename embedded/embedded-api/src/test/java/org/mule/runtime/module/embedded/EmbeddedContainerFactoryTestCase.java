/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.module.embedded;

import org.mule.runtime.module.embedded.api.ArtifactInfo;
import org.mule.runtime.module.embedded.api.EmbeddedContainer;
import org.mule.runtime.module.embedded.api.EmbeddedContainerFactory;

import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EmbeddedContainerFactoryTestCase {


  @Rule
  public TemporaryFolder containerFolder = new TemporaryFolder();

  @Test
  public void createsContainer() throws Exception {
    ArtifactInfo application =
        new ArtifactInfo(Collections.singletonList(getClass().getClassLoader().getResource("mule-config.xml").toURI()), null,
                         null);

    EmbeddedContainer embeddedContainer =
        EmbeddedContainerFactory.create("4.0-SNAPSHOT", containerFolder.newFolder().toURL(), application);

    // TODO(pablo.kraan): embedded - finish this test
    embeddedContainer.start();
  }
}
