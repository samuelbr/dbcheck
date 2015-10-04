package com.github.samuelbr.dbcheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public class ResultInfoRepository {

	private static final Logger LOG = LoggerFactory.getLogger(ResultInfoRepository.class);
	
	private static final String RESULT_INFO_DROP_THRESHOLD = "resultInfo.dropThreshold";
	
	private static final int DEFAULT_RESULT_INFO_DROP_THRESHOLD = 1000*1000;
	
	private final Object monitorLock = new Object();
	
	private List<ResultInfo> resultsList = Lists.newArrayList();
	
	private int dropThreshold;
	
	public ResultInfoRepository(Properties properties) {
		Preconditions.checkArgument(properties != null);
		
		String value = properties.getProperty(RESULT_INFO_DROP_THRESHOLD);
		
		dropThreshold = MoreObjects.firstNonNull(
				Ints.tryParse(value),
				DEFAULT_RESULT_INFO_DROP_THRESHOLD);

		if (dropThreshold < 1) {
			LOG.error("Invalid value {} for dropTreshold", dropThreshold);
			dropThreshold = DEFAULT_RESULT_INFO_DROP_THRESHOLD;
		}
		
	}

	public void add(Collection<ResultInfo> resultInfos) {
		synchronized (monitorLock) {
			
			if (resultsList.size() > dropThreshold) {
				resultsList.clear();
				LOG.error("Result lists size is bigger than threshold - clear it");
			}
			
			resultsList.addAll(resultInfos);
			
			LOG.trace("Collected {} result infos", resultsList.size());
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
