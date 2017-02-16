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

import org.junit.Test;

public class EmbeddedContainerFactoryTestCase {

  @Test
  public void createsContainer() throws Exception {
    ArtifactInfo application = new ArtifactInfo(null, null, null, null);

    EmbeddedContainer embeddedContainer = EmbeddedContainerFactory.create("4.0-SNAPSHOT", application);

    // TODO(pablo.kraan): embedded - finish this test
    embeddedContainer.start();
  }
}
