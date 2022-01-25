package com.warneriveris.jsonArrayToObj.services.read;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;

import com.google.gson.stream.JsonReader;
import com.warneriveris.jsonArrayToObj.errors.ErrorReporter;
import com.warneriveris.jsonArrayToObj.process.ReadPOJOQueue;
import com.warneriveris.jsonArrayToObj.validators.injectors.URLValidatorServiceInjector;
import com.warneriveris.jsonArrayToObj.validators.services.ValidatorService;

public class ReadStreamServiceImpl implements ReadService {

	private JsonReader reader;
	private ValidatorService<String> isURL = new URLValidatorServiceInjector().getValidator();
	
	@Override
	public ReadService getReader(String filename) {
		if(isURL.isValid(filename)) {
			createURLStream(filename);
		} else {
			createFileStream(filename);
		}
		try {
			reader.beginArray();
		} catch (IOException e) {
			readFileError(e);
		}
		return this;
	}


	private void createFileStream(String filename) {
		FileInputStream inputFile = null;
		try { inputFile = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			openFileError(e);
			System.exit(1);
		}
		reader = new JsonReader(new InputStreamReader(inputFile, StandardCharsets.UTF_8));
	}


	private void createURLStream(String url) {
		InputStream in = null;
		try {
			in = new URL(url).openStream();
		} catch (MalformedURLException e) {
			openFileError(e);
		} catch (IOException e) {
			openFileError(e);
		}
		reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		
	}


	@Override
	public boolean hasNext() {
		boolean more = false;
		try {
			more = reader.hasNext();
		} catch (IOException e) {
			readFileError(e);
		}
		return more;
	}

	@Override
	public Object next(Object type) {
		String URL = null;
		String path = null;
		int size = 0;
		try {
			reader.beginObject();
			while(reader.hasNext()) {
				String field = reader.nextName().toLowerCase();
				switch(field) 
				{
				case "url":
					URL = reader.nextString();
					break;
				case "path":
					path = reader.nextString();
					break;
				case "size":
					size = reader.nextInt();
					break;
				default:
					reader.skipValue();
				}
			}
			reader.endObject();
		} catch (IOException e) {
			readFileError(e);
			closeReader();
		}
		return new ReadPOJO(URL, path, size);
	}

	@Override
	public void closeReader() {
		try {
			reader.endArray();
			reader.close();
			ReadPOJOQueue.setIsReceivingInput(false);
		} catch (IOException e) {
			String msg = "Error closing input file reader";
			ErrorReporter.add(msg);
			LogManager.getLogger().error(msg, e);
			System.exit(1);
		} catch (IllegalStateException e) {
			String msg = "Error closing input file reader";
			ErrorReporter.add(msg);
			LogManager.getLogger().error(msg, e);
			System.exit(1);
		}
	}
	
	private void readFileError(Exception e) {
		String msg = "Error reading file";
		ErrorReporter.add(msg);
		LogManager.getLogger().error(msg, e);
	}
	
	private void openFileError(Exception e) {
		String msg = "Error opening input source";
		ErrorReporter.add(msg);
		LogManager.getLogger().error(msg, e);
	}
}
