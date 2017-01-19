/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.services.oauth.internal;

import org.mule.runtime.api.el.ExpressionEvaluator;
import org.mule.runtime.core.api.scheduler.SchedulerService;
import org.mule.runtime.oauth.api.AuthorizationCodeOAuthConfig;
import org.mule.runtime.oauth.api.ClientCredentialsConfig;
import org.mule.runtime.oauth.api.OAuthDancer;
import org.mule.runtime.oauth.api.OAuthService;
import org.mule.runtime.oauth.api.state.ResourceOwnerOAuthContext;
import org.mule.service.http.api.HttpService;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;


public final class DefaultOAuthService implements OAuthService {

  private final HttpService httpService;
  private final DefaultOAuthCallbackServersManager httpServersManager;
  private final SchedulerService schedulerService;

  public DefaultOAuthService(HttpService httpService, SchedulerService schedulerService) {
    this.httpService = httpService;
    httpServersManager = new DefaultOAuthCallbackServersManager(httpService);
    this.schedulerService = schedulerService;
  }

  @Override
  public String getName() {
    return "OAuthService";
  }

  @Override
  public <T> OAuthDancer createClientCredentialsGrantTypeDancer(ClientCredentialsConfig config,
                                                                Function<String, Lock> lockProvider,
                                                                Map<String, T> tokensStore,
                                                                ExpressionEvaluator expressionEvaluator) {
    return new ClientCredentialsOAuthDancer(config, lockProvider, (Map<String, ResourceOwnerOAuthContext>) tokensStore,
                                            httpService, expressionEvaluator);
  }

  @Override
  public <T> OAuthDancer createAuthorizationCodeGrantTypeDancer(AuthorizationCodeOAuthConfig config,
                                                                Function<String, Lock> lockProvider,
                                                                Map<String, T> tokensStore,
                                                                ExpressionEvaluator expressionEvaluator) {
    return new AuthorizationCodeOAuthDancer(httpServersManager, config, schedulerService, lockProvider,
                                            (Map<String, ResourceOwnerOAuthContext>) tokensStore,
                                            httpService, expressionEvaluator);
  }

}
