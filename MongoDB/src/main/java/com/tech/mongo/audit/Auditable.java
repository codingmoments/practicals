package com.tech.mongo.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Auditable {
  String collectionName();

  Class<? extends ChangeEventProcessor> changeEventProcessor() default SimpleChangeEventProcessor.class;
}