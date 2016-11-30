package io.transwarp.report;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.transwarp.api.NodeAPI;
import io.transwarp.bean.NodeBean;
import io.transwarp.util.CommonString;
import io.transwarp.util.CommonUtil;
import io.transwarp.util.FileUtil;
import io.transwarp.util.HttpMethod;
import io.transwarp.util.SessionTool;

import org.apache.log4j.Logger;

public class MainReport {

	private static Logger logger = Logger.getLogger(MainReport.class);
	private HttpMethod method;
	private String securityType = "simple";
	private Map<String, String> serviceConfigs;
	private Map<String, NodeBean> nodes;
	
	public MainReport() {
		this(HttpMethod.getMethod(), CommonString.prop_env.getProperty("securityType"));
	}
	
	public MainReport(HttpMethod method, String securityType) {
		this.method = method;
		this.securityType = securityType;
		this.nodes = new NodeAPI(this.method).getAllNodes("summary");
		this.serviceConfigs = new CheckServiceConfig(this.method).getAllNodeConfig();
	}
	
	public void getReport(String path) {
		List<String> answer = new ArrayList<String>();
		String ipAddress = "";
		String nodeUser = CommonString.prop_env.getProperty("nodeUser");
		String nodePwd = CommonString.prop_env.getProperty("nodePwd");
		String hostname;
		for(Iterator<String> keys = this.nodes.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			NodeBean node = nodes.get(key);
			ipAddress = node.getIpAddress();
			hostname = node.getHostName();
			List<String> roleTypes = node.getRoles();
			try {
				SessionTool session = SessionTool.getSession(ipAddress, nodeUser, nodePwd);
				answer.add("节点 " + node.getHostName() + " 的报告：\n");
				answer.add(CommonUtil.paddingString("", 100, '='));
				answer.add("\n");
				
				//节点基本信息
				answer.add(this.getNodeInfo(node));
				//节点基础检查：包括系统参数指标和平台服务相关配置
				answer.add(this.getBasisCheck(session, hostname));
				logger.info("check os final");
				
				//进程检查
				CheckProcess processCheck = new CheckProcess(session, roleTypes);
				answer.add(processCheck.checkProcess());
				logger.info("check process final");
				
				//日志检查
				CheckTDHLog logCheck = new CheckTDHLog(ipAddress, nodeUser, nodePwd, roleTypes);
				logCheck.checkLog();
				
				session.close();
			}catch(Exception e) {
				logger.error("error at check node : " + hostname + " , message is : " + e.getMessage());
			}
			answer.add("\n\n");
		}
		
		//数据检测
		CheckTDHData tdhDataCheck = new CheckTDHData(this.securityType, ipAddress, nodeUser, nodePwd);
		answer.add(tdhDataCheck.toString());
		logger.info("check data final");
		
		this.method.closeMethod();
		//将结果写入文件
		try {
			FileUtil.writeToFile(answer, path);
		}catch(Exception e) {
			logger.error("error at write result to file : " + e.getMessage());
		}
	}
	
	private String getNodeInfo(NodeBean node) {
		StringBuffer nodeInfo = new StringBuffer();
		nodeInfo.append("节 点 基 本 信 息 ：\n");
		List<String> temp = new ArrayList<String>();
		temp.add("hostname," + node.getHostName());
		temp.add("ipAddress," + node.getIpAddress());
		temp.add("rackName," + node.getRackName());
		nodeInfo.append(CommonUtil.printByTable(new String[]{"parameter name", "value"}, temp, 40)).append("\n");
		return nodeInfo.toString();
	}
	
	//与服务安全无关、每个节点都要进行的检查
	private String getBasisCheck(SessionTool session, String hostname) {
		StringBuffer basisCheck = new StringBuffer();
		try {
			//系统参数指标相关检测
			basisCheck.append("系 统 参 数 指 标 相 关 ：\n");
			CheckSystem systemCheck = new CheckSystem(session);
			basisCheck.append(systemCheck.toString()).append("\n");
			
			//写入平台服务参数与指标检测结果
			basisCheck.append("平 台 服 务 参 数 与 指 标 ：\n");
			String serviceConfig = this.serviceConfigs.get(hostname);
			basisCheck.append(serviceConfig).append("\n");
		}catch(Exception e) {
			logger.error("error at basis check, message is : " + e.getMessage());
		}
		return basisCheck.toString();
	}
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		MainReport report = new MainReport();
		report.getReport("/home/xhy/temp/report.txt");
		long endTime = System.currentTimeMillis();
		System.out.println("cost time is " + (endTime - startTime) * 1.0 / 1000 / 60 + "  min");
	}
}
