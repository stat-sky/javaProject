package io.transwarp.report;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.transwarp.api.ConfigAPI;
import io.transwarp.util.CommonString;
import io.transwarp.util.CommonUtil;
import io.transwarp.util.HttpMethod;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

public class CheckServiceConfig {
	
	private static Logger logger = Logger.getLogger(CheckServiceConfig.class);
	private HttpMethod method = null;
	
	public CheckServiceConfig() {
		this(HttpMethod.getMethod());
	}
	
	public CheckServiceConfig(HttpMethod method) {
		this.method = method;
	}
	
	public Map<String, String> getAllNodeConfig() {
		Map<String, String> answer = new HashMap<String, String>();
		String[] serviceTypes = CommonString.prop_report.getProperty("services").split(";");
		for(String serviceType : serviceTypes) {
			this.putInfoToConfig(answer, serviceType);
		}
		return answer;
	}
	
	//循环处理每个节点指定服务的配置获取
	private void putInfoToConfig(Map<String, String> answer, String serviceType) {
		try {
			ConfigAPI conf = new ConfigAPI(this.method, serviceType);
			Map<String, Configuration> xmlConfigs = conf.getConfigByXml();
			Map<String, Map<String, String>> shConfigs = conf.getConfigByShell();
			for(Iterator<String> keys = xmlConfigs.keySet().iterator(); keys.hasNext(); ) {
				String key = keys.next();
				String answer_value = answer.get(key);
				StringBuffer buffer;
				if(answer_value == null) {
					buffer = new StringBuffer(serviceType + " :\n");
				}else {
					buffer = new StringBuffer(answer_value).append("\n").append(serviceType).append(" :\n");
				}
				//因为sh的配置文件和xml的配置文件来自同一次rest api获取，素以所在节点相同
				Configuration xmlConfig = xmlConfigs.get(key);
				if(xmlConfig == null) logger.error("xmlConfig is null at service : " + serviceType);
				Map<String, String> shConfig = shConfigs.get(key);
				if(shConfig == null) logger.error("shConfig is null at service : " + serviceType);
				
				//根据配置来截取部分信息
				String[] files = CommonString.prop_report.getProperty(serviceType).split(";");
				for(String file : files ) {
					if(file.endsWith(".sh") == true || file.endsWith("-env") == true) {
						if(shConfig == null) continue;
						buffer.append("  ").append(file).append(" :\n");
						String[] values = CommonString.prop_report.getProperty(file).split(";");
						Map<String, String> temp = new HashMap<String, String>();
						for(String value : values) {
							temp.put(value, shConfig.get(value).replaceAll(",", "\n").replaceAll(" ", "\n"));
						}
						buffer.append(CommonUtil.paddingBegin(CommonUtil.printByTable(temp), "    "));
					}else if(file.endsWith(".xml") == true) {
						if(xmlConfig == null) continue;
						buffer.append("  ").append(file).append(" :\n");
						String[] values = CommonString.prop_report.getProperty(file).split(";");
						Map<String, String> temp = new HashMap<String, String>();
						for(String value : values) {
//							logger.info("value : " + xmlConfig.get(value));
							String xmlValue = xmlConfig.get(value);
							if(xmlValue == null) xmlValue = "";
							temp.put(value, xmlValue.replaceAll(",", "\n").replaceAll(" ", "\n"));
						}
						buffer.append(CommonUtil.paddingBegin(CommonUtil.printByTable(temp), "    "));

					}
				}
				answer.put(key, buffer.toString());
			}
		}catch(Exception e) {
			logger.error("error at get config of " + serviceType + " : " + e.getMessage());
//			e.printStackTrace();
		}
	}
}
