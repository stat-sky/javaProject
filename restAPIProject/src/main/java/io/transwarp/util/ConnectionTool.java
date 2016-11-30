package io.transwarp.util;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

public class ConnectionTool {

	private static Logger logger = Logger.getLogger(ConnectionTool.class);
	
	static {
		try {
			String className = CommonString.prop_env.getProperty("className");
			Class.forName(className);
			logger.info(className);
		}catch(Exception e) {
			logger.error("load driver is error : " + e.getMessage());
		}
	}
	
	public static Connection getConnection() {
		String securityType = CommonString.prop_env.getProperty("securityType");
		String url = CommonString.prop_env.getProperty("inceptorURL");
		if(securityType.equals("kerberos")) {
			String kerberosPath = CommonString.prop_env.getProperty("kerberosPath");
			return ConnectionTool.getConnection(securityType, url, kerberosPath);
		}else {
			String username = CommonString.prop_env.getProperty("jdbcUser");
			String password = CommonString.prop_env.getProperty("jdbcPwd");
			return ConnectionTool.getConnection(securityType, url, username, password);
		}
	}
	public static Connection getConnection(String securityType, String url, String kerberosPath) {
		Connection connection = null;
		url = url + ";" + kerberosPath;
		try {
			connection = DriverManager.getConnection(url);
		}catch(Exception e) {
			logger.error("get connection of Kerberos is error : " + e.getMessage());
		}
		return connection;
	}
	public static Connection getConnection(String securityType, String url, String username, String password) {
		Connection connection = null;
		try {
			if(securityType.equalsIgnoreCase("simple")) {
				logger.info("securityType is " + securityType + " , url is : " + url);
				connection = DriverManager.getConnection(url);
			}else if(securityType.equalsIgnoreCase("ldap") || securityType.equalsIgnoreCase("all")) {
				logger.info("securityType is " + securityType + " , url is : " + url);
				connection = DriverManager.getConnection(url, username, password);
			}
		}catch(Exception e) {
			logger.error("get connection error : " + e.getMessage());
		}
		return connection;
	}
	
	

}
