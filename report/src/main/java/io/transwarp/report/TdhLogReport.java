package io.transwarp.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.transwarp.bean.NodeBean;
import io.transwarp.util.Constant;
import io.transwarp.util.SessionTool;
import io.transwarp.util.UtilTool;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class TdhLogReport {

	private static Logger logger = Logger.getLogger(TdhLogReport.class);
	private NodeBean node;
	private String nodeUser;
	private String nodePwd;
	private long nowTime;
	
	public TdhLogReport(NodeBean node, String nodeUser, String nodePwd) {
		this.node = node;
		this.nodeUser = nodeUser;
		this.nodePwd = nodePwd;
		this.nowTime = System.currentTimeMillis();
	}
	
	public void getLogReport() {
		//scp 日志文件到本地指定路径下
		List<String> roles = this.node.getRoles();
		List<Element> configs = Constant.prop_logCheck.getAll();
		for(Element config : configs) {
			String serviceRole = config.elementText("ServiceRole");
			logger.info(serviceRole);
			boolean hasRole = false;
			for(String role : roles) {
				if(role.matches("\\S*" + serviceRole + "\\S*")) {
					hasRole = true;
					break;
				}
			}
			//若无该服务角色则跳过检测
			if(!hasRole) continue;
			
			//从节点中获取日志文件
			String filePath = config.elementText("logpath");
			String goalPath = Constant.goalPath + this.node.getIpAddress() + "_log/" + serviceRole + "/";
			File dirFile = new File(goalPath);
			if(dirFile.exists()) {
				UtilTool.deleteFile(dirFile.getAbsolutePath());
			}
			dirFile.mkdirs();
			//构建scp语句
			StringBuffer command = new StringBuffer("sh ");
			command.append(Constant.prop_env.getProperty("scp_script"));
			command.append(" ").append(this.nodePwd);
			command.append(" ").append(this.nodeUser).append("@").append(this.node.getIpAddress()).append(":").append(filePath).append("*");
			command.append(" ").append(goalPath);
			logger.info("scp command is : " + command.toString());
			try {
				SessionTool.executeLocal(command.toString());
			}catch(Exception e) {
				logger.error("exec scp shell error, error message is : " + e.getMessage());
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//分析日志
			File[] files = dirFile.listFiles();
			int numberOfFile = files.length;
			String filename = UtilTool.getFileName(filePath);
			if(filename.indexOf("*") != -1) filename = filename.replaceAll("\\*", node.getHostName());
			for(int i = numberOfFile - 1; i >= 0; i--) {
				StringBuffer logPath = new StringBuffer(goalPath);
				logPath.append(filename);
				if(i != 0) logPath.append(".").append(i);
				this.analysisLog(logPath.toString(), config);
			}
			
		}
		
		
	}
	

	public void analysisLog(String logPath, Element config) {
		logger.info("analysis log file is : " + logPath);
		String serviceRole = config.elementText("ServiceRole");
		String logfileDir = UtilTool.getDirectory(logPath);
		String configOfKey = config.elementText("key");
		logger.info("splitKey is : " + configOfKey);
		String[] splitKeys = configOfKey.split(";");
		//用于存放日志过滤结果，key为生成文件名，value为存放过滤结果的list
		Map<String, List<String>> saveLog = new HashMap<String, List<String>>();
		//用于存放日志过滤关键字，key为关键字，value为存放该关键字过滤结果要写到的文件名
		Map<String, String> key_file = new HashMap<String, String>();
		//分析关键字，建立关键字与过滤内容的映射
		for(String splitKey : splitKeys) {
			String[] items = splitKey.split(":");
			key_file.put(items[1], logfileDir + items[0] + "-" + serviceRole);
		}
		//分析日志，将结果存入list
		try {
			BufferedReader reader = new BufferedReader(new FileReader(logPath));
			boolean oneDay = false;  //用于标记是否为一天内的日志
			StringBuffer logBuffer = null;    //用于拼接一条日志
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] items = line.split(" ");
				if(items.length >= 3 && items[0].matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
					if(logBuffer != null && oneDay) {
						String log = logBuffer.toString();
						for(Iterator<String> logKeys = key_file.keySet().iterator(); logKeys.hasNext(); ) {
							String logKey = logKeys.next();
							if(line.indexOf(logKey) == -1) continue;
							String filename = key_file.get(logKey);
							List<String> fileValue = saveLog.get(filename);
							if(fileValue == null) fileValue = new ArrayList<String>();
							fileValue.add(log);
							fileValue.add("\n");
							saveLog.put(filename, fileValue);
						}
					}
					logBuffer = new StringBuffer(line);
					oneDay = UtilTool.checkDateTime(items[0] + " " + items[1], nowTime);
				}else if(logBuffer != null && oneDay) {
					logBuffer.append("\n").append(line);
				}
			}			
		}catch(Exception e) {
			logger.error("read log file error, error message is : " + e.getMessage());
		}

		
		//将结果写到文件
		for(Iterator<String> logKeys = key_file.keySet().iterator(); logKeys.hasNext(); ) {
			String logKey = logKeys.next();
			String filename = key_file.get(logKey);
			List<String> fileValue = saveLog.get(filename);
			if(fileValue == null) fileValue = new ArrayList<String>();
			FileWriter writer = null;
			try {
				writer = new FileWriter(filename, true);
				for(String value : fileValue) {
					writer.write(value);
				}			
			} catch(Exception e) {
				logger.error("writer log to file error , error message is : " + e.getMessage());
			} finally {
				if(writer != null) {
					try {
						writer.flush();
						writer.close();
					} catch (IOException e) {
						logger.error("close file writer error, error message is : " + e.getMessage());
					}
				}
			}

		}
		 
	}
	
}
