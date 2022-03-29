package com.tech.mongo.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
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
import org.springframework.util.CollectionUtils;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;

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
        List<Auditable> auditables = findAuditables();
        Map<String, List<ChangeEventProcessor>> changeEventProcessorsMap = collectChangeEventProcessors(auditables);

        // Registering to MongoDB Change Stream
        Runnable thread = new Runnable() {
          @Override
          public void run() {
            CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
            MongoDatabase db = mongoClient.getDatabase("tech_db").withCodecRegistry(codecRegistry);

            List<Bson> filters = auditables.stream().map(auditable -> auditable.collectionName()).distinct().map(coll -> Filters.eq("ns.coll", coll)).collect(Collectors.toList());
            List<Bson> pipeline = Collections.singletonList(Aggregates.match(Filters.or(filters)));

            db.watch(pipeline).cursor().forEachRemaining(e -> {
              List<ChangeEventProcessor> changeEventProcessors = changeEventProcessorsMap.get(e.getNamespace().getCollectionName().toLowerCase());
              if (CollectionUtils.isEmpty(changeEventProcessors)) {
                simpleChangeEventProcessor.processChangeEvent(e);
              }
              else {
                changeEventProcessors.forEach(changeEventProcessor -> changeEventProcessor.processChangeEvent(e));
              }
            });
          }
        };
        new Thread(thread).start();
      }
      catch (Exception e) {
        LOGGER.error("Exception occured while registering the listeners", e);
      }
    }
  }

  private List<Auditable> findAuditables() throws Exception {
    List<Auditable> auditables = new ArrayList<>();

    ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(Auditable.class));
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    scanner.setResourceLoader(new PathMatchingResourcePatternResolver(contextClassLoader));
    for (BeanDefinition definition : scanner.findCandidateComponents("com.tech")) {
      Class<?> beanClass = contextClassLoader.loadClass(definition.getBeanClassName());
      Auditable auditableAnnotation = beanClass.getAnnotation(Auditable.class);
      LOGGER.info("Class {} has Auditable annotation with collection name {} and change event processor {}", beanClass.getName(), auditableAnnotation.collectionName(),
        auditableAnnotation.changeEventProcessor());
      auditables.add(auditableAnnotation);
    }

    return auditables;
  }

  private Map<String, List<ChangeEventProcessor>> collectChangeEventProcessors(List<Auditable> auditables) throws Exception {
    Map<String, List<ChangeEventProcessor>> changeEventProcessorsMap = new HashMap<>();

    auditables.forEach(auditable -> {
      ChangeEventProcessor changeEventProcessor = applicationContext.getBean(auditable.changeEventProcessor());
      if (changeEventProcessorsMap.containsKey(auditable.collectionName().toLowerCase())) {
        changeEventProcessorsMap.get(auditable.collectionName().toLowerCase()).add(changeEventProcessor);
      }
      else {
        List<ChangeEventProcessor> changeEventProcessors = new ArrayList<>();
        changeEventProcessors.add(changeEventProcessor);
        changeEventProcessorsMap.put(auditable.collectionName().toLowerCase(), changeEventProcessors);
      }
    });

    return changeEventProcessorsMap;
  }
}
