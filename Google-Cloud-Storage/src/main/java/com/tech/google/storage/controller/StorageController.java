package com.tech.google.storage.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tech.google.storage.domain.Message;
import com.tech.google.storage.service.StorageService;

@RestController
@RequestMapping("/storage")
public class StorageController {

	private final StorageService storageService;

	public StorageController(StorageService storageService) {
		this.storageService = storageService;
	}

	@PostMapping
	public void storeObject(@RequestBody Message message) throws Exception {
		storageService.storeObject(message);
	}

	@GetMapping
	public List<String> getAllObjectNames() {
		return storageService.getAllObjectNames();
	}

	@GetMapping("/{objectName}")
	public String getObjectContent(@PathVariable String objectName) {
		return storageService.getObjectContent(objectName);
	}
}
