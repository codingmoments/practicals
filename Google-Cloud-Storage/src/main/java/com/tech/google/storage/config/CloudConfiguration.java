package com.tech.google.storage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Configuration
public class CloudConfiguration {

	private final ApplicationProperties applicationProperties;

	public CloudConfiguration(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	@Bean
	public Storage getGoogleCloudStorage() {
		return StorageOptions.newBuilder().setProjectId(applicationProperties.getProjectId()).build().getService();
	}
}
