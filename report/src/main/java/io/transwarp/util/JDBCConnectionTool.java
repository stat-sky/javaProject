package io.transwarp.util;

import java.sql.Connection;
import java.sql.DriverManager;


import org.apache.log4j.Logger;

public class JDBCConnectionTool {
	
	private static Logger logger = Logger.getLogger(JDBCConnectionTool.class);

	static {
		String className = Constant.prop_env.getProperty("className");
		try {
			Class.forName(className);
			logger.info("className is : " + className);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
	}
	
	public static Connection getConnection() throws Exception{
		String security = Constant.prop_env.getProperty("security");
		String url = Constant.prop_env.getProperty("inceptorURL");
		Connection conn = null;
		if(security.equals("kerberos")) {
			String principal = Constant.prop_env.getProperty("principal");
			String kuser = Constant.prop_env.getProperty("kuser");
			String keytab = Constant.prop_env.getProperty("keytabPath");
			String krb5conf = Constant.prop_env.getProperty("krb5confPath");
			//构建url连接串
			StringBuffer urlBuild = new StringBuffer(url).append(";");
			urlBuild.append("principal=").append(principal).append(";");
			urlBuild.append("authencation=kerberos;");
			urlBuild.append("kuser=").append(kuser).append(";");
			urlBuild.append("keytab=").append(keytab).append(";");
			urlBuild.append("krb5conf=").append(krb5conf);
			conn = getConnection(urlBuild.toString());
		}else if(security.equals("ldap") || security.equals("all")){
			String username = Constant.prop_env.getProperty("username");
			String password = Constant.prop_env.getProperty("password");
			conn = getConnection(url, username, password);
		}else if(security.equals("simple")) {
			conn = getConnection(url);
		}else {
			throw new RuntimeException("security can only be simple, ldap, kerberos, all");
		}
		return conn;
	}
	
	public static Connection getConnection(String url, String username, String password) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, username, password);
		}catch(Exception e) {
			logger.error("error at get connection : " + e.getMessage());
		}
		return conn;
	}
	
	public static Connection getConnection(String url) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		}catch(Exception e) {
			logger.error("error at get connection : " + e.getMessage());
		}
		return conn;
	}
}
