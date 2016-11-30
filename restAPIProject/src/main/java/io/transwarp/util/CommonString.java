package io.transwarp.util;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.apache.log4j.Logger;

public class CommonString {
	
	private static Logger logger = Logger.getLogger(CommonString.class);

	public static final String CONFIG_PATH = "config/restAPIConfig.xml";
	public static final String PROPERTIES_PATH = "config/env.properties";
	public static final String METRICNAMELIST_PATH = "config/restapi_metric.properties";
	public static final String REPORT_PATH = "config/report.properties";
	public static final String ENCODING = "UTF-8";	
	public static final String SQL_SELECT = "select database_name, table_name, table_type, transactional, table_format, table_location, owner_name from system.tables_v";
	
	//configuration
	public static final String USER_LOGIN = "用户登录";
	public static final String USER_LOGOUT = "用户登出";
	public static final String FIND_SERVICE = "查询服务";
	public static final String FIND_MORE_SERVICE = "查询多个服务";
	public static final String FIND_SERVICE_ROLE = "查询服务角色";
	public static final String FIND_MORE_SERVICE_ROLE = "查询多个服务角色";
	public static final String FIND_NODE = "查询节点";
	public static final String FIND_MORE_NODE = "查询多个节点";
	public static final String DOWNLOAD_CONFIG = "下载服务配置";
	public static final String INQUIRE_CLUSTER_METRIC = "集群指标查询";
	public static final String INQUIRE_SERVICE_METRIC = "服务指标查询";
	public static final String INQUIRE_NODE_METRIC = "节点指标查询";
	public static final String INQURIE_SERVICE_ROLE_METRIC = "服务角色指标查询";
	public static final String DOWNLOAD_KEYTAB = "下载用户keytab";
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public static DecimalFormat numberFormat = new DecimalFormat("#0.00");
	
	//table 
	public static final String[] COLUMN_NAME = {"参数", "值"};
	
	public static Properties prop_metric = new Properties();
	public static Properties prop_env = new Properties();
	public static Properties prop_report = new Properties();
	public static ReadXmlConfig config_restAPI = null;
	public static ReadXmlConfig config_jvm = null;
	public static ReadXmlConfig config_log = null;
	
	static {
		try {
			prop_metric.load(new FileInputStream(new File(CommonString.METRICNAMELIST_PATH)));
			prop_env.load(new FileInputStream(new File(CommonString.PROPERTIES_PATH)));
			prop_report.load(new FileInputStream(new File(CommonString.REPORT_PATH)));
			config_restAPI = new ReadXmlConfig(CommonString.CONFIG_PATH);
			config_jvm = new ReadXmlConfig(prop_report.getProperty("processCheck_path"));
			config_log = new ReadXmlConfig(prop_report.getProperty("logCheck_path"));
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
	}
}
