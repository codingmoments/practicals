package com.tech.mongo.audit;

import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import org.bson.BsonValue;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;

/**
 * Simple processor to process MongoDB change events
 *
 */
@Component
public class SimpleChangeEventProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleChangeEventProcessor.class);

  @Autowired
  private RedisTemplate<Object, Object> redisTemplate;

  public void processChangeEvent(ChangeStreamDocument<?> e) {
    Object key = e.getResumeToken().asDocument().get("_data").asString().getValue();

    if (redisTemplate.hasKey(key)) {
      LOGGER.info("Event is already being processed by other instance.");
      return;
    }

    ValueOperations<Object, Object> valueOps = redisTemplate.opsForValue();
    Boolean isKeySet = valueOps.setIfAbsent(key, "locked", 1, TimeUnit.MINUTES);

    if (isKeySet == null || !isKeySet) {
      LOGGER.info("Event is already being processed by other instance.");
      return;
    }

    ObjectId objectId = e.getDocumentKey().get("_id").asObjectId().getValue();

    if (e.getOperationType() == OperationType.INSERT) {
      LOGGER.info("A new document with Id {} has been created", objectId);
    }
    else if (e.getOperationType() == OperationType.UPDATE) {
      LOGGER.info("An existing document with Id {} has been updated", objectId);
      e.getUpdateDescription().getUpdatedFields().forEach((field, value) -> {
        logUpdatedField(field, value);
      });
    }
    else if (e.getOperationType() == OperationType.REPLACE) {
      LOGGER.info("An existing document with Id {} has been replaced", objectId);
    }
    else if (e.getOperationType() == OperationType.DELETE) {
      LOGGER.info("An existing document with Id {} has been deleted", objectId);
    }
    else {
      LOGGER.warn("Unknown operation - " + e.getOperationType());
    }
  }

  private void logUpdatedField(String field, BsonValue bsonValue) {
    Object value = new Object();
    switch (bsonValue.getBsonType()) {
      case DATE_TIME:
        value = Instant.ofEpochMilli(bsonValue.asDateTime().getValue()).atZone(ZoneId.systemDefault()).toLocalDate();
        break;
      case STRING:
        value = bsonValue.asString().getValue();
        break;
      case BOOLEAN:
        value = bsonValue.asBoolean().getValue();
        break;
      case INT32:
        value = bsonValue.asInt32().getValue();
        break;
      case INT64:
        value = bsonValue.asInt64().getValue();
        break;
      case DECIMAL128:
        value = bsonValue.asDecimal128().getValue();
        break;
      case DOUBLE:
        value = bsonValue.asDouble().getValue();
        break;
      default:
        value = bsonValue;
    }
    LOGGER.info("Field {} is updated with new value of {}", field, value);
  }
}
