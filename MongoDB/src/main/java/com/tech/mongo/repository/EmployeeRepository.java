package com.tech.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.tech.mongo.domain.Employee;

public interface EmployeeRepository extends MongoRepository<Employee, String> {

  @Query("{email: '?0'}")
  Employee findEmployeeByEmail(String email);

  @Query(value = "{lastName: '?0'}", fields = "{'firstName' : 1, 'lastName' : 1}")
  List<Employee> findEmployeesByLastName(String lastName);
}
