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
		String inceptorserverIP = CommonString.prop_env.getProperty("inceptorserverIP");
		return ConnectionTool.getConnection(securityType, inceptorserverIP);
	}
	public static Connection getConnection(String securityType, String ipAddress) {
		return ConnectionTool.getConnection(securityType, ipAddress, "10000");
	}
	public static Connection getConnection(String securityType, String ipAddress, String port) {
		return ConnectionTool.getConnection(securityType, ipAddress, port, "system");
	}
	public static Connection getConnection(String securityType, String ipAddress, String port, String database) {
		String jdbcUser = CommonString.prop_env.getProperty("jdbcUser");
		String jdbcPwd = CommonString.prop_env.getProperty("jdbcPwd");
		return ConnectionTool.getConnection(securityType, ipAddress, port, database, jdbcUser, jdbcPwd);
	}
	public static Connection getConnection(String securityType, String ipAddress, String port, String database, String jdbcUser, String jdbcPwd) {
		Connection connection = null;
		try {
			if(securityType.equals("simple")) {
				String url = "jdbc:hive2://" + ipAddress + ":" + port + "/" + database;
				logger.info("security is : " + securityType + "url of connect database is : " + url);
				connection = DriverManager.getConnection(url);
			}else if(securityType.equals("kerberos")) {
				String url = "jdbc:hive2://tw-node110:10000/default;principal=hive/tw-node110@TDH;"
						+ "kuser=hive;keytab=/home/xhy/temp/hive.keytab;authentication=kerberos;krb5conf=/home/xhy/temp/krb5.conf";
//				String url = CommonString.prop_env.getProperty("url");
//				logger.info(url);
//				connection = DriverManager.getConnection(url);
			}else {
				String url = "jdbc:hive2://" + ipAddress + ":" + port + "/" + database;
				logger.info("security is : " + securityType + "url of connect database is : " + url);
				connection = DriverManager.getConnection(url, jdbcUser, jdbcPwd);
			}
			
		}catch(Exception e) {
			logger.error("get connection error : " + e.getMessage());
			e.printStackTrace();
		}
		return connection;
	}
	

}
