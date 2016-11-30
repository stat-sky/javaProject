package io.transwarp.report;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.transwarp.util.CommonString;
import io.transwarp.util.CommonUtil;
import io.transwarp.util.FileUtil;
import io.transwarp.util.SessionTool;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class CheckTDHLog {

	private static Logger logger = Logger.getLogger(CheckTDHLog.class);
	private String hostname;
	private String ipAddress;
	private String nodeUser;
	private String nodePwd;
	private List<String> roleTypes;
	
	public CheckTDHLog(String hostname, String ipAddress, String nodeUser, String nodePwd, List<String> roleTypes) {
		this.hostname = hostname;
		this.ipAddress = ipAddress;
		this.nodeUser = nodeUser;
		this.nodePwd = nodePwd;
		this.roleTypes = roleTypes;
	}
	
	public void checkLog() {
		List<Element> configs = CommonString.config_log.getElements("log");
		for(Element config : configs) {
			boolean hasLog = false;
			String roleTypeMatch = config.elementText("service");
			if(roleTypeMatch == null || roleTypeMatch.equals("")) {
				hasLog = true;
			}else {
				for(String role : roleTypes) {
					if(role.matches("\\S*" + roleTypeMatch + "\\S*"))  {
						hasLog = true;
						break;
					}
				}				
			}
			logger.info("roleType is : " + roleTypeMatch);
			if(hasLog) {
				String filepath = config.elementText("filepath");
				String nowfile = CommonString.prop_env.getProperty("bufferPath") + CommonUtil.getFileNameHas(filepath.replaceAll("\\*", this.hostname));
				//执行shell脚本将需要解析的日志拷贝到本地
				StringBuffer command = new StringBuffer("sh ");
				command.append(CommonString.prop_report.getProperty("scp_shell"));
				command.append(" ").append(this.nodePwd);
				command.append(" ").append(this.nodeUser).append("@").append(this.ipAddress).append(":").append(filepath);
				command.append(" ").append(nowfile);
				logger.info("scp command is : " + command);
				try {
					SessionTool.executeLocal(command.toString());
				} catch (Exception e) {
					logger.error("scp log file error : " + e.getMessage());
				}
				
				List<String> logs = this.readLog(nowfile);

				//获取关键字
				List<Element> keys = config.element("keys").elements("key");
				for(Element key : keys) {
					String keyValue = key.elementText("value");
					List<String> getLog = new ArrayList<String>();
					for(String log : logs) {
						if(log.indexOf(keyValue) != -1) {
							getLog.add(log);
							getLog.add("\n");
						}
					}
					FileUtil.writeToFile(getLog, nowfile.substring(0, nowfile.indexOf(".")) + "-" + key.elementText("name") + ".log");
				}
/*				//读取日志并解析，将最近一天的日志存下
				String logName = CommonUtil.fileFormat(filepath);
				//获取在本地的路径
				String bufferPath = CommonString.prop_env.getProperty("bufferPath");
				try {
					String[] lines = SessionTool.executeLocal("ls " + bufferPath).split("\n");
					for(String line : lines) {
						if(line.matches(logName + "\\S+" + this.hostname + ".log")) {
							String nowPath = bufferPath + line;
							List<String> logs = this.readLog(nowPath);

							//获取关键字
							List<Element> keys = config.element("keys").elements("key");
							for(Element key : keys) {
								String keyValue = key.elementText("value");
								List<String> getLog = new ArrayList<String>();
								for(String log : logs) {
									if(log.indexOf(keyValue) != -1) {
										getLog.add(log);
										getLog.add("\n");
									}
								}
								FileUtil.writeToFile(getLog, nowPath.substring(0, nowPath.indexOf(".")) + "-" + key.elementText("name") + ".log");
							}
						}
					}
				}catch(Exception e) {
					logger.error(e.getMessage());
				}*/
			}
		}
	}
	
	private List<String> readLog(String filepath) {
		logger.info("log file path is : " + filepath);
		List<String> answer = new ArrayList<String>();
		try {
			//获取当期时间
			long nowTime = System.currentTimeMillis();
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
			boolean putOk = false;
			StringBuffer value = null;
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] items = line.split(" ");
				if(items.length >= 3 && items[0].matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
					if(value != null && putOk) {
						answer.add(value.toString());
					}
					value = new StringBuffer(line);
					String dateTime = items[0] + " " + items[1];
					putOk = CommonUtil.checkDateTime(dateTime, nowTime, 24);
				}else {
					if(value != null) value.append("\n").append(line);
				}
			}
		}catch(Exception e) {
			logger.error("error at read log file : " + e.getMessage());
//			e.printStackTrace();
		}
		return answer;
	}
	
	private String cutLog(String log) {
		String[] items = log.split(" ");
		String dateTime = items[0] + " " + items[1];
		String info = log.substring(log.indexOf(" - ") + 3);
		return dateTime + " : " + info;
	}
}
