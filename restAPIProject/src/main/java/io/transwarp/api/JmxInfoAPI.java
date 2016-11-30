package io.transwarp.api;

import java.util.List;

import io.transwarp.util.CommonString;
import io.transwarp.util.SessionTool;

import org.apache.log4j.Logger;
import org.dom4j.Element;


public class JmxInfoAPI {

	private static Logger logger = Logger.getLogger(JmxInfoAPI.class);
	private SessionTool session;
	
	public JmxInfoAPI(String ipAddress, String nodeUser, String nodePwd) throws Exception{
		this(SessionTool.getSession(ipAddress, nodeUser, nodePwd));
	}
	public JmxInfoAPI(SessionTool session) {
		this.session = session;
	}
	
	public String getInfoByShell(Element config) {
		StringBuffer answer = new StringBuffer();
		String command = null;
		try {
			command = config.elementText("command");
			logger.info("exec command is : " + command);
			String cmd_result = this.session.executeDis(command);
			//获取配置，判断是否要对结果进行处理
			Element properties = config.element("properties");
			if(properties != null && properties.elements("property") != null && properties.elements("property").size() != 0) {
				answer.append(this.analysisResult(cmd_result, properties.elements("property")));
			}else {
				answer.append(cmd_result);
			}
		}catch(Exception e) {
			logger.error("error at get jvm info , command is \"" + command + "\" , error message is : " + e.getMessage());
		}
		return answer.toString();
	}
	
	private String analysisResult(String cmd_result, List<Element> properties) {
		StringBuffer result = new StringBuffer();
		String[] lines = cmd_result.split("\n");
		for(String line : lines) {
			//获取开头字段作为该行信息的关键字key
			int keyEnd = line.indexOf("=",1);
			keyEnd = keyEnd < 0 ? line.length() : keyEnd;
			String key = line.substring(0, keyEnd);
			//循环判断该行数据是否包含需要的关键字
			for(Element prop : properties) {
				String prop_name = prop.elementText("prop_name");
				if(key.indexOf(prop_name) != -1) {
					if(prop.elementText("cut_out").equals("0")) {
						result.append(line).append("\n");
					}else {
						result.append(prop_name).append(" : \n");
						//获取该行的分隔符和获取的子项关键字
						String delimited = prop.elementText("delimited");
						delimited = delimited.equals("") ? " " : delimited;
						String[] itemKeys = prop.elementText("prop_value").split(";");
						String[] items = line.split(delimited);
						for(String item : items) {
							for(String itemKey : itemKeys) {
								if(item.indexOf(itemKey) != -1) {
									result.append("  ").append(item).append("\n");
									break;
								}
							}
						}
					}
				}
			}
		}
		return result.toString();
	}
}
