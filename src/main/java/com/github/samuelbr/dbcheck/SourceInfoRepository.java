package com.github.samuelbr.dbcheck;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class SourceInfoRepository {

	private List<SourceInfo> sources;
	
	public SourceInfoRepository(String configFileName) {
		Preconditions.checkArgument(configFileName != null);

		initFromConfig(configFileName);
	}
	
	private void initFromConfig(String configFileName) {
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new InputStreamReader(
				SourceInfoRepository.class.getClassLoader().getResourceAsStream(configFileName), 
				Charsets.UTF_8));
		
		Map<String, List<DataSourceStatement>> map = gson.fromJson(reader, new TypeToken<Map<String, List<DataSourceStatement>>>(){}.getType());
		
		initSources(map);
	}
	
	private void initSources(Map<String, List<DataSourceStatement>> map) {
		Builder<SourceInfo> builder = ImmutableList.<SourceInfo>builder();
		
		for (Entry<String, List<DataSourceStatement>> entry: map.entrySet()) {
			for (DataSourceStatement statement: entry.getValue()) {
				builder.add(new SourceInfo(entry.getKey(), statement));
			}
		}
		
		sources = builder.build();
	}
	
	public List<SourceInfo> getSources() {
		return sources;
	}

	private static class DataSourceStatement {
		private String sql;
		private List<String> tags;
		
	}
	
	public static class SourceInfo {
		private final String dataSource;
		private final String sql;
		private final List<String> tags;
		
		private SourceInfo(String dataSource, DataSourceStatement dataSourceStatement) {
			this.dataSource = dataSource;
			this.sql = dataSourceStatement.sql;
			this.tags = ImmutableList.copyOf(dataSourceStatement.tags);
		}

		public String getDataSource() {
			return dataSource;
		}
		
		public String getSql() {
			return sql;
		}
		
		public List<String> getTags() {
			return tags;
		}
	}
}
