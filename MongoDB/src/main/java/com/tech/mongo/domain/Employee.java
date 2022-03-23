package com.tech.mongo.domain;

import java.time.LocalDate;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Domain class for employees
 */
@Document("employees")
public class Employee {

  @Id
  @BsonRepresentation(BsonType.OBJECT_ID)
  private String id;
  private String firstName;
  private String lastName;
  private String email;
  private LocalDate birthDate;

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
}
