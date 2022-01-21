package com.tech.google.storage.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.tech.google.storage.aop.LogExecutionTime;
import com.tech.google.storage.config.ApplicationProperties;
import com.tech.google.storage.domain.Message;

@Service
public class StorageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

	private final Storage storage;
	private final ApplicationProperties applicationProperties;

	public StorageService(Storage storage, ApplicationProperties applicationProperties) {
		this.storage = storage;
		this.applicationProperties = applicationProperties;
	}

	/**
	 * Stores the given object into the Cloud Storage.
	 * 
	 * @param objectName - The name of the object
	 * @param text       - The text content of the object
	 * @throws Exception
	 */
	@LogExecutionTime
	public void storeObject(Message message) throws Exception {
		String attachmentName = message.getAttachmentName();
		String messageContent = message.getMessageContent();

		LOGGER.info("Storing object with name - {} ...", attachmentName);

		Map<String, String> metadata = new HashMap<>();
		metadata.put("name", attachmentName);

		BlobId blobId = BlobId.of(applicationProperties.getBucketName(), message.getMessageId().toString());
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setMetadata(metadata).setContentType(message.getContentType())
				.build();
		storage.create(blobInfo, messageContent.getBytes("UTF-8"));

		LOGGER.info("Object with name - {} is stored successfully!", attachmentName);
	}

	/**
	 * Gets the names of all the objects in the Cloud Storage.
	 * 
	 * This does not fetch the contents of data object, it just fetches the attributes.
	 * 
	 * @return - List of object names
	 */
	@LogExecutionTime
	public List<String> getAllObjectNames() {
		LOGGER.info("Getting the names of all objects ...");

		Page<Blob> blogPage = storage.list(applicationProperties.getBucketName());
		Iterator<Blob> blobIterator = blogPage.iterateAll().iterator();

		List<String> objectNames = new ArrayList<>();
		while (blobIterator.hasNext()) {
			Blob blob = blobIterator.next();
			objectNames.add(blob.getName() + "-" + blob.getMetadata().get("name"));
		}

		LOGGER.info("Got the names of all objects!");
		return objectNames;
	}

	/**
	 * Gets the contents of of the given object name from the Cloud Storage.
	 * 
	 * @param objectName - The name of the object
	 * @return - The text content of the object
	 */
	@LogExecutionTime
	public String getObjectContent(String objectName) {
		LOGGER.info("Getting the content for the object {} ...", objectName);

		BlobId blobId = BlobId.of(applicationProperties.getBucketName(), objectName);
		Blob blob = storage.get(blobId);

		LOGGER.info("Got the content for the object {} successfully!", objectName);
		return new String(blob.getContent(), StandardCharsets.UTF_8);
	}

}
