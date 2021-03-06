package com.warneriveris.jsonArrayToObj.read;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.warneriveris.jsonArrayToObj.process.ReadPOJOQueue;
import com.warneriveris.jsonArrayToObj.services.read.ReadPOJO;
import com.warneriveris.jsonArrayToObj.services.read.ReadStreamServiceImpl;

import org.junit.jupiter.api.MethodOrderer;

/**
 * SUT: {@link ReadStreamServiceImpl}
 * 
 * @author Warner Iveris
 *
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReadStreamServiceTest {

	private static ReadStreamServiceImpl rs = new ReadStreamServiceImpl();
	private static String inputName = "src/test/resources/data/read/normalUnprocessedData.json";
	private static String inputURLName = "https://raw.githubusercontent.com/SuperWarnerMan/json/main/src/test/resources/data/read/normalUnprocessedData.json";
	private static ReadPOJO rp;
	private static Map<String, WriteObj> POJOMap = new HashMap<>();

	@Test
	@Order(1)
	void testNormalDataResultSize() {
		rs.getReader(inputName);
		ReadPOJO rp;
		while (rs.hasNext()) {
			rp = rs.next();
			ReadPOJOQueue.add(rp);
		}
		int expected = 2;
		int actual = ReadPOJOQueue.getSize();
		assertEquals(expected, actual);
	}

	@Test
	@Order(2)
	void testNormalDataResultAccuracy() {
		while (ReadPOJOQueue.getSize() > 0) {
			rp = ReadPOJOQueue.remove();
			POJOMap.put(rp.getPath(), new WriteObj(rp.getUrl(), rp.getSize()));
		}
		assertEquals(new WriteObj("http://www.lqe.com/tya", 164), POJOMap.get("itkbt"));
		assertEquals(new WriteObj("http://www.lnn.com/usl", 233), POJOMap.get("frh"));
	}

	@Test
	@Order(3)
	void testNormalDataResultAccuracyURL() {
		rs.getReader(inputURLName);
		ReadPOJO rp;
		while (rs.hasNext()) {
			rp = rs.next();
			ReadPOJOQueue.add(rp);
		}
		while (ReadPOJOQueue.getSize() > 0) {
			rp = ReadPOJOQueue.remove();
			POJOMap.put(rp.getPath(), new WriteObj(rp.getUrl(), rp.getSize()));
		}
		assertEquals(new WriteObj("http://www.lqe.com/tya", 164), POJOMap.get("itkbt"));
		assertEquals(new WriteObj("http://www.lnn.com/usl", 233), POJOMap.get("frh"));
	}

	@Test
	@Order(4)
	void testDataWithExtraFields() throws IOException {
		String xtraData = "./src/test/resources/data/read/xtraFields.json";
		// ignores extra fields and only enters predefined fields
		rs.getReader(xtraData);
		while (rs.hasNext()) {
			rp = rs.next();
			POJOMap.put(rp.getPath(), new WriteObj(rp.getUrl(), rp.getSize()));
		}
		assertEquals(new WriteObj("http://www.lqe.com/tya", 164), POJOMap.get("itkbt"));
		assertEquals(new WriteObj("http://www.lnn.com/usl", 233), POJOMap.get("frh"));
		POJOMap.clear();
	}

	@Test
	@Order(5)
	void testNonExistentFile() {
		// Make sure queue is empty, then create reader with a non-existent
		// file and test that it instantiated, that it closed, and that
		// nothing was written to the queue
		while (ReadPOJOQueue.getSize() > 0) {
			ReadPOJOQueue.remove();
		}
		Thread noFileHere = new Thread(() -> {
			rs.getReader("invalidFile.json");
			assertTrue(ReadPOJOQueue.getIsReceivingInput());
		});
		noFileHere.start();
		assertEquals(0, ReadPOJOQueue.getSize());
		assertFalse(ReadPOJOQueue.getIsReceivingInput());
	}

	@Test
	@Order(6)
	void testNonExistentURL() {
		// Make sure queue is empty, then create reader with a non-existent
		// URL and test that it instantiated, that it closed, and that
		// nothing was written to the queue
		while (ReadPOJOQueue.getSize() > 0) {
			ReadPOJOQueue.remove();
		}
		Thread noURLHere = new Thread(() -> {
			rs.getReader("http://www.github.com/SuperWarnerMan/invalidFile.json");
			assertTrue(ReadPOJOQueue.getIsReceivingInput());
		});
		noURLHere.start();
		assertEquals(0, ReadPOJOQueue.getSize());
		assertFalse(ReadPOJOQueue.getIsReceivingInput());
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		rs.closeReader();
	}
}

// the purpose of this class is to hold the URL and size values in a single 
// object for ease in testing the ReadPOJOQueue against the hash map. The hash map
// is a good approximation for how the read data will end up after being processed
// through write services into a single JSON object with path values used as keys
// to the URL and size values.
class WriteObj {
	private String url;
	private int size;

	public WriteObj(String url, int size) {
		this.url = url;
		this.size = size;
	}

	public String getUrl() {
		return url;
	}

	public int getSize() {
		return size;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		WriteObj p = (WriteObj) o;
		return Objects.equals(getUrl(), p.getUrl()) && Objects.equals(getSize(), p.getSize());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUrl(), getSize());
	}

	@Override
	public String toString() {
		return "URL: " + getUrl() + "\tSize: " + getSize() + "\n";
	}
}
