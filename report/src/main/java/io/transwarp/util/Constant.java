package io.transwarp.util;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.apache.log4j.Logger;

public final class Constant {

	private static Logger logger = Logger.getLogger(Constant.class);
	/**
	 * 编码格式设置
	 */
	public static final String ENCODING = "UTF-8";	
	/**
	 * 环境变量配置文件路径
	 */
	public static final String ENV_PATH = "config/env.properties";
	/**
	 * 表的数据字典查询语句
	 */
	public static final String SELECT_TABLES = "select database_name, table_name, table_type, transactional, table_format, table_location, owner_name from system.tables_v;";
	/**
	 * rest api调用配置路径
	 */
	public static String restapi_path;
	/**
	 * 服务指标查询配置路径
	 */
	public static String metric_path;
	/**
	 * 报告生成配置路径
	 */
	public static String report_path;
	/**
	 * 日志检测配置路径
	 */
	public static String logCheck_path;
	/**
	 * 进程检测配置路径
	 */
	public static String processCheck_path;
	/**
	 * 端口检测配置路径
	 */
	public static String portCheck_path;
	/**
	 * 发送文件的shell脚本路径
	 */
	public static String scp_path;
	/**
	 * kerberos认证下的执行脚本路径
	 */
	public static String kerberos_path;
	/**
	 * 输出路径
	 */
	public static String goalPath;
	/**
	 * 环境配置读取
	 */
	public static Properties prop_env = new Properties();
	/**
	 * 指标查询配置读取
	 */
	public static Properties prop_metric = new Properties();
	/**
	 * 报告配置读取
	 */
	public static ConfigRead prop_report = null;
	/**
	 * rest api配置读取
	 */
	public static ConfigRead prop_restapi = null;
	/**
	 * 日志检测配置读取
	 */
	public static ConfigRead prop_logCheck = null;
	/**
	 * 进程检测配置读取
	 */
	public static ConfigRead prop_processCheck = null;
	/**
	 * 端口检测配置读取
	 */
	public static ConfigRead prop_portCheck = null;
	/**
	 * 日期格式设置
	 */
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	/**
	 * 浮点数小数位设置
	 */
	public static DecimalFormat numberFormat = new DecimalFormat("#0.00");

	
	//rest api对应查询项名称
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
	
	static {
		try {
			//加载环境变量
			prop_env.load(new FileInputStream(ENV_PATH));
			//获取环境变量信息
			restapi_path = prop_env.getProperty("restapi_path");
			metric_path = prop_env.getProperty("metric_path");
			report_path = prop_env.getProperty("report_path");
			logCheck_path = prop_env.getProperty("logCheck_path");
			processCheck_path = prop_env.getProperty("processCheck_path");
			portCheck_path = prop_env.getProperty("portCheck_path");
			scp_path = prop_env.getProperty("scp_path");
			kerberos_path = prop_env.getProperty("kerberos_path");
			goalPath = prop_env.getProperty("goalPath");
			
			//加载其他配置
			prop_metric.load(new FileInputStream(metric_path));
			prop_report = new ConfigRead(report_path);
			prop_restapi = new ConfigRead(restapi_path);
			prop_logCheck = new ConfigRead(logCheck_path);
			prop_processCheck = new ConfigRead(processCheck_path);
			prop_portCheck = new ConfigRead(portCheck_path);
		}catch(Exception e) {
			logger.error("load configuration error : " + e.getMessage());
		}
	}
}
