package io.transwarp.thread;

import io.transwarp.util.JDBCConnectionTool;
import io.transwarp.util.UtilTool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

public class SqlQueryCallable implements Callable<List<Object>>{

	private static Logger logger = Logger.getLogger(SqlQueryCallable.class);
	private String security;
	private String url;
	private String jdbcUser;
	private String jdbcPwd;
	private String sql;
	private Class clazz;
	
	public SqlQueryCallable(String security, String url, String sql, Class clazz) {
		this(security, url, sql, clazz, null, null);
	}
	public SqlQueryCallable(String security, String url, String sql, Class clazz, String jdbcUser, String jdbcPwd) {
		this.security = security;
		this.url = url;
		this.jdbcUser = jdbcUser;
		this.jdbcPwd = jdbcPwd;
		this.sql = sql;
		this.clazz = clazz;
	}
	
	@Override
	public List<Object> call() {
		List<Object> results = new ArrayList<Object>();
		//获取jdbc连接
		Connection conn = null;
		if(this.security.equals("simple") || this.security.equals("kerberos")) {
			conn = JDBCConnectionTool.getConnection(url);
		}else if(this.security.equals("ldap") || this.security.equals("all")) {
			conn = JDBCConnectionTool.getConnection(url, jdbcUser, jdbcPwd);
		}
		//查询数据
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			while(rs.next()) {
				Object result = clazz.newInstance();
				StringBuffer buf = new StringBuffer();
				//获取查询结果列名，根据列名来获取set方法
				for(int i = 1; i <= columnCount; i++) {
					String columnName = metaData.getColumnName(i);
					try {
						Method setMethod = (Method)clazz.getMethod("set" + UtilTool.changeFirstCharToCapital(columnName), String.class);
						String value = rs.getString(columnName);
						buf.append(value).append(",");
						setMethod.invoke(result, new Object[]{value});						
					}catch(Exception refactException) {
						refactException.printStackTrace();
					}

				}
//				logger.info("value is : " + buf.toString());
				results.add(result);
			}
		}catch(Exception e) {
			logger.error("query information by sql is error : " + e.getMessage());
			logger.error("error sql is : " + sql);
		}
		return results;
	}
}
