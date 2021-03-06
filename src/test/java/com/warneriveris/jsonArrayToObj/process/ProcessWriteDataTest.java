package com.warneriveris.jsonArrayToObj.process;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.warneriveris.jsonArrayToObj.services.read.ReadPOJO;

/**
 * SUT: {@link ProcessWriteData}
 * 
 * @author Warner Iveris
 *
 */

class ProcessWriteDataTest {

	private static ProcessData pd;
	private static String actual = "src/test/resources/data/temporary/processWriteDataTest.json";
	private static String expected = "src/test/resources/data/write/processedDataTest.json";
	private static File file = new File(actual);
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		ReadPOJO rp = new ReadPOJO("http://www.lqe.com/tya", "itkbt", 164);
		ReadPOJOQueue.add(rp);
		rp = new ReadPOJO("http://www.lnn.com/usl", "frh", 233);
		ReadPOJOQueue.add(rp);
		
		pd = new ProcessWriteData(actual);
		Thread t = new Thread(pd);
		t.start();
		ReadPOJOQueue.setIsReceivingInput(false);
		Thread.sleep(10);
	}

	// The same data values in the test file are processed as strings in the setup
	// and the resulting file sizes are compared to ensure that the finished 
	// output file matches the test file.
	@Test
	void testFileSizes() throws Exception {
		Path actualFile = Paths.get(actual);
		Path expectedFile = Paths.get(expected);
		long actualSize = 0;
		long expectedSize = 0;
		try {
			actualSize = Files.size(actualFile);
			expectedSize = Files.size(expectedFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(expectedSize, actualSize);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		file.delete();
	}
}
