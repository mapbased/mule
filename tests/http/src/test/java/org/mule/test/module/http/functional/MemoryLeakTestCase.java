/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.module.http.functional;

import static java.lang.String.format;
import static org.apache.http.impl.client.HttpClientBuilder.create;
import org.mule.runtime.core.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class MemoryLeakTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort port1 = new DynamicPort("port1");


  @Override
  protected String getConfigFile() {
    return "http-memory-leak.xml";
  }

  @Test
  public void jmeter() throws Exception {
    Thread.sleep(999999999);
  }


  @Test
  @Ignore
  public void memoryLeak() throws Exception {
    String url = format("http://localhost:%d/", port1.getNumber());
    String payload = RandomStringUtils.randomAlphabetic(10 * 1024);
    for (int i = 0; i < 1000000; i++) {
      System.out.println("cycle " + i);
      for (int j = 0; j < 10; j++) {
        try {
          executeRequest(url, payload);
        } catch (Exception e) {
          if (j < 10) {
            System.out.println("Error, retry " + j);
          } else {
            System.out.println("connection problem, giving up");
          }
        }
      }
    }
  }

  private void executeRequest(String url, String payload) throws Exception {
    try (CloseableHttpClient httpClient = create().build()) {
      HttpPost post = new HttpPost(url);
      post.setEntity(new StringEntity(payload));
      try (CloseableHttpResponse response = httpClient.execute(post)) {
        IOUtils.toString(response.getEntity().getContent());
      }
    }
  }

}
