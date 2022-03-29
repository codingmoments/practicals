package com.tech.mongo.audit;

import com.mongodb.client.model.changestream.ChangeStreamDocument;

/**
 * Interface to process MongoDB change events
 *
 */
public interface ChangeEventProcessor {
  public void processChangeEvent(ChangeStreamDocument<?> e);
}
