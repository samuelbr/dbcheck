package com.github.samuelbr.dbcheck;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.samuelbr.dbcheck.SourceInfoRepository.SourceInfo;
import com.google.common.base.Preconditions;

public class DbCheckPoolerJob extends Thread {

	private static final Logger LOG = LoggerFactory.getLogger(DbCheckPoolerJob.class);
	
	private static final long DEFAULT_INTERVAL = 10000;
	
	private final DbExecutor dbExecutor;
	
	private final SourceInfoRepository sourceInfoRepository;
	
	private final ResultInfoRepository resultInfoRepository;
	
	private volatile boolean run = true;
	
	private long interval = DEFAULT_INTERVAL;

	public DbCheckPoolerJob(
			DbExecutor dbExecutor, 
			SourceInfoRepository sourceInfoRepository,
			ResultInfoRepository resultInfoRepository) {
		this.dbExecutor = dbExecutor;
		this.sourceInfoRepository = sourceInfoRepository;
		this.resultInfoRepository = resultInfoRepository;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		Preconditions.checkArgument(interval > 0);
		
		this.interval = interval;
	}

	@Override
	public void run() {
		LOG.info("Start thread");
		
		do {
			long currentTime = System.currentTimeMillis();
			
			long delta = interval - ( currentTime % interval);
			
			try {
				Thread.sleep(delta);
			} catch (InterruptedException e) {
				LOG.error("Thread interrupted", e);
			}
			
			doWork();
		} while (run);
		
		LOG.info("Finish thread");
	}
	
	private void doWork() {
		long timestamp = System.currentTimeMillis();
		timestamp -= (timestamp % interval);
		
		List<SourceInfo> sources = sourceInfoRepository.getSources();
		
		for (SourceInfo sourceInfo: sources) {
			List<ResultInfo> resultCollection = dbExecutor.execute(sourceInfo, timestamp);
			
			resultInfoRepository.add(resultCollection);
		}
	}
	
	public void shutdown() {
		LOG.info("Shutdown");
		run = false;
	}
	
}
