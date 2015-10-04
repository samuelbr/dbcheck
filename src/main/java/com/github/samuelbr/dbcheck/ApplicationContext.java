package com.github.samuelbr.dbcheck;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public final class ApplicationContext {
	
	private static final Logger LOG = LoggerFactory.getLogger(ApplicationContext.class);
	
	private static final String ALLOWED_VALUES = "applicationContext.allowedValues";
	
	private static final String SEARCH_KEY = "applicationContext.searchKey";

	private DbCheckPoolerJob poolerJob;
	
	private SourceInfoRepository sourceInfoRepository;
	
	private ResultInfoRepository resultInfoRepository;
	
	private DbExecutor dbExecutor;
	
	private Properties properties;
	
	private boolean active = true;
	
	private ApplicationContext() {
		
	}
	
	private void init() {
		LOG.info("Initialize application context");
		
		properties = new Properties();
		try {
			properties.load(ApplicationContext.class.getClassLoader().getResourceAsStream("app.properties"));
		} catch (IOException e) {
			throw new RuntimeException("Cannot initialize application properties", e);
		}
		
		active = isActive(properties);
		
		if (!isActive()) {
			LOG.info("Application is not active");
			return;
		}
		
		sourceInfoRepository = new SourceInfoRepository("sources.js");
		resultInfoRepository = new ResultInfoRepository(properties);
		dbExecutor = new DbExecutor("mybatis-config.xml");
		
		poolerJob = new DbCheckPoolerJob(
				dbExecutor, 
				sourceInfoRepository, 
				resultInfoRepository,
				properties);
		poolerJob.start();
	}
	
	private void destroy() {
		LOG.info("Destroy application context");
		
		if (!isActive()) {
			LOG.info("Application is not active");
			return;
		}
		
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
	
	private boolean isActive(Properties properties) {
		String allowedValues = properties.getProperty(ALLOWED_VALUES);
		String searchKey = properties.getProperty(SEARCH_KEY);
		
		LOG.info("AllowedValues {}, SearchKeys {}", allowedValues, searchKey);
		
		if (Strings.isNullOrEmpty(allowedValues) || Strings.isNullOrEmpty(searchKey)) {
			return true;
		}
		
		if (Objects.equal("*", allowedValues)) {
			return true;
		}
		
		String settedValue = System.getProperty(searchKey);
		LOG.info("System property for {} is {}", searchKey, settedValue);
		
		List<String> allowedValuesList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(allowedValues);
		
		return allowedValuesList.contains(settedValue);
	}
	
	public DbExecutor getDbExecutor() {
		return dbExecutor;
	}
	
	public SourceInfoRepository getSourceInfoRepository() {
		return sourceInfoRepository;
	}
	
	public ResultInfoRepository getResultInfoRepository() {
		return resultInfoRepository;
	}
	
	public boolean isActive() {
		return active;
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
