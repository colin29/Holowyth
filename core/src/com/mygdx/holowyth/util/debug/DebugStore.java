package com.mygdx.holowyth.util.debug;

import java.util.Map;
import java.util.TreeMap;

public class DebugStore {

	private Map<String, DebugValues> store = new TreeMap<>();
	
	public Map<String, DebugValues> getStore(){
		return store;
	}
	public DebugValues registerComponent(String name){
		if(store.containsKey(name)){
			System.out.printf("Warning: Duplicate component registered%s%n", name);
			return store.get(name);
		}else {
			
			DebugValues a = new DebugValues();
			store.put(name, a);
			return a;
		}
	}
	
	
}
