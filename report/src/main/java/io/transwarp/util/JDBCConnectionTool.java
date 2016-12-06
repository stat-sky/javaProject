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
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
	}
	
	public static Connection getConnection() {
		String security = Constant.prop_env.getProperty("security");
		String url = Constant.prop_env.getProperty("inceptorURL");
		Connection conn = null;
		if(security.equals("kerberos")) {
			String principal = Constant.prop_env.getProperty("principal");
			String kuser = Constant.prop_env.getProperty("kuser");
			String keytab = Constant.prop_env.getProperty("keytabPath");
			String krb5conf = Constant.prop_env.getProperty("krb5confPath");
			conn = getConnection(url, principal, kuser, keytab, krb5conf);
		}else {
			String username = Constant.prop_env.getProperty("username");
			String password = Constant.prop_env.getProperty("password");
			conn = getConnection(security, url, username, password);
		}
		return conn;
	}
	
	public static Connection getConnection(String security, String url, String username, String password) {
		Connection conn = null;
		try {
			if(security.equals("simple")) {
				logger.info("security is simple, url is : " + url);
				conn = DriverManager.getConnection(url);
			}else if(security.equals("ldap") || security.equals("all")) {
				logger.info("security is " + security + ", url is : " + url);
				conn = DriverManager.getConnection(url, username, password);
			}
		}catch(Exception e) {
			logger.error("error at get connection : " + e.getMessage());
		}
		return conn;
	}
	
	public static Connection getConnection(String url, String principal, String kuser, String keytab, String krb5conf) {
		Connection conn = null;
		StringBuffer urlBuild = new StringBuffer(url).append(";");
		urlBuild.append("principal=").append(principal).append(";");
		urlBuild.append("authencation=kerberos;");
		urlBuild.append("kuser=").append(kuser).append(";");
		urlBuild.append("keytab=").append(keytab).append(";");
		urlBuild.append("krb5conf=").append(krb5conf);
		logger.info("security is kerberos, url is : " + urlBuild.toString());
		try {
			conn = DriverManager.getConnection(urlBuild.toString());
		}catch(Exception e) {
			logger.error("error at get connection by kerberos : " + e.getMessage());
		}
		return conn;
	}
}
