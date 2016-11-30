package io.transwarp.api;

import io.transwarp.bean.ServiceBean;
import io.transwarp.thread.ConfigCallable;
import io.transwarp.thread.ServiceCallable;
import io.transwarp.util.CommonString;
import io.transwarp.util.HttpMethod;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.log4j.Logger;

/**
 * 实现功能：
 * 		1、解析获取指定服务的.xml配置文件，用HBaseConfiguration返回；
 * 		2、解析获取指定服务的.properties配置文件，用Properties返回；
 * 		3、解析获取指定服务的.sh配置文件，截取其中含"_OPTS"的配置，用Map<String, String>返回；
 * 		4、解析获取指定无法的exclude_list.txt文件，用String[]返回；
 * @author 30453
 *
 */
public class ConfigAPI {

	private static Logger logger = Logger.getLogger(ConfigAPI.class);
	private HttpMethod method;
	private String serviceType;
	private Map<String, byte[]> configFiles;
	private static Map<String, ServiceBean> services;
	
	public ConfigAPI(String serviceType) throws Exception {
		this(HttpMethod.getMethod(), serviceType);
	}
	public ConfigAPI(String manager, String username, String password, String serviceType) throws Exception {
		this(HttpMethod.getMethod(manager, username, password), serviceType);
	}
	public ConfigAPI(HttpMethod method, String serviceType) throws Exception {
		this.method = method;
		this.serviceType = serviceType.toUpperCase();
		init();
	}
	
	private void init() {
		//列出所有服务的简要信息，用于根据服务类型查询服务ID
		if(ConfigAPI.services == null) {
			ServiceCallable serviceCallable = new ServiceCallable(null, "idname", this.method);
			ConfigAPI.services = serviceCallable.call();			
		}

		//查询服务ID
		ServiceBean service = null;
		for(Iterator<String> keys = services.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			ServiceBean temp = services.get(key);
			if(temp.getType().equals(this.serviceType)) {
				service = temp;
				break;
			}
		}
		//未找到服务
		if(service == null) {
			logger.error("no found service");
			throw new RuntimeException("no found service");
		}
		//获取该服务的配置
		String serviceId = service.getId();
		ConfigCallable configCallable = new ConfigCallable(this.method, serviceId, serviceType);
		this.configFiles = configCallable.call();
	}
	
/*	public void writeToFile() {
		for(Iterator<String> keys = this.configFiles.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			try {
				String value = new String(this.configFiles.get(key), "utf-8");
				String path = "E:/temp/" + key;
				File file = new File(path);
				if(!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				FileUtil.writeToFile(value, path);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}
		}
	}*/
	
	public Map<String, Map<String, String>> getConfigByShell() throws Exception{
		Map<String, Map<String, String>> answer = new HashMap<String, Map<String, String>>();
		for(Iterator<String> keys = this.configFiles.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			if(key.endsWith(".sh") == false && key.endsWith("-env") == false) continue;
			//获取该配置所在节点的hostname
			String[] buf = key.split("/");
			if(buf.length < 2) {
				logger.error(".sh config file error");
				throw new RuntimeException(".sh config file error");
			}
			String hostname = buf[1];
			
			Map<String, String> configShell = answer.get(hostname);
			if(configShell == null || configShell.size() == 0) {
				configShell = new HashMap<String, String>();
			}
			String[] lines = new String(this.configFiles.get(key), "utf-8").split("\n");
			for(String line : lines) {
				//去掉首尾空白
				line = line.trim();
				//过滤注释行和空行
				if(line.indexOf("#") != -1 || line.equals("")) continue;
				//确定变量名的位置
				int beginIndex = line.indexOf(' ', 1) + 1;
				int endIndex = line.indexOf('=', 1);
				if(beginIndex >= endIndex) {
					beginIndex = 0;
				}
				//跳过非赋值行
				if(endIndex < 0) continue;
				//变量名
				String paramName = line.substring(beginIndex, endIndex);
				//变量值
				String value = line.substring(endIndex + 1);
				//去掉值中的双引号
				if(value.indexOf("\"") != -1) value = value.substring(1, value.length() - 1);
				
				//判断是否已有该变量的值，若有则进行选择
				String oldValue = configShell.get(paramName);
				if(oldValue != null) {
					if(value.indexOf("-Xms") != -1 || value.indexOf("-Xmx") != -1) configShell.put(paramName, value);
					else configShell.put(paramName, oldValue);
				}else {
					configShell.put(paramName, value);
				}
				
			}
			answer.put(hostname, configShell);
		}
		return answer;
	}
	
	public Map<String, Configuration> getConfigByXml() throws Exception {
		Map<String, Configuration> answer = new HashMap<String, Configuration>();
		for(Iterator<String> keys = this.configFiles.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			if(key.endsWith(".xml") == false) continue;
			//获取该配置所在节点的hostname
			String[] buf = key.split("/");
			if(buf.length < 2) {
				logger.error(".xml config file error");
				throw new RuntimeException("config file error");
			}
			String hostname = buf[1];
			
			Configuration configXml = answer.get(hostname);
			if(configXml == null) {
				configXml = HBaseConfiguration.create();
			}
			byte[] xmlValue = this.configFiles.get(key);
/*			if(key.indexOf(".xml") != -1 && key.indexOf("inceptor1") != -1) {
				System.out.println(key);
				System.out.println("\n" + new String(xmlValue, "utf-8"));
			}*/
			InputStream inputStream = new ByteArrayInputStream(xmlValue);
			configXml.addResource(inputStream);
			answer.put(hostname, configXml);
		}
		
		return answer;
	}
	
	public Map<String, Properties> getConfigByProp() throws Exception {
		Map<String, Properties> answer = new HashMap<String, Properties>();
		for(Iterator<String> keys = this.configFiles.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			if(key.endsWith(".properties") == false) continue;
			//获取该配置所在节点的hostname
			String[] buf = key.split("/");
			if(buf.length < 2) {
				logger.error(".properties config file error");
				throw new RuntimeException(".properties config file error");
			}
			String hostname = buf[1];
			
			Properties prop = answer.get(key);
			if(prop == null) {
				prop = new Properties();
			}
			InputStream inputStream = new ByteArrayInputStream(this.configFiles.get(key));
			prop.load(inputStream);
			answer.put(hostname, prop);
		}
		
		return answer;
	}
	
	public Map<String, StringBuffer> getExclude_list() {
		Map<String, StringBuffer> answer = new HashMap<String, StringBuffer>();
		for(Iterator<String> keys = this.configFiles.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			if(key.indexOf("exclude") != -1 ) continue;
			//获取该配置所在节点的hostname
			String[] buf = key.split("/");
			if(buf.length < 2) {
				logger.error(".properties config file error");
				throw new RuntimeException(".properties config file error");
			}
			String hostname = buf[1];
			
			StringBuffer buffer = answer.get(hostname);
			if(buffer == null) {
				buffer = new StringBuffer();
			}
			buffer.append(new String(this.configFiles.get(key)));
			answer.put(hostname, buffer);
		}
		return answer;
	}
}
