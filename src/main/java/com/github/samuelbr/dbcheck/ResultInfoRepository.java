package com.github.samuelbr.dbcheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ResultInfoRepository {

	private static final Logger LOG = LoggerFactory.getLogger(ResultInfoRepository.class); 
	
	private final Object monitorLock = new Object();
	
	private List<ResultInfo> resultsList = Lists.newArrayList();
	
	public void add(Collection<ResultInfo> resultInfos) {
		synchronized (monitorLock) {
			resultsList.addAll(resultInfos);
			
			LOG.info("Collected {} result infos", resultsList.size());
		}
		
	}
	
	public List<ResultInfo> get() {
		synchronized (monitorLock) {
			ArrayList<ResultInfo> copy = Lists.newArrayList(resultsList);
			return copy;
		}
	}
	
	public List<ResultInfo> clearAndGet() {
		synchronized (monitorLock) {
			ArrayList<ResultInfo> copy = Lists.newArrayList(resultsList);
			resultsList.clear();
			
			return copy;
		}
	}
		
}
