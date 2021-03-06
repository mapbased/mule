/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.http.internal.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tck.util.MuleContextUtils.mockContextWithServices;
import org.mule.compatibility.transport.socket.api.TcpServerSocketProperties;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.core.api.MuleContext;
import org.mule.service.http.api.HttpConstants;
import org.mule.runtime.module.http.api.HttpListenerConnectionManager;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HttpListenerConnectionManagerTestCase extends AbstractMuleTestCase {

  private static final String SPECIFIC_IP = "172.24.24.1";
  public static final int PORT = 5555;
  public static final int CONNECTION_IDLE_TIMEOUT = 1000;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void initializationFailsWhenHostIsRepeated() throws Exception {
    testInitialization(SPECIFIC_IP, SPECIFIC_IP);
  }

  @Test
  public void initializationFailsWhenSpecificHostIsOverlapping() throws Exception {
    testInitialization(HttpConstants.ALL_INTERFACES_IP, SPECIFIC_IP);
  }

  @Test
  public void initializationFailsWhenAllInterfacesIsOverlapping() throws Exception {
    testInitialization(SPECIFIC_IP, HttpConstants.ALL_INTERFACES_IP);
  }

  private void testInitialization(String firstIp, String secondIp) throws MuleException {
    final HttpListenerConnectionManager connectionManager = new HttpListenerConnectionManager();
    final MuleContext mockMuleContext = mockContextWithServices();
    connectionManager.setMuleContext(mockMuleContext);
    Supplier<Scheduler> mockSchedulerSource = mock(Supplier.class);
    when((Object) (mockMuleContext.getRegistry().lookupObject(TcpServerSocketProperties.class)))
        .thenReturn(mock(TcpServerSocketProperties.class));

    connectionManager.initialise();
    connectionManager.createServer(new DefaultServerAddress(firstIp, PORT), mockSchedulerSource, false,
                                   CONNECTION_IDLE_TIMEOUT);
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectMessage(String.format(HttpListenerConnectionManager.SERVER_ALREADY_EXISTS_FORMAT, PORT, secondIp));

    try {
      connectionManager.createServer(new DefaultServerAddress(secondIp, PORT), mockSchedulerSource, false,
                                     CONNECTION_IDLE_TIMEOUT);
    } finally {
      connectionManager.dispose();
    }
  }
}
