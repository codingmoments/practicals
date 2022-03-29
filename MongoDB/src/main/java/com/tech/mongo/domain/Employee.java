package com.tech.mongo.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tech.mongo.audit.Auditable;
import com.tech.mongo.processor.EmployeeChangeEventProcessor;

/**
 * Domain class for employees
 */
@Document("employees")
@Auditable(collectionName = "employees", changeEventProcessor = EmployeeChangeEventProcessor.class)
public class Employee {

  @Id
  @BsonRepresentation(BsonType.OBJECT_ID)
  private String id;
  private String firstName;
  private String lastName;
  private String email;
  private LocalDate birthDate;
  private Integer salary;
  private Double performanceRating;
  private boolean isActive;

  @LastModifiedBy
  private String lastModifiedBy;
  @LastModifiedDate
  private LocalDateTime lastModified;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public LocalDateTime getLastModified() {
    return lastModified;
  }

  public void setLastModified(LocalDateTime lastModified) {
    this.lastModified = lastModified;
  }

  public Integer getSalary() {
    return salary;
  }

  public void setSalary(Integer salary) {
    this.salary = salary;
  }

  public Double getPerformanceRating() {
    return performanceRating;
  }

  public void setPerformanceRating(Double performanceRating) {
    this.performanceRating = performanceRating;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }
}
