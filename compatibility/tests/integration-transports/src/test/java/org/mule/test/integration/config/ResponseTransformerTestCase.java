/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.integration.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.mule.compatibility.core.api.endpoint.InboundEndpoint;
import org.mule.functional.extensions.CompatibilityFunctionalTestCase;
import org.mule.runtime.api.meta.NameableObject;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.processor.MessageProcessorChain;
import org.mule.runtime.core.api.processor.Processor;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class ResponseTransformerTestCase extends CompatibilityFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/test/integration/config/response-transformer-test-flow.xml";
  }

  @Test
  public void testTransformers() {
    Flow flowService = muleContext.getRegistry().lookupObject("service");
    InboundEndpoint endpoint = (InboundEndpoint) flowService.getMessageSource();
    assertFalse(endpoint.getMessageProcessors().isEmpty());
    assertEquals(2, endpoint.getMessageProcessors().size());
    checkNames("normal", endpoint.getMessageProcessors());
    assertFalse(endpoint.getResponseMessageProcessors().isEmpty());
    final List<Processor> messageProcessors =
        ((MessageProcessorChain) endpoint.getResponseMessageProcessors().get(0)).getMessageProcessors();
    assertEquals(2, messageProcessors.size());
    checkNames("response", messageProcessors);
  }

  protected void checkNames(String prefix, List<Processor> transformers) {
    Iterator<Processor> iterator = transformers.iterator();
    for (int count = 1; iterator.hasNext(); count++) {
      NameableObject transformer = (NameableObject) iterator.next();
      logger.debug(transformer.toString());
      assertEquals(prefix + count, transformer.getName());
    }
  }
}
