package com.kazurayam.ks.gc

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import com.kazurayam.ks.gc.Database
import com.kazurayam.ks.testcase.TestCaseId
import com.kazurayam.ks.testcase.TextSearchResult
import com.kazurayam.ks.testcase.TextSearchResult.Builder

import groovy.json.JsonOutput

@RunWith(JUnit4.class)
public class DatabaseTest {

	private Database db
	private TestCaseId testCaseId
	
	@Before
	void setup() {
		db = new Database()
		testCaseId = new TestCaseId("main/TC1")
		TCTOReference reference = TCTOReferenceTest.createSampleInstance()
		db.put(testCaseId, reference)
	}

	@Test
	void test_keySet_size() {
		assertEquals(1, db.keySet().size())
	}

	@Test
	void test_containsKey() {
		assertTrue(db.containsKey(testCaseId))
	}

	@Test
	void test_get() {
		Set<TCTOReference> set = db.get(testCaseId)
		assertEquals(1, set.size())
	}

	@Test
	void test_toString() {
		println db.toString()
	}

	@Test
	void test_toJson() {
		println JsonOutput.prettyPrint(db.toJson())
	}
}