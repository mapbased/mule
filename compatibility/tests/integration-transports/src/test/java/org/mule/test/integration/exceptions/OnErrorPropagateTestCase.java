/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.integration.exceptions;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mule.service.http.api.HttpConstants.HttpStatus.INTERNAL_SERVER_ERROR;
import org.mule.functional.exceptions.FunctionalTestException;
import org.mule.functional.extensions.CompatibilityFunctionalTestCase;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.client.MuleClient;
import org.mule.runtime.core.api.context.notification.ExceptionNotificationListener;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.context.notification.ExceptionNotification;
import org.mule.runtime.core.util.CharSetUtils;
import org.mule.runtime.core.util.concurrent.Latch;
import org.mule.service.http.api.HttpService;
import org.mule.services.http.TestHttpClient;
import org.mule.tck.junit4.rule.DynamicPort;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class OnErrorPropagateTestCase extends CompatibilityFunctionalTestCase {

  public static final int TIMEOUT = 5000;
  public static final int MAX_REDELIVERY = 4;
  public static final int EXPECTED_DELIVERED_TIMES = MAX_REDELIVERY + 1;
  public static final int SHORT_MAX_REDELIVERY = 2;
  public static final int EXPECTED_SHORT_DELIVERED_TIMES = SHORT_MAX_REDELIVERY + 1;
  public static final String MESSAGE = "some message";
  public static final String MESSAGE_EXPECTED = "some message consumed successfully";

  @Rule
  public DynamicPort dynamicPort1 = new DynamicPort("port1");
  @Rule
  public DynamicPort dynamicPort2 = new DynamicPort("port2");
  @Rule
  public TestHttpClient httpClient = new TestHttpClient.Builder(getService(HttpService.class)).build();

  public OnErrorPropagateTestCase() {
    System.setProperty("maxRedelivery", String.valueOf(MAX_REDELIVERY));
    System.setProperty("shortMaxRedelivery", String.valueOf(SHORT_MAX_REDELIVERY));
  }

  @Override
  protected String getConfigFile() {
    return "org/mule/test/integration/exceptions/on-error-propagate-use-case-flow.xml";
  }

  @Test
  public void testAlwaysRollback() throws Exception {
    final CountDownLatch latch = new CountDownLatch(EXPECTED_DELIVERED_TIMES);
    MuleClient client = muleContext.getClient();
    muleContext.registerListener((ExceptionNotificationListener<ExceptionNotification>) notification -> latch.countDown());
    client.dispatch("vm://in", "some message", null);
    if (!latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
      fail("message should have been delivered at least 5 times");
    }
  }

  @Test
  @Ignore("MULE-6926: flaky test")
  public void testAlwaysRollbackJmsNoTransaction() throws Exception {
    final CountDownLatch latch = new CountDownLatch(EXPECTED_DELIVERED_TIMES);
    MuleClient client = muleContext.getClient();
    muleContext.registerListener((ExceptionNotificationListener<ExceptionNotification>) notification -> latch.countDown());
    client.dispatch("jms://in?connector=activeMq", "some message", null);
    if (!latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
      fail("message should have been delivered at least 5 times");
    }
  }

  @Test
  @Ignore("MULE-6926: flaky test")
  public void testRedeliveryExhaustedTransactional() throws Exception {
    final CountDownLatch latch = new CountDownLatch(EXPECTED_DELIVERED_TIMES);
    final MutableInt deliveredTimes = new MutableInt(0);
    MuleClient client = muleContext.getClient();
    muleContext.registerListener((ExceptionNotificationListener<ExceptionNotification>) notification -> {
      deliveredTimes.increment();
      latch.countDown();
    });
    client.dispatch("jms://in2?connector=activeMq", MESSAGE, null);
    if (!latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
      fail("message should have been delivered at least 5 times");
    }
    assertThat(deliveredTimes.intValue(), is(EXPECTED_DELIVERED_TIMES));
    InternalMessage dlqMessage = client.request("jms://dlq?connector=activeMq", TIMEOUT).getRight().get();
    assertThat(dlqMessage, notNullValue());
    assertThat(getPayloadAsString(dlqMessage), is(MESSAGE_EXPECTED));
  }

  @Test
  public void testRollbackWithComponent() throws Exception {
    final CountDownLatch latch = new CountDownLatch(EXPECTED_DELIVERED_TIMES);
    MuleClient client = muleContext.getClient();
    muleContext.registerListener((ExceptionNotificationListener<ExceptionNotification>) notification -> latch.countDown());
    client.dispatch("vm://in5", "some message", null);
    if (!latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
      fail("message should have been delivered at least 5 times");
    }
    InternalMessage result = client.send("vm://in5", MESSAGE, null, TIMEOUT).getRight();
    assertThat(result, notNullValue());
    assertThat(getPayloadAsString(result), is(MESSAGE + " Rolled Back"));
  }

  @Test
  public void testFullyDefinedRollbackExceptionStrategyWithComponent() throws Exception {
    MuleClient client = muleContext.getClient();
    InternalMessage result = null;
    for (int i = 1; i <= EXPECTED_SHORT_DELIVERED_TIMES; i++) {
      result = client.send("vm://in6", MESSAGE, null, TIMEOUT).getRight();
      assertThat(result, notNullValue());
      assertThat(result.getExceptionPayload(), notNullValue());
      assertThat(getPayloadAsString(result), is(MESSAGE + " apt1 apt2 apt3"));
    }
    result = client.send("vm://in6", MESSAGE, null, TIMEOUT).getRight();
    assertThat(result, notNullValue());
    assertThat(getPayloadAsString(result), is(MESSAGE + " apt4 groovified"));
  }

  @Test
  public void testRedeliveryExhaustedNoTransaction() throws Exception {
    final CountDownLatch latch = new CountDownLatch(EXPECTED_DELIVERED_TIMES);
    final MutableInt deliveredTimes = new MutableInt(0);
    MuleClient client = muleContext.getClient();
    muleContext.registerListener((ExceptionNotificationListener<ExceptionNotification>) notification -> {
      deliveredTimes.increment();
      latch.countDown();
    });
    client.dispatch("jms://in3?connector=activeMq", MESSAGE, null);
    if (!latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
      fail("message should have been delivered at least 5 times");
    }
    assertThat(deliveredTimes.intValue(), is(EXPECTED_DELIVERED_TIMES));
    InternalMessage dlqMessage = client.request("jms://dlq?connector=activeMq", TIMEOUT).getRight().get();
    assertThat(dlqMessage, notNullValue());
    assertThat(getPayloadAsString(dlqMessage), is(MESSAGE_EXPECTED));
  }

  @Test
  public void testHttpAlwaysRollbackUsingHttpClient() throws Exception {
    HttpClient httpClient = new HttpClient();
    GetMethod getMethod = new GetMethod(format("http://localhost:%s", dynamicPort1.getNumber()));
    int status = httpClient.executeMethod(getMethod);
    assertThat(status, is(INTERNAL_SERVER_ERROR.getStatusCode()));
    getMethod.releaseConnection();
  }

  @Ignore("See MULE-9197")
  @Test
  public void testHttpRedeliveryExhaustedRollbackUsingHttpClient() throws Exception {
    HttpClient httpClient = new HttpClient();
    PostMethod postMethod = new PostMethod(format("http://localhost:%s", dynamicPort2.getNumber()));
    postMethod.setRequestEntity(new StringRequestEntity(MESSAGE, "html/text", CharSetUtils.defaultCharsetName()));
    int status;
    for (int i = 1; i <= EXPECTED_SHORT_DELIVERED_TIMES; i++) {
      status = httpClient.executeMethod(postMethod);
      assertThat(status, is(500));
      postMethod.releaseConnection();
    }
    status = httpClient.executeMethod(postMethod);
    assertThat(status, is(200));
    assertThat(postMethod.getResponseBodyAsString(), is(MESSAGE_EXPECTED));
    postMethod.releaseConnection();
  }

  @Test
  public void testFullyDefinedRollbackExceptionStrategy() throws Exception {
    MuleClient client = muleContext.getClient();
    InternalMessage result = null;
    for (int i = 1; i <= EXPECTED_SHORT_DELIVERED_TIMES; i++) {
      result = client.send("vm://in2", MESSAGE, null, TIMEOUT).getRight();
      assertThat(result, notNullValue());
      assertThat(result.getExceptionPayload(), notNullValue());
      assertThat(getPayloadAsString(result), is(MESSAGE + " apt1 apt2 apt3"));
    }
    result = client.send("vm://in2", MESSAGE, null, TIMEOUT).getRight();
    assertThat(result, notNullValue());
    assertThat(getPayloadAsString(result), is(MESSAGE + " apt4 apt5"));
  }

  @Test
  @Ignore("MULE-6926: flaky test")
  public void testRedeliveryPolicyRedefinition() throws Exception {
    final CountDownLatch latch = new CountDownLatch(EXPECTED_DELIVERED_TIMES);
    final MutableInt deliveredTimes = new MutableInt(0);
    MuleClient client = muleContext.getClient();
    muleContext.registerListener((ExceptionNotificationListener<ExceptionNotification>) notification -> {
      deliveredTimes.increment();
      latch.countDown();
    });
    client.dispatch("vm://in3", "some message", null);
    if (!latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
      fail("message should have been delivered at least 5 times");
    }
    assertThat(deliveredTimes.intValue(), is(EXPECTED_DELIVERED_TIMES));
  }

  @Test
  @Ignore("MULE-6926: flaky test")
  public void testInboundEndpointMaxRedeliveryTakesPrecendence() throws Exception {
    final CountDownLatch latch = new CountDownLatch(EXPECTED_DELIVERED_TIMES);
    final MutableInt deliveredTimes = new MutableInt(0);
    MuleClient client = muleContext.getClient();
    muleContext.registerListener((ExceptionNotificationListener<ExceptionNotification>) notification -> {
      deliveredTimes.increment();
      latch.countDown();
    });
    client.dispatch("vm://in4", "some message", null);
    if (!latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
      fail("message should have been delivered at least 5 times");
    }
    assertThat(deliveredTimes.intValue(), is(EXPECTED_DELIVERED_TIMES));
  }

  @Test
  public void testRollbackExceptionStrategyCatchMessageRedeliveryDespiteChoiceConfiguration() throws Exception {
    MuleClient client = muleContext.getClient();
    client.dispatch("vm://in7", "some message", null);
    if (!CallMessageProcessor.latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
      fail("custom message processor wasn't call");
    }
  }



  private void verifyFlow(String flowName, Object payload) throws InterruptedException {
    try {
      flowRunner(flowName).withPayload(payload).run();
    } catch (Exception e) {
      assertThat(e.getCause(), is(instanceOf(FunctionalTestException.class)));
      if (!CallMessageProcessor.latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
        fail("custom message processor wasn't call");
      }
    }
  }

  private void verifyFlow(String flowName) throws InterruptedException {
    verifyFlow(flowName, MESSAGE);
  }

  public static class CallMessageProcessor implements Processor {

    public static Latch latch = new Latch();

    @Override
    public Event process(Event event) throws MuleException {
      latch.release();
      return event;
    }
  }
}
