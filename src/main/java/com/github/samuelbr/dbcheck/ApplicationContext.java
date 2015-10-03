package com.github.samuelbr.dbcheck;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public final class ApplicationContext {
	
	private static final Logger LOG = LoggerFactory.getLogger(ApplicationContext.class);

	private DbCheckPoolerJob poolerJob;
	
	private SourceInfoRepository sourceInfoRepository;
	
	private ResultInfoRepository resultInfoRepository;
	
	private DbExecutor dbExecutor;
	
	private ApplicationContext() {
		
	}
	
	private void init() {
		LOG.info("Initialize application context");
		sourceInfoRepository = new SourceInfoRepository("config.js");
		resultInfoRepository = new ResultInfoRepository();
		dbExecutor = new DbExecutor("mybatis-config.xml");
		
		poolerJob = new DbCheckPoolerJob(dbExecutor, sourceInfoRepository, resultInfoRepository);
		poolerJob.start();
		
	}
	
	private void destroy() {
		LOG.info("Destroy application context");
		
		poolerJob.shutdown();
		
		try {
			poolerJob.join(poolerJob.getInterval() * 10);
		} catch (InterruptedException e) {
			LOG.error("InterruptedException while joining pooler thread", e);
		}
		
		if (poolerJob.isAlive()) {
			LOG.error("PoolerJob is still running, terminate it.");
			poolerJob.interrupt();
		}
	}
	
	public DbExecutor getDbExecutor() {
		return dbExecutor;
	}
	
	public SourceInfoRepository getSourcesRepository() {
		return sourceInfoRepository;
	}
	
	public static void register(ServletContext servletContext) {
		Preconditions.checkArgument(servletContext != null);
		
		ApplicationContext context = new ApplicationContext();
		context.init();
		
		servletContext.setAttribute(ApplicationContext.class.getName(), context);
	}
	
	public static void unregister(ServletContext servletContext) {
		Preconditions.checkArgument(servletContext != null);
		
		ApplicationContext context = getApplicationContext(servletContext);
		if (context != null) {
			context.destroy();
			
			servletContext.removeAttribute(ApplicationContext.class.getName());
		} else {
			LOG.error("ApplicationContext is not set in servlet context");
		}
	}
	
	public static ApplicationContext getApplicationContext(ServletContext servletContext) {
		Preconditions.checkArgument(servletContext != null);
		
		return (ApplicationContext) servletContext.getAttribute(ApplicationContext.class.getName());
	}
	
}
