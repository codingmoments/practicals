package com.tech.mongo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tech.mongo.domain.Employee;
import com.tech.mongo.service.EmployeeService;

@RequestMapping("/employees")
@RestController
public class EmployeeController {

  @Autowired
  private EmployeeService employeeService;

  @PostMapping
  public Employee createEmployee(@RequestBody Employee employee) {
    return employeeService.createEmployee(employee);
  }

  @GetMapping
  public List<Employee> getAllEmployees() {
    return employeeService.getAllEmployees();
  }

  @PutMapping
  public Employee updateEmployee(@RequestBody Employee employee) {
    return employeeService.updateEmployee(employee);
  }

  @DeleteMapping("/{id}")
  public void deleteEmployee(@PathVariable String id) {
    employeeService.deleteEmployee(id);
  }
}
