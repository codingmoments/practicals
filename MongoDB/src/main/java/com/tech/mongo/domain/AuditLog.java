package com.tech.mongo.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.client.model.changestream.OperationType;

/**
 * Domain class for departments
 */
@Document("auditLog")
public class AuditLog {

  @Id
  private String id;
  private LocalDateTime eventTime;
  private String collectionName;
  private String documentId;
  private Long employeeId;
  private OperationType operationType;
  private Object document;
  private String fieldName;
  private String oldValue;
  private String newValue;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public LocalDateTime getEventTime() {
    return eventTime;
  }

  public void setEventTime(LocalDateTime eventTime) {
    this.eventTime = eventTime;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public void setCollectionName(String collectionName) {
    this.collectionName = collectionName;
  }

  public String getDocumentId() {
    return documentId;
  }

  public void setDocumentId(String documentId) {
    this.documentId = documentId;
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(Long employeeId) {
    this.employeeId = employeeId;
  }

  public OperationType getOperationType() {
    return operationType;
  }

  public void setOperationType(OperationType operationType) {
    this.operationType = operationType;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getOldValue() {
    return oldValue;
  }

  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  public String getNewValue() {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public Object getDocument() {
    return document;
  }

  public void setDocument(Object document) {
    this.document = document;
  }

}
