package com.kazurayam.ks

import static org.junit.Assert.*

import java.nio.file.Path
import java.nio.file.Paths

import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import com.kms.katalon.core.testobject.ObjectRepository
import com.kms.katalon.core.testobject.TestObject

@RunWith(JUnit4.class)
class ObjectRepositoryExtensionTest {

	private static final Path objectRepository = Paths.get("./Object Repository")

	@BeforeClass
	static void beforeClass() {
		ObjectRepositoryExtension ext = new ObjectRepositoryExtension()
		ext.apply()
	}

	@Test
	void testList() {
		List<String> list = ObjectRepository.list()
		assertTrue(list.size() > 0)
		println "********** testList() **********"
		list.forEach { p ->
			println p
		}
	}

	@Test
	void testListWithArgs() {
		List<String> list = ObjectRepository.list("button_", false)
		assertTrue(list.size() == 4)
		println "********** testListWithArgs() **********"
		list.forEach { p ->
			println p
		}
	}

	@Test
	void testXref() {
		Map<String, Set<String>> result = ObjectRepository.xref()
		assertEquals(12, result.size())
	}

	@Test
	void testXrefWithArgs() {
		Map<String, Set<String>> result = ObjectRepository.xref("btn-.+-appointment", true)
		assertEquals(2, result.size())
		//
		result = ObjectRepository.xref("select", false)
		assertEquals(2, result.size())
	}

	@Test
	void testXrefAsJson() {
		String xref = ObjectRepository.xrefAsJson()
		assertNotNull(xref)
		println "********** testXrefAsJson() **********"
		println xref
	}

	@Test
	void testXrefAsJsonWithArgs() {
		String xref = ObjectRepository.xrefAsJson("//a[@id")   // default: isRegex = false
		assertNotNull(xref)
		println "********** testXrefAsJsonWithArg() **********"
		println xref
	}

	/**
	 * test if ObjectRepository's ordinary method works
	 */
	@Test
	void testFindTestObject() {
		TestObject tObj = ObjectRepository.findTestObject("Page_CURA Healthcare Service/button_Login")
		assertNotNull(tObj)
	}
}