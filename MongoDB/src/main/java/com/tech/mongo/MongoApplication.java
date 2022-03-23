package com.tech.mongo;

import java.time.Instant;
import java.time.ZoneId;

import javax.annotation.PostConstruct;

import org.bson.BsonValue;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import com.tech.mongo.domain.Employee;

@EnableMongoRepositories
@SpringBootApplication
public class MongoApplication {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoApplication.class);

  @Autowired
  private MongoClient mongoClient;

  public static void main(String[] args) {
    SpringApplication.run(MongoApplication.class, args);
  }

  @PostConstruct
  public void postContruct() {

    // Registering to MongoDB Change Stream
    Runnable thread = new Runnable() {
      @Override
      public void run() {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        MongoDatabase db = mongoClient.getDatabase("tech_db").withCodecRegistry(codecRegistry);
        MongoCollection<Employee> employees = db.getCollection("employees", Employee.class);
        ChangeStreamIterable<Employee> changeStream = employees.watch();

        changeStream.forEach(e -> {
          processChangeEvent(e);
        });
      }
    };
    new Thread(thread).start();
  }

  private void processChangeEvent(ChangeStreamDocument<Employee> e) {
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
