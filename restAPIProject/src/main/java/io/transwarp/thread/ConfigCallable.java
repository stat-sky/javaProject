package io.transwarp.thread;

import io.transwarp.util.CommonUtil;
import io.transwarp.util.CommonString;
import io.transwarp.util.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class ConfigCallable implements Callable<Map<String, byte[]>>{

	private static Logger logger = Logger.getLogger(ConfigCallable.class);
	private HttpMethod method;
	private String serviceId;
	private String serviceType = "";
	
	public ConfigCallable(String manager, String username, String password, String serviceId) {
		this(manager, username, password, serviceId, "");
	}
	public ConfigCallable(String manager, String username, String password, String serviceId, String serviceType) {
		this(HttpMethod.getMethod(manager, username, password), serviceId, serviceType);
	}
	public ConfigCallable(HttpMethod method, String serviceId) {
		this(method, serviceId, "");
	}
	public ConfigCallable(HttpMethod method, String serviceId, String serviceType) {
		this.method = method;
		this.serviceId = serviceId;
		this.serviceType = serviceType;
	}
	
	@Override
	public Map<String, byte[]> call() {
		logger.info("get config of " + this.serviceType);
		Map<String, byte[]> files = null;
		try {
			Element config = CommonString.config_restAPI.getElementByChild("purpose", CommonString.DOWNLOAD_CONFIG);
			Map<String, Object> urlParam = new HashMap<String, Object>();
			urlParam.put("serviceId", this.serviceId);
			urlParam.put("fileName", CommonString.prop_env.getProperty("fileName"));
			String url = CommonUtil.buildURL(config.elementText("url"), urlParam);
			files = this.method.getConfig(url, serviceType);
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return files;
	}
}
