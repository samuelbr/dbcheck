package com.github.samuelbr.dbcheck;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Maps;

public class ResultInfo {

	private static final Interner<String> MAP_KEYS_INTERNER = Interners.newWeakInterner();

	private final Map<String, Object> result;
	private final List<String> tags;
	private final long timestamp;
	
	private ResultInfo(Map<String, Object> result, List<String> tags, long timestamp) {
		this.result = result;
		this.tags = tags;
		this.timestamp = timestamp;
	}

	public Map<String, Object> getResult() {
		return result;
	}
	
	public List<String> getTags() {
		return Collections.unmodifiableList(tags);
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public static ResultInfo create(Map<String, Object> result, List<String> tags, long timestamp) {
		Map<String, Object> newMap = Maps.newHashMapWithExpectedSize(result.size());
		
		for (Entry<String, Object> entry: result.entrySet()) {
			
			Object value = entry.getValue();
			
			newMap.put(
				MAP_KEYS_INTERNER.intern(entry.getKey()),
				value instanceof String 
					? MAP_KEYS_INTERNER.intern((String) value)
					: value);
		}
		
		return new ResultInfo(newMap, tags ,timestamp);
	}
	
	
}
