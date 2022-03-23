package com.tech.mongo;

import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
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
import com.tech.mongo.domain.Employee;

@EnableMongoRepositories
@SpringBootApplication
public class MongoApplication {

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
        changeStream.forEach((Consumer<ChangeStreamDocument<Employee>>) System.out::println);
      }
    };
    new Thread(thread).start();
  }
}
