package com.mygdx.holowyth.util.debug;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class DebugStore {

	private Map<String, ArrayList<DebugValue>> store = new TreeMap<>();
	
	public Map<String, ArrayList<DebugValue>> getStore(){
		return store;
	}
	public ArrayList<DebugValue> registerComponent(String name){
		if(store.containsKey(name)){
			System.out.printf("Warning: Duplicate component registered%s%n", name);
			return store.get(name);
		}else {
			ArrayList<DebugValue> a = new ArrayList<DebugValue>();
			store.put(name, a);
			return a;
		}
	}
	
	
}
