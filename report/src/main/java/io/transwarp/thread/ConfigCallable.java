package io.transwarp.thread;


import io.transwarp.util.Constant;
import io.transwarp.util.HttpMethodTool;
import io.transwarp.util.UtilTool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class ConfigCallable implements Callable<Map<String, byte[]>>{

	private static Logger logger = Logger.getLogger(ConfigCallable.class);
	private HttpMethodTool method;
	private String serviceId;
	private String serviceType = "";
	
	public ConfigCallable(HttpMethodTool method, String serviceId, String serviceType) {
		this.method = method;
		this.serviceId = serviceId;
		this.serviceType = serviceType;
	}
	
	@Override
	public Map<String, byte[]> call() {
		logger.info("get config of " + this.serviceType);
		Map<String, byte[]> files = null;
		try {
			Element config = Constant.prop_restapi.getElement("purpose", Constant.DOWNLOAD_CONFIG);
			Map<String, Object> urlParam = new HashMap<String, Object>();
			urlParam.put("serviceId", this.serviceId);
			urlParam.put("fileName", "config");
			String url = UtilTool.buildURL(config.elementText("url"), urlParam);
			files = this.method.getConfig(url, serviceType);
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return files;
	}
}
