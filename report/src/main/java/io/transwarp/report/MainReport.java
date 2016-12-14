package io.transwarp.report;

import io.transwarp.bean.NodeBean;
import io.transwarp.bean.RoleBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.thread.NodeCallable;
import io.transwarp.thread.RoleCallable;
import io.transwarp.thread.ServiceCallable;
import io.transwarp.util.Constant;
import io.transwarp.util.HttpMethodTool;
import io.transwarp.util.SessionTool;

import java.io.FileWriter;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

public class MainReport {

	private static Logger logger = Logger.getLogger(MainReport.class);
	private String security;
	//rest api连接信息
	private String managerIP;
	private String username;
	private String password;
	//节点连接信息
	private String nodeUser;
	private String nodePwd;
	//inceptor连接信息
	private String inceptorIP;
	private String port;
	private String jdbcUser;
	private String jdbcPwd;
	//hdfs用户密码
	private String hdfsPwd;
	//rest api调用方法
	private HttpMethodTool method;
	//集群服务、节点、配置信息
	private Map<String, ServiceBean> services;
	private Map<String, NodeBean> nodes;
	private Map<String, String> configs;
	
	public MainReport() throws Exception {
		this.security = Constant.prop_env.getProperty("security");
		this.managerIP = Constant.prop_env.getProperty("managerIP");
		this.username = Constant.prop_env.getProperty("username");
		this.password = Constant.prop_env.getProperty("password");
		this.nodeUser = Constant.prop_env.getProperty("nodeUser");
		this.nodePwd = Constant.prop_env.getProperty("nodePwd");
		this.inceptorIP = Constant.prop_env.getProperty("inceptorIP");
		this.port = Constant.prop_env.getProperty("port");
		this.jdbcUser = Constant.prop_env.getProperty("jdbcUser");
		this.jdbcPwd = Constant.prop_env.getProperty("jdbcPwd");
		this.hdfsPwd = Constant.prop_env.getProperty("hdfsPwd");
	}
	
	private void init() {
		//查询所有服务信息
		ServiceCallable serviceCallable = new ServiceCallable(this.method, "summary", null);
		this.services = serviceCallable.call();
		//查询所有节点信息
		NodeCallable nodeCallable = new NodeCallable(this.method, "summary", null);
		this.nodes = nodeCallable.call();
		//查询所有服务角色信息
		RoleCallable roleCallable = new RoleCallable(this.method, null);
		Map<String, RoleBean> roles = roleCallable.call();
		for(Iterator<String> keys = roles.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			RoleBean role = roles.get(key);
			String serviceName = role.getService().getName();
			ServiceBean service = this.services.get(serviceName);
			if(service == null) {
				service = role.getService();
				service.addRole(role);
			}else {
				service.addRole(role);
			}
			this.services.put(serviceName, service);
			
		}
		//获取平台服务配置信息
		ServiceConfigReport configReport = new ServiceConfigReport(this.method, this.services);
		try {
			this.configs = configReport.getConfigReprot();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
/*	public void outputConfig() throws Exception{
		this.method = HttpMethodTool.getMethod("http://" + managerIP + ":8180", username, password);
		init();
		for(Iterator<String> keys = this.configs.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			System.out.println(key);
			String value = this.configs.get(key);
			System.out.println(value);
		}
		this.method.close();
	}*/
	
	public void getReport(String path) throws Exception {
		this.method = HttpMethodTool.getMethod("http://" + managerIP + ":8180", username, password);
		init();
		//创建一个写文件
		FileWriter output = new FileWriter(path);
		//集群数据检测 —— 包括hdfs集群检测和数据表检测
		StringBuffer url = new StringBuffer("jdbc:hive2://");
		if(this.security.equals("kerberos")) {
			String principal = Constant.prop_env.getProperty("principal");
			String kuser = Constant.prop_env.getProperty("kuser");
			String keytab = Constant.prop_env.getProperty("keytab");
			String krb5conf = Constant.prop_env.getProperty("krb5conf");
			url.append("/default;principal=").append(principal).append(";");
			url.append("authentication=kerberos;");
			url.append("kuser=").append(kuser).append(";");
			url.append("keytab=").append(keytab).append(";");
			url.append("krb5conf=").append(krb5conf);
		}else {
			url.append(this.inceptorIP).append(":").append(this.port);
		}
		TDHDataReport dataReport = new TDHDataReport(this.security, this.managerIP, this.nodeUser, this.nodePwd, url.toString(), 
				this.jdbcUser, this.jdbcPwd, this.hdfsPwd);
		output.write(dataReport.getDataReport());
		//平台服务角色分布
		ServiceRoleMapReport roleMap = new ServiceRoleMapReport(this.nodes, this.services);
		output.write(roleMap.getRoleMap());
		//进程检测
		SessionTool session = SessionTool.getSession(inceptorIP, nodeUser, nodePwd);
		ProcessReport processReport = new ProcessReport(session);
		output.write(processReport.getProcessReport());
		session.close();
		//节点检测
		for(Iterator<String> hostnames = this.nodes.keySet().iterator(); hostnames.hasNext(); ) {
			String hostname = hostnames.next();
			NodeBean node = this.nodes.get(hostname);
			output.write("节点 " + hostname + " 检测\n");
			output.write("=================================================================================\n");
			//节点基本信息检测、端口检测
			NodeBaseReport nodeReport = new NodeBaseReport(node, this.nodeUser, this.nodePwd);
			output.write(nodeReport.getNodeCheckReport());
			//平台服务配置检测
			output.write(this.configs.get(hostname));
			
			//节点日志检测
			TdhLogReport logReport = new TdhLogReport(node, nodeUser, nodePwd);
			logReport.getLogReport();
		}
		output.flush();
		output.close();
		this.method.close();
	}
	
/*	public void getRoleMap() {
		ServiceRoleMapReport roleMap = new ServiceRoleMapReport(this.nodes, this.services);
		System.out.println(roleMap.getRoleMap());
	}*/
	
	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();
			MainReport report = new MainReport();
			report.getReport(Constant.prop_env.getProperty("goalPath") + "reprot.txt");
			long end = System.currentTimeMillis();
			System.out.println((end - start) * 1.0 / 1000 + " s");
//			report.outputConfig();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
