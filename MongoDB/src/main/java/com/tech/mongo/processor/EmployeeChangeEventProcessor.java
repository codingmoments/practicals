package com.tech.mongo.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.tech.mongo.audit.ChangeEventProcessor;

/**
 * Processor to process MongoDB change events in employees collection
 *
 */
@Component
public class EmployeeChangeEventProcessor implements ChangeEventProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeChangeEventProcessor.class);
  
  @Override
  public void processChangeEvent(ChangeStreamDocument<?> e) {
    LOGGER.info("EMPLOYEE EVENT PROCESSOR");
  }

}
