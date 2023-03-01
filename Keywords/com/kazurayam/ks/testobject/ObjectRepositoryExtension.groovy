package com.kazurayam.ks.testobject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.testobject.ObjectRepository
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.SelectorMethod
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.util.regex.Matcher
import java.util.regex.Pattern
import groovy.json.JsonOutput

/**
 * This class extends the `com.kms.katalon.core.testobject.ObjectRepository` class and
 * add some useful methods on the fly by Groovy's Meta-programming technique.
 * 
 * @author kazurayam
 */
public class ObjectRepositoryExtension {

	private ObjectRepositoryExtension() {}

	@Keyword
	static void apply() {
		ObjectRepository.metaClass.static.invokeMethod = { String name, args ->
			switch (name) {
				case "list" :
					return this.list(args)
					break
				case "reverseLookup" :
					return this.reverseLookup(args)
					break
				case "reverseLookupAsJson" :
					return this.reverseLookupAsJson(args)
					break
				default :
				// just do what ObejctRepository is designed to do
					def result
					try {
						result = delegate.metaClass.getMetaMethod(name, args).invoke(delegate, args)
					} catch (Exception e) {
						System.err.println("call to method $name raised an Exception")
						e.printStackTrace()
					}
					return result
			}
		}
	}

	/*
	 * 
	 */
	static List<String> list(Object ... args) throws Exception {
		if (args.length == 0) {
			return this.doList("", false)
		} else if (args.length == 1) {
			return this.doList((String)args[0], false)
		} else {
			return this.doList((String)args[0], (Boolean)args[1])
		}
	}

	private static List<String> doList(String pattern, Boolean isRegex) throws IOException {
		Path dir = Paths.get("./Object Repository")
		ObjectRepositoryVisitor visitor = new ObjectRepositoryVisitor(dir)
		Files.walkFileTree(dir, visitor)
		List<String> ids = visitor.getTestObjectIDs()
		//
		List<String> result = new ArrayList<>()
		BiMatcher bim = new BiMatcher(pattern, isRegex)
		ids.forEach { id ->
			if (bim.matches(id)) {
				result.add(id)
			}
		}
		return result;
	}


	static Map<String, Set<String>> reverseLookup(Object ... args) throws IOException {
		if (args.length == 0) {
			return this.doReverseLookup("", false)
		} else if (args.length == 1) {
			return this.doReverseLookup((String)args[0], false)
		} else {
			return this.doReverseLookup((String)args[0], (Boolean)args[1])
		}
	}

	private static Map<String, Set<String>> doReverseLookup(String pattern, Boolean isRegex) throws IOException {
		Map<String, Set<String>> result = new TreeMap<>()
		BiMatcher bim = new BiMatcher(pattern, isRegex)
		List<String> idList = this.list()  // list of IDs of Test Object
		idList.forEach { id ->
			String locator = findSelector(id) // get the locator contained in the Test Object
			Set<String> idSet
			if (result.containsKey(locator)) {
				idSet = result.get(locator)
			} else {
				idSet = new TreeSet<>()
			}
			if (bim.matches(locator)) {
				idSet.add(id)
				result.put(locator, idSet)
			}
		}
		return result
	}

	static String reverseLookupAsJson(Object ... args) throws IOException {
		if (args.length == 0) {
			return this.doReverseLookupAsJson("", false)
		} else if (args.length == 1) {
			return this.doReverseLookupAsJson((String)args[0], false)
		} else {
			return this.doReverseLookupAsJson((String)args[0], (Boolean)args[1])
		}
	}

	private static doReverseLookupAsJson(String pattern, Boolean isRegex) throws IOException {
		Map<String, Set<String>> result = this.reverseLookup(pattern, isRegex)
		String json = JsonOutput.toJson(result)
		String pp = JsonOutput.prettyPrint(json)
		return pp
	}

	private static String findSelector(String testObjectId) {
		Objects.requireNonNull(testObjectId)
		TestObject tObj = ObjectRepository.findTestObject(testObjectId)
		SelectorMethod selectorMethod = tObj.getSelectorMethod()
		return tObj.getSelectorCollection().getAt(selectorMethod)
	}

}

