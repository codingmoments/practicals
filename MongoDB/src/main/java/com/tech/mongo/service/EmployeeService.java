package com.tech.mongo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tech.mongo.domain.Employee;
import com.tech.mongo.repository.EmployeeRepository;

@Service
public class EmployeeService {

  @Autowired
  private EmployeeRepository employeeRepository;

  public Employee createEmployee(Employee employee) {
    return employeeRepository.save(employee);
  }

  public List<Employee> getAllEmployees() {
    return employeeRepository.findAll();
  }

  public Employee findEmployeeByEmail(String email) {
    return employeeRepository.findEmployeeByEmail(email);
  }

  public List<Employee> findEmployeesByLastName(String lastName) {
    return employeeRepository.findEmployeesByLastName(lastName);
  }

  public Employee updateEmployee(Employee employee) {
    return employeeRepository.save(employee);
  }

  public void deleteEmployee(String id) {
    employeeRepository.deleteById(id);
  }
}
