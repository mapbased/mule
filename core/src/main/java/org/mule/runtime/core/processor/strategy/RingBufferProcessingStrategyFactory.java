/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.strategy;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.processor.strategy.AbstractSchedulingProcessingStrategy.TRANSACTIONAL_ERROR_MESSAGE;
import static org.mule.runtime.core.transaction.TransactionCoordination.isTransactionActive;
import static reactor.core.Exceptions.propagate;
import static reactor.core.publisher.Flux.from;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.exception.MessagingExceptionHandler;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;

import java.util.function.Consumer;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import reactor.core.publisher.TopicProcessor;

/**
 * Creates {@link RingBufferProcessingStrategy} instances. This processing strategy demultiplexes incoming messages to
 * single-threaded event-loop.
 *
 * This processing strategy is not suitable for transactional flows and will fail if used with an active transaction.
 *
 * @since 4.0
 */
public class RingBufferProcessingStrategyFactory implements ProcessingStrategyFactory {

  @Override
  public ProcessingStrategy create(MuleContext muleContext) {
    return new RingBufferProcessingStrategy();
  }

  static class RingBufferProcessingStrategy implements ProcessingStrategy {

    @Override
    public Function<Publisher<Event>, Publisher<Event>> onPipeline(FlowConstruct flowConstruct,
                                                                   Function<Publisher<Event>, Publisher<Event>> pipelineFunction,
                                                                   MessagingExceptionHandler messagingExceptionHandler) {
      return publisher -> from(publisher)
          .doOnNext(assertCanProcess())
          .subscribeWith(TopicProcessor.create())
          .transform(pipelineFunction);
    }

    protected Consumer<Event> assertCanProcess() {
      return event -> {
        if (isTransactionActive()) {
          throw propagate(new DefaultMuleException(createStaticMessage(TRANSACTIONAL_ERROR_MESSAGE)));
        }
      };
    }
  }

}