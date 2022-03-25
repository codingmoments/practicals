package com.tech.mongo.audit;

import java.util.ArrayList;
import java.util.List;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Class that listens to MongoDB ChangeStream
 * 
 */
@Component
public class ChangeEventListener implements ApplicationListener<ApplicationReadyEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChangeEventListener.class);

  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private MongoClient mongoClient;
  @Autowired
  private SimpleChangeEventProcessor simpleChangeEventProcessor;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    if (event.getApplicationContext().equals(this.applicationContext)) {

      try {
        List<Class<?>> auditables = findAuditables();

        // Registering to MongoDB Change Stream
        Runnable thread = new Runnable() {
          @Override
          public void run() {
            CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

            MongoDatabase db = mongoClient.getDatabase("tech_db").withCodecRegistry(codecRegistry);

            for (Class<?> auditable : auditables) {
              Auditable auditableAnnotation = auditable.getAnnotation(Auditable.class);
              MongoCollection<?> mongoCollection = db.getCollection(auditableAnnotation.collectionName(), auditable);
              ChangeStreamIterable<?> changeStream = mongoCollection.watch();

              changeStream.forEach(e -> {
                simpleChangeEventProcessor.processChangeEvent(e);
              });
            }
          }
        };
        new Thread(thread).start();

      }
      catch (Exception e) {
        LOGGER.error("Exception occured while registering the listeners", e);
      }
    }
  }

  private List<Class<?>> findAuditables() throws Exception {
    List<Class<?>> auditables = new ArrayList<>();

    ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(Auditable.class));
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    scanner.setResourceLoader(new PathMatchingResourcePatternResolver(contextClassLoader));
    for (BeanDefinition definition : scanner.findCandidateComponents("com.tech")) {
      Class<?> beanClass = contextClassLoader.loadClass(definition.getBeanClassName());
      Auditable auditableAnnotation = beanClass.getAnnotation(Auditable.class);
      LOGGER.info("Class {} has Auditable annotation with collection name {}", beanClass.getName(), auditableAnnotation.collectionName());
      auditables.add(beanClass);
    }

    return auditables;
  }

}
