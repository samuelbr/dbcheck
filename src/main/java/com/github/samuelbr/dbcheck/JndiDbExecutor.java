package com.github.samuelbr.dbcheck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.github.samuelbr.dbcheck.SourceInfoRepository.SourceInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JndiDbExecutor {

	private DataSource getDataSource(SourceInfo sourceInfo) throws ExecutorException {
		
		try {
			InitialContext context = new InitialContext();
			Context envContext = (Context) context.lookup("java:comp/env");
			
			return (DataSource) envContext.lookup(sourceInfo.getDataSource());
		} catch (NamingException e) {
			throw new ExecutorException(e);
		}
	}
	
	public List<ResultInfo> execute(SourceInfo sourceInfo, long timestamp) throws ExecutorException {
		
		DataSource dataSource = getDataSource(sourceInfo);
		
		Connection connection = null; 
		PreparedStatement preparedStatement = null;
		try {
			connection = dataSource.getConnection();
			connection.setReadOnly(true);

			preparedStatement = connection.prepareStatement(sourceInfo.getSql());
			
			ResultSet resultSet = preparedStatement.executeQuery();
			
			return processResultSet(timestamp, resultSet, sourceInfo);
		} catch (SQLException e) {
			throw new ExecutorException(e); 
		} finally {
			
			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				throw new ExecutorException(e);
			}
		}
		
	}
	
	private List<ResultInfo> processResultSet(long timestamp, ResultSet resultSet, SourceInfo sourceInfo) throws SQLException {
		List<ResultInfo> result = Lists.newArrayList();
		
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		

		String labels[] = new String[columnCount];
		
		for (int a=0; a<columnCount; a++) {
			labels[a] = metaData.getColumnLabel(a+1);
		}
		
		Map<String, Object> row = Maps.newHashMapWithExpectedSize(columnCount);
		
		while (resultSet.next()) {
			for (int a=0; a<columnCount; a++) {
				row.put(labels[a], extractValue(resultSet.getObject(a+1)));
			}
			
			result.add(ResultInfo.create(row, sourceInfo.getTags(), timestamp));
		}
		
		return result;
	}
	
	private Object extractValue(Object value) {
		if (value == null || value instanceof String || value.getClass().isPrimitive()) {
			return value;
		}
		
		return String.valueOf(value);
	}
	
}
