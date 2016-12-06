package io.transwarp.thread;

import io.transwarp.bean.ServiceBean;
import io.transwarp.util.Constant;
import io.transwarp.util.HttpMethodTool;
import io.transwarp.util.UtilTool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class ServiceCallable implements Callable<Map<String, ServiceBean>>{

	private static Logger logger = Logger.getLogger(ServiceCallable.class);
	private HttpMethodTool method;
	private String serviceId;
	private String viewtype;
	
	public ServiceCallable(HttpMethodTool method, String viewtype, String serviceId) {
		this.method = method;
		this.serviceId = serviceId;
		this.viewtype = viewtype;
	}
	
	@Override
	public Map<String, ServiceBean> call() {
		Map<String, ServiceBean> services = new HashMap<String, ServiceBean>();
		try {
			//获取配置
			Element config = null;
			if(this.serviceId == null) {  //根据服务ID的有无判断查询多个服务还是单个服务
				config = Constant.prop_restapi.getElement("purpose", Constant.FIND_MORE_SERVICE);
			}else {
				config = Constant.prop_restapi.getElement("purpose", Constant.FIND_SERVICE);
			}
			//构建URL
			Map<String, Object> urlParam = new HashMap<String, Object>();
			urlParam.put("serviceId", serviceId);
			urlParam.put("viewtype", viewtype);
			String url = UtilTool.buildURL(config.elementText("url"), urlParam);
			//执行http方法，获得结果
			String exec_result = this.method.execute(url, config.elementText("http-method"), null);
			if(exec_result == null || exec_result.equals("")) {  //以返回结果的有无判断是否存在查询的服务
				logger.error("this service not found!");
			}else {
				//判断返回为一个服务还是多个服务
				if(this.serviceId == null) {	//未指定查询服务ID，则为查询多个服务
					JSONArray array = JSONArray.fromObject(exec_result);
					int length = array.size();
					logger.info("there are " + length + " have been found");
					for(int i = 0; i < length; i++) {
						ServiceBean service = new ServiceBean(array.getJSONObject(i));
						services.put(service.getName(), service);
					}
				}else {
					logger.info("found service's id is " + serviceId);
					ServiceBean service = new ServiceBean(exec_result);
					services.put(service.getName(), service);
				}
			}
		}catch(Exception e) {
			logger.error("find service information error : " + e.getMessage());
		}
		return services;
	}
}
