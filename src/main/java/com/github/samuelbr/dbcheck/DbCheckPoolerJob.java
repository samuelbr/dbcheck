package com.github.samuelbr.dbcheck;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.samuelbr.dbcheck.SourceInfoRepository.SourceInfo;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;

public class DbCheckPoolerJob extends Thread {

	private static final Logger LOG = LoggerFactory.getLogger(DbCheckPoolerJob.class);
	
	private static final String POOL_INTERVAL = "dbCheckPoolerJob.poolInterval";
	
	private static final long DEFAULT_INTERVAL = 10000;
	
	private final DbExecutor dbExecutor;
	
	private final SourceInfoRepository sourceInfoRepository;
	
	private final ResultInfoRepository resultInfoRepository;
	
	private final Object shutdownMonitor = new Object();
	
	private volatile boolean run = true;
	
	private long interval = DEFAULT_INTERVAL;

	public DbCheckPoolerJob(
			DbExecutor dbExecutor, 
			SourceInfoRepository sourceInfoRepository,
			ResultInfoRepository resultInfoRepository, 
			Properties properties) {
		this.dbExecutor = dbExecutor;
		this.sourceInfoRepository = sourceInfoRepository;
		this.resultInfoRepository = resultInfoRepository;
		applyProperties(properties);
	}

	private void applyProperties(Properties properties) {
		String value = properties.getProperty(POOL_INTERVAL);
		interval = MoreObjects.firstNonNull(Longs.tryParse(value), DEFAULT_INTERVAL);

		if (interval < 1) {
			LOG.error("Invalid interval {}", interval);
			
			interval = DEFAULT_INTERVAL;
		}
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
				synchronized (shutdownMonitor) {
					shutdownMonitor.wait(delta);
				}
			} catch (InterruptedException e) {
				LOG.error("Thread interrupted", e);
			}
			
			if (run) {
				doWork();
			}
		} while (run);
		
		LOG.info("Finish thread");
	}
	
	private void doWork() {
		try {
			long timestamp = System.currentTimeMillis();
			timestamp -= (timestamp % interval);
			
			List<SourceInfo> sources = sourceInfoRepository.getSources();
			
			for (SourceInfo sourceInfo: sources) {
				List<ResultInfo> resultCollection = dbExecutor.execute(sourceInfo, timestamp);
				
				resultInfoRepository.add(resultCollection);
			}
		} catch (Exception e) {
			LOG.error("Exception occurred", e);
		}
	}
	
	public void shutdown() {
		LOG.info("Shutdown");
		run = false;
		
		synchronized (shutdownMonitor) {
			shutdownMonitor.notify();
		}
	}
	
}
