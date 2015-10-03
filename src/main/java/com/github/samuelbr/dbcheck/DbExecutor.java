package com.github.samuelbr.dbcheck;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.samuelbr.dbcheck.SourceInfoRepository.SourceInfo;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DbExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(DbExecutor.class);
	
	private final Map<String, SqlSessionFactory> dataSourcesSessionFactory = Maps.newHashMap();
	
	private final Object lockMonitor = new Object();

	private String configFile;
	
	public DbExecutor(String configFile) {
		Preconditions.checkArgument(configFile != null);
		this.configFile = configFile;
	}

	public List<ResultInfo> execute(SourceInfo sourceInfo, long timestamp) {
		Preconditions.checkArgument(sourceInfo != null);
		
		SqlSessionFactory sessionFactory = getSqlSessionFactory(sourceInfo);
		SqlSession session = null;
		try {
			
			DbExecutorResultHandler handler = new DbExecutorResultHandler(timestamp, sourceInfo.getTags());
			
			session = sessionFactory.openSession();
			session.getMapper(SqlMapper.class)
					.select(sourceInfo.getSql(), handler);

			return handler.getResultList();
			
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	private SqlSessionFactory getSqlSessionFactory(SourceInfo sourceInfo) {
		synchronized (lockMonitor) {
			if (!dataSourcesSessionFactory.containsKey(sourceInfo.getDataSource())) {
				dataSourcesSessionFactory.put(
						sourceInfo.getDataSource(), 
						createSqlSessionFactory(sourceInfo));
			}
			
			return dataSourcesSessionFactory.get(sourceInfo.getDataSource());
		}
	}
	
	private SqlSessionFactory createSqlSessionFactory(SourceInfo sourceInfo) {
		LOG.info("Create SqlSessionFactory for {}", sourceInfo.getDataSource());
		return new SqlSessionFactoryBuilder()
			.build(DbExecutor.class.getClassLoader().getResourceAsStream(configFile),
				   sourceInfo.getDataSource());
		
	}
	
	private static class DbExecutorResultHandler implements ResultHandler<Object> {

		private final long timestamp;
		
		private final List<String> tags;
		
		private List<ResultInfo> resultList = Lists.newArrayList();
		
		public DbExecutorResultHandler(long timestamp, List<String> tags) {
			this.timestamp = timestamp;
			this.tags = tags;
		}
		
		@SuppressWarnings("unchecked")
		public void handleResult(ResultContext<? extends Object> resultContext) {
			ResultInfo resultInfo = ResultInfo.create((Map<String, Object>) resultContext.getResultObject(), tags, timestamp);
			
			resultList.add(resultInfo);
		}
		
		public List<ResultInfo> getResultList() {
			return resultList;
		}
		
	}
	
}
