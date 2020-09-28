package com.mygdx.holowyth.map;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;


/**
 * A simple Map class that uses names as keys and forbids null keys and values
 * (String name --> V value)
 * @author Colin
 *
 */
public class StringNonNullMap <V> {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Map<String, V> map = new LinkedHashMap<>();
	/**
	 * If entry doesn't exist, returns null
	 * @return
	 */
	public V get(String name){
		if(name==null) {
			throw new HoloIllegalArgumentsException("Can't query null name");
		}
		return map.get(name);
	}
	public boolean has(String name) {
		return get(name)!=null;
	}
	
	public boolean put(String name, V value) {
		if(name==null)
			throw new HoloIllegalArgumentsException("name can't be null");
		if(value==null)
			throw new HoloIllegalArgumentsException("value can't be null");
		
		boolean oldLocExisted = has(name);
		map.put(name, value);
		return oldLocExisted;
	}
	
	public Set<String> keySet(){
		return Collections.unmodifiableSet(map.keySet());
	}
	public Collection<V> values(){
		return Collections.unmodifiableCollection(map.values());
	}
}
