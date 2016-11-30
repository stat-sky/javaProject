package io.transwarp.thread;

import io.transwarp.bean.RoleBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.util.CommonUtil;
import io.transwarp.util.CommonString;
import io.transwarp.util.HttpMethod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.dom4j.Element;

/**
 * 实现功能：
 * 		1、输入参数serviceId则获取指定服务的详细信息和该服务下所有的服务角色信息
 * 		2、不输入参数serviceId则获取所有服务的idname信息
 * @author 30453
 *
 */
public class ServiceCallable implements Callable<Map<String, ServiceBean>> {

	private static Logger logger = Logger.getLogger(ServiceCallable.class);
	private HttpMethod method;
	private String serviceId;
	private String viewtype;
	
	public ServiceCallable(String serviceId, String viewtype) {
		this(serviceId, viewtype, HttpMethod.getMethod());
	}
	public ServiceCallable(String serviceId, String viewtype, HttpMethod method) {
		this.method = method;
		this.serviceId = serviceId;
		this.viewtype = viewtype;
	}
	
	@Override
	public Map<String, ServiceBean> call() {
		Map<String, ServiceBean> services = new HashMap<String, ServiceBean>();
		try {
			//构建获取信息的url
			Map<String, Object> urlParam = new HashMap<String, Object>();
			urlParam.put("serviceId", serviceId);
			urlParam.put("viewtype", viewtype);
			Element config = null;
			if(this.serviceId == null) {
				config = CommonString.config_restAPI.getElementByChild("purpose", CommonString.FIND_MORE_SERVICE);
			}else {
				config = CommonString.config_restAPI.getElementByChild("purpose", CommonString.FIND_SERVICE);
			}
			
			String url = CommonUtil.buildURL(config.elementText("url"), urlParam);
			String httpMethod = config.elementText("http-method");
			String result = this.method.execute(url, httpMethod);
			//判断是否有返回结果
			if(result == null) {
				logger.error("this services is not found");
			}else {
				//判断返回为一个服务还是多个服务
				if(this.serviceId == null) {	//未指定查询服务ID，则为查询多个服务
					JSONArray array = JSONArray.fromObject(result);
					int length = array.size();
					logger.info("there are " + length + " have been found");
					for(int i = 0; i < length; i++) {
						ServiceBean service = new ServiceBean(array.getJSONObject(i));
						services.put(service.getName(), service);
					}
				}else {
					logger.info("found service's id is " + serviceId);
					ServiceBean service = new ServiceBean(result);
					services.put(service.getName(), service);
				}
			}
			//根据viewtype的值确定是否查询服务角色内容
			if(this.viewtype != null && this.viewtype.equals("summary")) {
				for(Iterator<String> keys = services.keySet().iterator(); keys.hasNext(); ) {
					String key = keys.next();
					ServiceBean service = services.get(key);
					RoleCallable roleCallable = new RoleCallable(service.getId(), this.method);
					Map<String, RoleBean> roles = roleCallable.call();
					for(Iterator<String> roleKeys = roles.keySet().iterator(); roleKeys.hasNext(); ) {
						String roleKey = roleKeys.next();
						service.addRole(roles.get(roleKey));
					}
					services.put(key, service);
				}
			}
		}catch(Exception e) {
			logger.error("the thread of get service error : " + e.getMessage());
		}
		return services;
	}
	
	
}
