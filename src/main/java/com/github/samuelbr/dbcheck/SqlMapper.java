package com.github.samuelbr.dbcheck;

import java.util.Map;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.session.ResultHandler;

public interface SqlMapper {
    static class PureSqlProvider {
        public String sql(String sql) {
            return sql;
        }
    }

    @SelectProvider(type = PureSqlProvider.class, method = "sql")
    @ResultType(value=Map.class)
    public void select(String sql, ResultHandler<?> handler);

}