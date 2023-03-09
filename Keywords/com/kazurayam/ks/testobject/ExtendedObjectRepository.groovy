package com.kazurayam.ks.testobject

import com.kms.katalon.core.testobject.ObjectRepository
import com.kms.katalon.core.testobject.TestObject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import com.kms.katalon.core.testobject.SelectorMethod

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput

public class ExtendedObjectRepository {

	private static Logger logger = LoggerFactory.getLogger(ExtendedObjectRepository.class)

	private Path baseDir
	private String subpath = null

	ExtendedObjectRepository() {
		this(Paths.get(".").resolve("Object Repository"), null)
	}

	ExtendedObjectRepository(Path baseDir) {
		this(baseDir, null)
	}

	ExtendedObjectRepository(Path baseDir, String subpath) {
		Objects.requireNonNull(baseDir)
		assert Files.exists(baseDir)
		this.baseDir = baseDir
		this.subpath = subpath
	}

	Path getTargetDir() {
		return (subpath != null) ? baseDir.resolve("subpath") : baseDir
	}

	String list(String pattern, Boolean isRegex) throws IOException {
		List<TestObjectId> list = listRaw(pattern, isRegex)
		StringBuilder sb = new StringBuilder()
		sb.append("[")
		String sep = ""
		list.forEach { toi ->
			sb.append(sep)
			sb.append(toi.toJson())
			sep = ","
		}
		sb.append("]")
		return JsonOutput.prettyPrint(sb.toString())
	}

	List<TestObjectId> listRaw(String pattern, Boolean isRegex) throws IOException {
		Path dir = getTargetDir()
		ObjectRepositoryVisitor visitor = new ObjectRepositoryVisitor(baseDir)
		Files.walkFileTree(dir, visitor)
		List<TestObjectId> ids = visitor.getTestObjectIdList()
		//
		List<TestObjectId> result = new ArrayList<>()
		BilingualMatcher bim = new BilingualMatcher(pattern, isRegex)
		ids.forEach { testObjectId ->
			if (bim.found(testObjectId.value().toString())) {
				result.add(testObjectId)
			}
		}
		return result;
	}

	String listGist(String pattern, Boolean isRegex, Boolean requirePrettyPrint = false) throws IOException {
		List<TestObjectGist> result = this.listGistRaw(pattern, isRegex)
		StringBuilder sb = new StringBuilder()
		sb.append("[")
		String sep = ""
		result.forEach { tog ->
			sb.append(sep)
			sb.append(tog.toJson())
			sep = ","
		}
		sb.append("]")
		if (requirePrettyPrint) {
			return JsonOutput.prettyPrint(sb.toString())
		} else {
			return sb.toString()
		}
	}

	List<TestObjectGist> listGistRaw(String pattern, Boolean isRegex) throws IOException {
		Path dir = getTargetDir()
		ObjectRepositoryVisitor visitor = new ObjectRepositoryVisitor(baseDir)
		Files.walkFileTree(dir, visitor)
		List<TestObjectId> ids = visitor.getTestObjectIdList()
		BilingualMatcher bim = new BilingualMatcher(pattern, isRegex)
		//
		List<TestObjectGist> result = new ArrayList<>()
		ids.forEach { id ->
			TestObject tObj = ObjectRepository.findTestObject(id.value())
			Locator locator = findLocator(id)
			if (bim.found(id.value())) {
				TestObjectGist gist =
						new TestObjectGist(id, tObj.getSelectorMethod().toString(), locator)
				result.add(gist)
			}
		}
		return result
	}

	//-------------------------------------------------------------------------


	Map<Locator, Set<TestObjectGist>> reverseLookupRaw(String pattern, Boolean isRegex) throws IOException {
		Map<Locator, Set<TestObjectGist>> result = new TreeMap<>()
		BilingualMatcher bim = new BilingualMatcher(pattern, isRegex)
		List<TestObjectId> idList = this.listRaw("", false)  // list of IDs of Test Object
		idList.forEach { id ->
			Locator locator = findLocator(id)
			Set<TestObjectId> idSet
			if (result.containsKey(locator)) {
				idSet = result.get(locator)
			} else {
				idSet = new TreeSet<>()
			}
			if (bim.found(locator.value())) {
				TestObjectGist gist = findGist(id)
				idSet.add(gist)
				result.put(locator, idSet)
			}
		}
		return result
	}

	String reverseLookup(String pattern, Boolean isRegex) throws IOException {
		Map<Locator, Set<TestObjectGist>> result = this.reverseLookupRaw(pattern, isRegex)
		String json = JsonOutput.toJson(result)
		return JsonOutput.prettyPrint(json)
	}

	//-------------------------------------------------------------------------

	private Locator findLocator(TestObjectId testObjectId) {
		Objects.requireNonNull(testObjectId)
		TestObjectGist gist = findGist(testObjectId)
		return gist.locator()
	}

	private TestObjectGist findGist(TestObjectId testObjectId) {
		Objects.requireNonNull(TestObjectId)
		TestObject tObj = ObjectRepository.findTestObject(testObjectId.value())
		SelectorMethod selectorMethod = tObj.getSelectorMethod()
		Locator locator = new Locator(tObj.getSelectorCollection().getAt(selectorMethod))
		TestObjectGist gist = new TestObjectGist(testObjectId,
				selectorMethod.toString(),
				locator)
		return gist
	}

	//-------------------------------------------------------------------------

	public Set<TestObjectId> getAllTestObjectIds() {
		Set<TestObjectId> result = new TreeSet<>()
		List<TestObjectGist> allGist = listGistRaw("", false)
		allGist.forEach { gist ->
			TestObjectId toi = gist.testObjectId()
			if (toi != null && toi.value() != "") {
				result.add(toi)
			}
		}
		return result
	}

}
