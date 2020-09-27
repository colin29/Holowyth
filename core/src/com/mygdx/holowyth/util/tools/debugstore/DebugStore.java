package com.mygdx.holowyth.util.tools.debugstore;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugStore {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Map<String, DebugValues> store = new TreeMap<>();

	public Map<String, DebugValues> getStore() {
		return store;
	}

	/** When a component is registered twice, the old entries under that name are removed **/
	public DebugValues registerComponent(String name) {
		if (store.containsKey(name)) {
			logger.info("Replacing previously registered component '{}'", name);
			store.get(name).clear();
			return store.get(name);
		} else {
			DebugValues a = new DebugValues();
			store.put(name, a);
			return a;
		}
	}

}
